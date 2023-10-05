package com.sebin.uhc.services.payments;

import com.google.gson.Gson;
import com.sebin.uhc.commons.*;
import com.sebin.uhc.entities.Wallet;
import com.sebin.uhc.entities.WalletTransactions;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.entities.payments.MpesaRequests;
import com.sebin.uhc.exceptions.ExceptionManager;
import com.sebin.uhc.models.RequestLogModel;
import com.sebin.uhc.models.TokenTypes;
import com.sebin.uhc.models.mpesa.MpesaBody;
import com.sebin.uhc.models.mpesa.WalletBalance;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.requests.payments.Mpesa;
import com.sebin.uhc.models.requests.payments.STKPush;
import com.sebin.uhc.models.requests.payments.WalletBalanceRequest;
import com.sebin.uhc.models.responses.onboarding.Header;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.models.responses.payments.MpesaNotification;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepository;
import com.sebin.uhc.repositories.payments.MpesaRequestsRepository;
import com.sebin.uhc.repositories.payments.WalletRepository;
import com.sebin.uhc.repositories.payments.WalletTransactionRepository;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

@Slf4j(topic = ":: SERVICE :: BENEFICIARY :::")
@Service
@Transactional
public class MpesaService {

    @Autowired
    private MpesaRequestsRepository mpesaRequestsRepository;
    @Autowired
    private Configs configs;
    @Autowired
    private RequestLogTrailService requestLogTrailService;
    @Autowired
    private AccessTokenLoader accessTokenLoader;

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private WalletRepository walletRepository;

