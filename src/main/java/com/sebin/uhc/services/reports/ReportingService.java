package com.sebin.uhc.services.reports;

import com.sebin.uhc.commons.Criteria;
import com.sebin.uhc.commons.Helper;
import com.sebin.uhc.commons.ResponseCodes;
import com.sebin.uhc.entities.WalletTransactions;
import com.sebin.uhc.entities.notifications.Sms;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.exceptions.ExceptionManager;
import com.sebin.uhc.models.RequestLogModel;
import com.sebin.uhc.models.SmsContext;
import com.sebin.uhc.models.reports.Statement;
import com.sebin.uhc.models.reports.StatementRequest;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.responses.onboarding.Header;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.repositories.SmsRepository;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepository;
import com.sebin.uhc.repositories.payments.WalletTransactionRepository;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
public class ReportingService {
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private SubscriptionsRepository subscriptionsRepository;
    @Autowired
    private SmsRepository smsRepository;
    @Autowired
    private RequestLogTrailService requestLogTrailService;

    public Response<List<Statement>> statement(Request<StatementRequest> request, RequestLogModel requestLogModel) {
        log.info("Mini-statement parameter validation");
        StringBuilder stringBuilder = new StringBuilder("Request received");
        try {
            List<Statement> statements = new ArrayList<>();

            Predicate<StatementRequest> stringPredicate = r -> r == null || r.getIdNumber().isBlank() || r.getMobileNumber().isBlank() || r.getStatementType().isBlank();
            if(stringPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Missing national Id, passport, mobile number, criterion or request type");
                log.info("Could not generate mini statement, no subscriber parameter was provided at {}", new Date());
                throw new ExceptionManager("Missing national Id, passport or mobile number in the request.", ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            Optional<Subscriptions> subscriptions = subscriptionsRepository.findByPersonId(request.getBody().getIdNumber());

            if(subscriptions.isEmpty()) {
                stringBuilder.append("\n").append("Subscriber not found.");
                log.info("Subscriber {} not found, cannot run statement at {}", request.getBody().getIdNumber(), new Date());
                throw new ExceptionManager(String.format("Subscriber %s not found, cannot run statement.", request.getBody().getIdNumber()), ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            if(subscriptions.get().getWallet() == null) {
                return new Response<>(new Header(true, ResponseCodes.SUCCESS.getCode(), "Statement generated successfully"), statements);
            }

            List<WalletTransactions> walletTransactions;
            if(request.getBody().getStatementType().equalsIgnoreCase(Criteria.MINI.getCriterion()))
                walletTransactions = walletTransactionRepository.findTop5ByWalletOrderByDateCreatedDesc(subscriptions.get().getWallet());
            else {
                if(request.getBody().getNumberOfMonths() <=0 ) {
                    log.info("Months cannot be zero or less for request {}", request.getHeader().getRequestId());
                    throw new ExceptionManager("Months cannot be zero or less for full statement.", ResponseCodes.FAIL.getCode());
                }

                LocalDateTime localDateTime = LocalDateTime.now();
                walletTransactions = walletTransactionRepository.findByWalletAndDateCreatedBetweenOrderByDateCreatedDesc(subscriptions.get().getWallet(), localDateTime.minusMonths(request.getBody().getNumberOfMonths()), localDateTime);
            }

            if(walletTransactions.isEmpty()) {
                stringBuilder.append("\n").append("Could not find wallet transactions.");
                log.info("No wallet transactions were found for {} at {} for the request {}", request.getBody().getIdNumber(), new Date(), request.getHeader().getRequestId());
            }


            StringBuilder s = new StringBuilder("===Mini Statement===");
            for(WalletTransactions wt : walletTransactions) {
                statements.add(new Statement(wt.getAmount(), wt.getWalletBalance(), wt.getTransactionType(), wt.getDateCreated()));
                s.append("\n").append(String.format("Date: %s, Amount: %s, Type: %s", wt.getDateCreated(), wt.getAmount(), wt.getTransactionType()));
            }
            s.append("\n").append("===END===");

            stringBuilder.append("\n").append(String.format("Statement request successful. Records found %s", statements.size()));

            if(request.getBody().getStatementType().equals(Criteria.MINI.getCriterion())) {
                log.info("Being a mini-statement, an sms entry is being created for sending to subscriber for request {} at {}", request.getHeader().getRequestId(), new Date());
                Sms sms = new Sms();
                sms.setDateCreated(LocalDateTime.now());
                sms.setSmsContext(SmsContext.MINI_STATEMENT);
                sms.setMobileNumber(subscriptions.get().getMobileNumber());
                sms.setMessage(s.toString());
                smsRepository.save(sms);
                stringBuilder.append("\n").append("Creating sms entry.");
            }

            return new Response<>(new Header(true, ResponseCodes.SUCCESS.getCode(), "Statement generated successfully"), statements);
        } catch (Exception exception) {
            stringBuilder.append("\n").append("Exception: ").append(exception.getMessage());
            log.error("Exception while processing statement for {} at {}", request.getBody().getIdNumber(), new Date());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                log.info("Saving request log trail to db at {} for request {}", new Date(), request.getHeader().getRequestId());
                Helper.saveTrailLog(requestLogModel,requestLogTrailService);
            } catch (Exception exception) {
                log.error("Exception while saving the request log trail to db at {} for request {}", new Date(), request.getHeader().getRequestId());
            }
        }
    }
}
