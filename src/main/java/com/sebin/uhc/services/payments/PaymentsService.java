package com.sebin.uhc.services.payments;

import com.sebin.uhc.commons.*;
import com.sebin.uhc.entities.onboarding.Beneficiaries;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.entities.payments.FundsTransferRequests;
import com.sebin.uhc.exceptions.ExceptionManager;
import com.sebin.uhc.models.PaymentPurpose;
import com.sebin.uhc.models.RequestLogModel;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.requests.payments.FundsTransferRequest;
import com.sebin.uhc.models.responses.onboarding.Header;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.repositories.onboarding.BeneficiaryRepository;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepository;
import com.sebin.uhc.repositories.payments.FundsTransferRequestsRepository;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j(topic = ":: SERVICE :: BENEFICIARY :::")
@Service
@Transactional
public class PaymentsService {

    @Autowired
    private FundsTransferRequestsRepository fundsTransferRequestsRepository;
    @Autowired
    private Configs configs;
    @Autowired
    private RequestLogTrailService requestLogTrailService;
    @Autowired
    AccessTokenLoader accessTokenLoader;
    @Autowired
    private SubscriptionsRepository subscriptionsRepository;
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    public Response<?> processTransferRequest(Request<FundsTransferRequest> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\n").append("Validation started.");
            log.info("Validation for request addition request started {}", new Date());
            Predicate<FundsTransferRequest> beneficiaryPredicate = Objects::isNull;
            Predicate<String> stringPredicate = str -> str == null || str.isBlank();
            if(beneficiaryPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Null or empty request.");
                log.info("Null request request {}", new Date());
                throw new ExceptionManager("Null request, could not proceed.", ResponseCodes.NULL_REQUEST.getCode());
            }

            if(stringPredicate.test(request.getBody().getIdNumber())) {
                stringBuilder.append("\n").append("ID Number missing in the request.");
                log.info("ID Number missing in the request {}", new Date());
                throw new ExceptionManager("Your ID Number is missing in the request.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(stringPredicate.test(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Mobile Number missing in the request.");
                log.info("Mobile Number missing in the request {}", new Date());
                throw new ExceptionManager("Your Mobile Number is missing in the request.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(stringPredicate.test(request.getBody().getAmount())) {
                stringBuilder.append("\n").append("Amount missing in the request.");
                log.info("Amount missing in the request {}", new Date());
                throw new ExceptionManager("Amount is missing in the request.", ResponseCodes.AMOUNT_MISSING.getCode());
            }

            if(!Helper.isAmountValid(request.getBody().getAmount()) || Double.parseDouble(request.getBody().getAmount()) <= 0) {
                stringBuilder.append("\n").append("Amount invalid in the request.");
                log.info("Amount invalid in the request {}", new Date());
                throw new ExceptionManager("Amount invalid.", ResponseCodes.INVALID_AMOUNT.getCode());
            }

            if((Integer.parseInt(request.getBody().getAmount())) % 500 != 0) {
                stringBuilder.append("\n").append("Amount must be in multiple of 500.");
                log.info("Amount must be in multiple of 500 in the request {}", new Date());
                throw new ExceptionManager("Amount must be in multiple of 500", ResponseCodes.INVALID_AMOUNT.getCode());
            }

            if(stringPredicate.test(request.getBody().getBeneficiaryIdOrPassportNumber())) {
                stringBuilder.append("\n").append("Beneficiary Id or passport missing in the request.");
                log.info("Beneficiary Id or passport missing in the request {}", new Date());
                throw new ExceptionManager("Beneficiary Id or passport is missing in the request.", ResponseCodes.BENEFICIARY_ID_MISSING.getCode());
            }

            if(stringPredicate.test(request.getBody().getPIN())) {
                stringBuilder.append("\n").append("PIN missing in the request.");
                log.info("PIN missing in the request {}", new Date());
                throw new ExceptionManager("PIN is missing in the request.", ResponseCodes.PIN_MISSING.getCode());
            }

            Optional<Subscriptions> person = subscriptionsRepository.findByPersonId(request.getBody().getIdNumber());
            if(person.isEmpty()) {
                stringBuilder.append("\n").append(String.format("Subscriber %s not found", request.getBody()));
                log.info("Subscriber {} not found.", request);
                return new Response<>(new Header(String.format("Subscriber %s not found.", request.getBody().getIdNumber()), ResponseCodes.BENEFICIARY_OR_SPONSOR_MISSING.getCode()));
            }

            if(person.get().getWallet().getAmount() < Double.parseDouble(request.getBody().getAmount()))
            {
                stringBuilder.append("\n").append(String.format("Insufficient balance {}", new Date()));
                log.info("Insufficient balance {}", new Date());
                return new Response<>(new Header(String.format("Insufficient balance for %s", request.getBody().getIdNumber()), ResponseCodes.INSUFFICIENT_BALANCE.getCode()));
            }

            if(stringPredicate.test(request.getBody().getPurpose())) {
                stringBuilder.append("\n").append("Payment Purpose missing in the request.");
                log.info("Payment Purpose missing in the request {}", new Date());
                throw new ExceptionManager("Payment Purpose is missing in the request.", ResponseCodes.PAYMENT_PURPOSE_MISSING.getCode());
            }

            if(!(EnumUtils.isValidEnum(PaymentPurpose.class, request.getBody().getPurpose().toUpperCase())))
            {
                stringBuilder.append("\n").append("Invalid Payment Purpose");
                log.info("Invalid Payment Purpose in the request {}", new Date());
                throw new ExceptionManager("Invalid Payment Purpose in the request.", ResponseCodes.INVALID_PAYMENT_PUROSE.getCode());
            }

            Optional<Beneficiaries> beneficiary =  beneficiaryRepository.findByPersonIdAndStatus(request.getBody().getBeneficiaryIdOrPassportNumber(),Statuses.ACTIVE.getStatus());
            if(beneficiary.isPresent()) {
                if (beneficiary.get().getSubscriptions().getId() != person.get().getId()) {
                    stringBuilder.append("\n").append("Sponsor Beneficiary not found");
                    log.info("Sponsor Beneficiary with Id {} for the sponsor with phone number {} was not found. {}", request.getBody().getBeneficiaryIdOrPassportNumber(), request.getBody().getMobileNumber(), new Date());
                    return new Response<>(new Header(String.format("Sponsor Beneficiary with Id %s was not found.", request.getBody().getBeneficiaryIdOrPassportNumber()), ResponseCodes.BENEFICIARY_OR_SPONSOR_MISSING.getCode()));
                }

                if (beneficiary.get().getMemberNumber() == null || beneficiary.get().getMemberNumber().equals("")) {
                    stringBuilder.append("\n").append("Beneficiary has no member number");
                    log.info("Beneficiary  {} has no member number", request.getBody().getBeneficiaryIdOrPassportNumber());
                    return new Response<>(new Header(String.format("Sponsor Beneficiary with Id %s has no member number.", request.getBody().getBeneficiaryIdOrPassportNumber()), ResponseCodes.MEMBER_NUMBER_MISSING.getCode()));
                }
            }
            else
            {
                System.out.println("beneficiary "+request.getBody().getBeneficiaryIdOrPassportNumber()+" was not found at "+LocalDateTime.now());
                throw new ExceptionManager("Beneficiary not found.", ResponseCodes.BENEFICIARY_OR_SPONSOR_MISSING.getCode());
            }

            if(!PasswordHandler.validatePassword(request.getBody().getPIN(),person.get().getPassword())) {
                log.info("Invalid password for request {} at {}", requestLogModel.getRequestId(), new Date());
                stringBuilder.append("\n").append("Invalid password.");
                throw new ExceptionManager("Incorrect PIN", ResponseCodes.PIN_INCORRECT.getCode());
            }


            FundsTransferRequests fundsTransferRequests = new FundsTransferRequests();
            fundsTransferRequests.setIdNumber(request.getBody().getIdNumber());
            fundsTransferRequests.setMobileNumber(request.getBody().getMobileNumber());
            fundsTransferRequests.setAmount(Double.parseDouble(request.getBody().getAmount()));
            fundsTransferRequests.setBeneficiaryIdOrPassportNumber(request.getBody().getBeneficiaryIdOrPassportNumber());
            fundsTransferRequests.setBeneficiaryMemberNumber(beneficiary.get().getMemberNumber());
            fundsTransferRequests.setDescription(request.getBody().getDescription());
            fundsTransferRequests.setPurpose(request.getBody().getPurpose().toUpperCase());
            fundsTransferRequests.setDateCreated(LocalDateTime.now());
            fundsTransferRequests.setReferenceNumber(General.getReference("F"+request.getBody().getIdNumber().trim()));

            Optional<FundsTransferRequests> pendingRequest = fundsTransferRequestsRepository.findPendingTransactions(fundsTransferRequests.getMobileNumber(),fundsTransferRequests.getAmount(),fundsTransferRequests.getBeneficiaryIdOrPassportNumber(),fundsTransferRequests.getPurpose(),false);
            if(pendingRequest.isPresent()) {
                throw new ExceptionManager("Similar Request Pending", ResponseCodes.SIMILAR_REQUEST_PENDING.getCode());
            }

            fundsTransferRequestsRepository.save(fundsTransferRequests);

            return new Response<>(new Header(true,  ResponseCodes.SUCCESS.getCode(), "Payment initiated successfully."));

        } catch (Exception ex) {
            ex.printStackTrace();
            stringBuilder.append("\n").append("Exception ").append(ex.getMessage());
            log.error("Exception occurred while processing FundsTransferRequests request request {} for sponsor {}. Error message {} at {}", request.getBody().getMobileNumber(), request.getBody().getAmount(), ex.getMessage(), new Date());
            throw new ExceptionManager(ex.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log trail.");
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
            } catch (Exception exception) {
                log.error("Exception while saving request log trail for {} at {}", request.getHeader().getRequestId(), new Date());
            }
        }
    }

}