    public Response<?> processMpesaRequest(Request<Mpesa> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\n").append("Validation started.");
            log.info("Validation for request addition request started {}", new Date());
            Predicate<Mpesa> beneficiaryPredicate = Objects::isNull;
            Predicate<String> stringPredicate = str -> str == null || str.isBlank();
            if(beneficiaryPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Null or empty request.");
                log.info("Null request request {}", new Date());
                throw new ExceptionManager("Null request, could not proceed.", ResponseCodes.NULL_REQUEST.getCode());
            }

            if(stringPredicate.test(request.getBody().getIdNumber())) {
                stringBuilder.append("\n").append("ID Number missing in the request.");
                log.info("ID Number missing in the request {}", new Date());
                throw new ExceptionManager("Your ID Number is missing in the request.", ResponseCodes.MOBILE.getCode());
            }

            if(stringPredicate.test(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Mobile Number missing in the request.");
                log.info("Mobile Number missing in the request {}", new Date());
                throw new ExceptionManager("Your Mobile Number is missing in the request.", ResponseCodes.MOBILE.getCode());
            }

            if(stringPredicate.test(request.getBody().getAmount())) {
                stringBuilder.append("\n").append("Amount missing in the request.");
                log.info("Amount missing in the request {}", new Date());
                throw new ExceptionManager("Amount is missing in the request.", ResponseCodes.AMOUNT.getCode());
            }

            if(!Helper.isAmountValid(request.getBody().getAmount())) {
                stringBuilder.append("\n").append("Amount value not valid");
                log.info("Amount value for the request {} at {} not valid", requestLogModel.getRequestId(), new Date());
                throw new ExceptionManager("Amount not valid");
            }

            if(Double.parseDouble(request.getBody().getAmount()) <= 0) {
                stringBuilder.append("\n").append(String.format("Amount value(%s) not acceptable", request.getBody().getAmount()));
                log.info("Amount {} not valid for request {} at {}", request.getBody().getAmount(), requestLogModel.getRequestId(), new Date());
                throw new ExceptionManager("Amount not valid");
            }

            Optional<Subscriptions> person = subscriptionsRepository.findByPersonId(request.getBody().getIdNumber());
            if(person.isEmpty()) {
                stringBuilder.append("\n").append(String.format("Subscriber %s not found", request.getBody()));
                log.info("Subscriber {} not found.", request);
                return new Response<>(new Header(String.format("Subscriber %s not found.", request.getBody().getIdNumber()), ResponseCodes.BENE_SPONSOR.getCode()));
            }

            MpesaRequests mpesaRequests = new MpesaRequests();
            mpesaRequests.setIdNumber(request.getBody().getIdNumber());
            mpesaRequests.setMobileNumber(request.getBody().getMobileNumber());
            mpesaRequests.setAmount(request.getBody().getAmount());
            mpesaRequests.setDescription(request.getBody().getDescription());
            mpesaRequests.setDateCreated(LocalDateTime.now());
            mpesaRequests.setReferenceNumber(General.getReference("MP"+request.getBody().getIdNumber()));
            MpesaRequests savedRequest = mpesaRequestsRepository.save(mpesaRequests);

            new Thread(() -> initiateSTKPush(savedRequest)).start();

            return new Response<>(new Header(true,  ResponseCodes.SUCCESS.getCode(), "STK push initiated successfully."));

        } catch (Exception ex) {
            stringBuilder.append("\n").append("Exception ").append(ex.getMessage());
            log.error("Exception occurred while processing mpesa request request {} for sponsor {}. Error message {} at {}", request.getBody().getIdNumber(), request.getBody().getAmount(), ex.getMessage(), new Date());
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

    private void initiateSTKPush(MpesaRequests mpesaRequests) {
        STKPush stkPush = getStkPush(mpesaRequests);
        String accessToken = accessTokenLoader.getToken(TokenTypes.STKPUSH);
        if (accessToken == null) {
            log.info("Null or empty access token at {}", new Date());
            mpesaRequests.setRequestTime(LocalDateTime.now());
            mpesaRequests.setResponseDescription("Null access token, could not continue.");
            return;
        }

        String request = new Gson().toJson(stkPush);
        mpesaRequests.setRequest(request);
        mpesaRequests.setRequestTime(LocalDateTime.now());
        String stk_resp = General.send_request(mpesaRequests.getReferenceNumber(), configs.getMpesa_stk_push_url(), request, AppConstants.APPLICATION_JSON, accessToken);
        mpesaRequests.setResponse(stk_resp);
        mpesaRequests.setResponseTime(LocalDateTime.now());
        if (stk_resp.isBlank()) {
            log.info("Response was null or empty {}", new Date());
            mpesaRequests.setResponseDescription("Response is null or empty.");
            mpesaRequests.setRequestTime(LocalDateTime.now());
            mpesaRequestsRepository.save(mpesaRequests);
            return;
        }

        MpesaNotification mpesaNotification = new Gson().fromJson(stk_resp, MpesaNotification.class);
        if (mpesaNotification == null) {
            mpesaRequests.setResponseDescription("Null MPesa notification response");
            log.info("MPesa notification was null at {}", new Date());
            mpesaRequestsRepository.save(mpesaRequests);
            return;
        }
        mpesaRequests.setResponseDescription(mpesaNotification.getResponse().getResponseDescription());

        mpesaRequests.setResponseStatus(mpesaNotification.getHeader().getStatusCode());
        if (mpesaNotification.getHeader().getStatusCode().equals("0" )) {
            log.info("STK push request successful at {}", new Date());
            if (mpesaNotification.getResponse().getResponseCode().equals("0" )) {
                mpesaRequests.setResponseDescription("Success");
                mpesaRequests.setAccepted(true);
            }
        } else mpesaRequests.setResponseDescription(mpesaNotification.getHeader().getStatusDescription());

        mpesaRequestsRepository.save(mpesaRequests);
    }

    private STKPush getStkPush(MpesaRequests mpesaRequests) {
        STKPush stkPush = new STKPush();
        stkPush.setAmount(mpesaRequests.getAmount());
        stkPush.setCallbackUrl(configs.getMpesa_payment_notification_url()+"?referenceNumber="+ mpesaRequests.getReferenceNumber());
        stkPush.setInvoiceNumber(mpesaRequests.getIdNumber());
        stkPush.setOrgPassKey(configs.getMpesa_orgPassKey());
        stkPush.setPhoneNumber(mpesaRequests.getMobileNumber().substring(1));
        stkPush.setOrgShortCode(configs.getMpesa_orgShortCode());
        stkPush.setSharedShortCode(Boolean.parseBoolean(configs.getMpesa_sharedShortCode()));
        stkPush.setTransactionDescription(mpesaRequests.getDescription());
        return stkPush;
    }

    @Transactional
    public void createNotification(MpesaBody request, RequestLogModel requestLogModel,String referenceNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            log.info("Mpesa notification received at {} for request Id {}", LocalDateTime.now(), requestLogModel.getRequestId());
            stringBuilder.append("\n").append("Validation process");

            Optional<MpesaRequests> mpesaRequests = mpesaRequestsRepository.findByReferenceNumber(referenceNumber);
            if(mpesaRequests.isEmpty()) {
                log.info("Mpesa reference {} was not found at {}", referenceNumber, new Date());
                throw new Exception("Mpesa reference Number was not found");
            }

            double amount = Double.parseDouble(String.valueOf(request.getStkCallback().getCallbackMetadata().getItem().get(0).getValue()));
            String mpesaRef = String.valueOf(request.getStkCallback().getCallbackMetadata().getItem().get(1).getValue());

            mpesaRequests.get().setPaid(true);
            mpesaRequestsRepository.save(mpesaRequests.get());

            Optional<Subscriptions> subscriptions = subscriptionsRepository.findByPersonId(mpesaRequests.get().getIdNumber());

            if(subscriptions.isPresent()) {
                Optional<List<WalletTransactions>> walletTransactionsOptional =  walletTransactionRepository.findByReferenceNumber(referenceNumber);
                if(walletTransactionsOptional.isEmpty() || walletTransactionsOptional.get().isEmpty()) {
                    stringBuilder.append("\n").append("Creating a wallet transaction entry");
                    WalletTransactions walletTransactions = new WalletTransactions();
                    walletTransactions.setAmount(amount);
                    walletTransactions.setTransactionId(mpesaRef);
                    walletTransactions.setDateCreated(LocalDateTime.now());
                    walletTransactions.setTransactionType(WalletTransactionType.CREDIT.getType());
                    walletTransactions.setDescription("Deposit into the wallet via MPesa STK push.");
                    walletTransactions.setFundSource("MPESA");
                    walletTransactions.setFundDestination("NHIF");
                    walletTransactions.setLastUpdatedOn(LocalDateTime.now());
                    walletTransactions.setWalletBalance(0);
                    walletTransactions.setRequestId(mpesaRef);
                    walletTransactions.setStatus("Active");
                    walletTransactions.setReferenceNumber(referenceNumber);
                    walletTransactions.setWallet(subscriptions.get().getWallet());
                    walletTransactions = walletTransactionRepository.save(walletTransactions);
                    stringBuilder.append("\n").append("Wallet transaction entry created");

                    stringBuilder.append("\n").append(String.format("Subscriber for notification found, mobile %s", request.getStkCallback().getCallbackMetadata().getItem().get(4).getValue()));

                    if(subscriptions.get().getWallet() != null) {
                        log.info("updating wallet for the subscriber oof mobile {} and Id {} at {} for the request Id {}", subscriptions.get().getMobileNumber(), subscriptions.get().getPersonId(), new Date(), requestLogModel.getRequestId());
                        stringBuilder.append("\n").append("Subscriber found");

                        log.info("Updating wallet for request {} at {}", requestLogModel.getRequestId(), new Date());
                        stringBuilder.append("\n").append("Wallet updating...");
                        subscriptions.get().getWallet().setAmount(subscriptions.get().getWallet().getAmount() + amount);
                        stringBuilder.append("\n").append(String.format("Wallet for %s updated with %s", request.getStkCallback().getCallbackMetadata().getItem().get(4).getValue(), amount));
                        Wallet newWallet =  walletRepository.save(subscriptions.get().getWallet());
                        walletTransactions.setWalletBalance(newWallet.getAmount());
                    }
                    else {
                        log.info("Did not get wallet, creating one for the request Id {} at {}", requestLogModel.getRequestId(), new Date());
                        walletRepository.save(new Wallet(amount, LocalDateTime.now(), subscriptions.get(), List.of(walletTransactions)));
                    }
                } else {
                    stringBuilder.append("\n").append(String.format("Reference number %s has already been used, cannot be used again.", referenceNumber));
                    log.info("The provided reference number {} has already been used for the request {} at {}", referenceNumber, requestLogModel.getRequestId(), new Date());
                }
            } else {
                log.info("No subscriber was found matching mobile number {} for the notification with request Id {} at {}", request.getStkCallback().getCallbackMetadata().getItem().get(4).getValue(), requestLogModel.getRequestId(), new Date());
                stringBuilder.append("\n").append(String.format("No subscriber matching %s", request.getStkCallback().getCallbackMetadata().getItem().get(4).getValue()));
            }
        } catch (Exception exception) {
            log.error("MPesa notification exception at {} for request Id {} with message {}", new Date(), requestLogModel.getRequestId(), exception.getMessage());
            stringBuilder.append("\n").append(String.format("Exception %s", exception.getMessage()));
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log to db");
                requestLogModel.setDescription(requestLogModel.getDescription() + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
            } catch (Exception exception) {
                log.error("Exception while saving the db log at {} message {} for request {}", new Date(), exception.getMessage(), requestLogModel.getRequestId());
            }
        }
    }

    public Response<WalletBalance> getWalletBalance(Request<WalletBalanceRequest> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append("Request validation");
        try {
            Predicate<String> requestPredicate = String::isBlank;
            if(requestPredicate.test(request.getBody().getIdNumber())) {
                stringBuilder.append("\n").append("ID number missing.");
                throw new ExceptionManager("ID Number is missing",ResponseCodes.ID_PASSPORT.getCode());
            }

            if(requestPredicate.test(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Mobile number missing.");
                throw new ExceptionManager("Mobile Number is missing",ResponseCodes.MOBILE.getCode());
            }

            Optional<Subscriptions> subscriptions = subscriptionsRepository.findByPersonId(request.getBody().getIdNumber());
            if(subscriptions.isPresent()) {
                log.info("Subscriber found");
                stringBuilder.append("\n").append("Subscriber found.");
                Response<WalletBalance> response = new Response<>(new Header(true, ResponseCodes.SUCCESS.getCode(),"Balance returned"));
                double wallet = subscriptions.get().getWallet() == null ? 0 : subscriptions.get().getWallet().getAmount();
                response.setBody(new WalletBalance(wallet));
                return response;
            } else {
                stringBuilder.append("\n").append(String.format("No subscriber %s", request.getBody()));
                log.info("No subscriber was found with mobile {} at {}", request.getBody(), new Date());
                return new Response<>(new Header(String.format("Subscriber %s not found", request.getBody().getIdNumber()), ResponseCodes.FAIL.getCode()));
            }
        } catch (Exception exception) {
            stringBuilder.append("\n").append("Exception ").append(exception.getMessage());
            log.error("Exception while checking balance for {} at {}", request.getBody(), new Date());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                stringBuilder.append("Saving request log to db");
                requestLogModel.setDescription(requestLogModel.getDescription() + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
            } catch (Exception exception) {
                log.error("Exception while saving the request log trail to db at {} for the request {}", new Date(), requestLogModel.getRequestId());
            }
        }
    }
}
