package com.sebin.uhc.tasks;

import com.google.gson.Gson;
import com.sebin.uhc.commons.*;
import com.sebin.uhc.entities.onboarding.Beneficiaries;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.entities.payments.FundsTransferRequests;
import com.sebin.uhc.models.TokenTypes;
import com.sebin.uhc.models.requests.onboarding.Vooma;
import com.sebin.uhc.models.requests.payments.FundTransfer;
import com.sebin.uhc.models.requests.payments.FundsTransferRequest;
import com.sebin.uhc.models.responses.onboarding.Header;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.models.responses.onboarding.VoomaResp;
import com.sebin.uhc.models.responses.payments.FundsTransferResponse;
import com.sebin.uhc.repositories.onboarding.BeneficiaryRepository;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepository;
import com.sebin.uhc.repositories.payments.FundsTransferRequestsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
@Slf4j(topic = ":: FUNDS-TRANSFER :: TASK :::")
public class SendFundsTransfer
{
        @Autowired
        private FundsTransferRequestsRepository fundsTransferRequestsRepository;

        @Autowired
        private BeneficiaryRepository beneficiaryRepository;

        @Autowired
        private Configs configs;

        @Autowired
        private AccessTokenLoader accessTokenLoader;

        @Autowired
        private SubscriptionsRepository subscriptionsRepository;


private void submitFundsTransfers() {
        List<FundsTransferRequests> fundsTransferRequests =   fundsTransferRequestsRepository.findBySentToKcb(false);
        for(FundsTransferRequests transferRequests : fundsTransferRequests)
        {
                try {
                        Optional<Beneficiaries> beneficiary =  beneficiaryRepository.findByPersonIdAndStatus(transferRequests.getBeneficiaryIdOrPassportNumber(),Statuses.ACTIVE.getStatus());
                        if(beneficiary.isEmpty())
                                continue;

                        FundTransfer fundTransfer = new FundTransfer();
                        fundTransfer.setRequestId(transferRequests.getReferenceNumber());
                        fundTransfer.setAmount(transferRequests.getAmount()+"");
                        fundTransfer.setNhifNo(transferRequests.getBeneficiaryMemberNumber());

                        String accessToken = new GetToken().getSecret(configs.getApis_consumer_key(),configs.getApis_consumer_secret());

                        log.info("Submitting FT at {}", LocalDateTime.now());
                        transferRequests.setDateSentToCB(LocalDateTime.now());
                        String request = new Gson().toJson(fundTransfer);
                        transferRequests.setRequest(request);
                        String ft_resp = General.send_request(transferRequests.getReferenceNumber(), configs.getFT_request_url(), request, AppConstants.APPLICATION_JSON, accessToken);
                        transferRequests.setResponseDate(LocalDateTime.now());
                        transferRequests.setResponse(ft_resp);
                        log.info("FT response {} at {}", ft_resp, new Date());

                        if (ft_resp.isBlank()) {
                                log.info("Could not get FT response at {}", new Date());
                                transferRequests.setSendToKcbRetries(transferRequests.getSendToKcbRetries()+1);
                                fundsTransferRequestsRepository.save(transferRequests);
                                continue;
                        }

                        FundsTransferResponse fundsTransferResponse = new Gson().fromJson(ft_resp, FundsTransferResponse.class);
                        if (fundsTransferResponse == null) {
                                log.info("Could not get FT response at {}", new Date());
                                transferRequests.setSendToKcbRetries(transferRequests.getSendToKcbRetries()+1);
                                fundsTransferRequestsRepository.save(transferRequests);
                                continue;
                        }

                        transferRequests.setResponseCode(fundsTransferResponse.getStatusCode());
                        transferRequests.setStatusMessage(fundsTransferResponse.getStatusMessage());
                        transferRequests.setStatusDescription(fundsTransferResponse.getStatusDescription());
                        transferRequests.setProcessed(true);
                        transferRequests.setSentToKcb(true);

                        if (fundsTransferResponse.getStatusCode().equals("0")) {
                                transferRequests.setProcessingStatus(Statuses.SUCCESS.getStatus());
                                transferRequests.setKcbFTTransactionID(fundsTransferResponse.getResponsePayload().ftTransactionID);
                                Optional<Subscriptions> person = subscriptionsRepository.findByPersonId(beneficiary.get().getSubscriptions().getPersonId());
                                if(person.isPresent()) {
                                        person.get().getWallet().setAmount(person.get().getWallet().getAmount() + transferRequests.getAmount());
                                        log.info("Wallet updated for {} at {}", beneficiary.get().getPersonId(), new Date());
                                } else {
                                        log.info("Subscription not found yet the request went to bank at {} for {}", new Date(), beneficiary.get().getPersonId());
                                        transferRequests.setErrorsDescription("Subscription was not found upon response.");
                                }
                        } else {
                                transferRequests.setProcessingStatus(Statuses.FAIL.getStatus());
                                StringBuilder error = new StringBuilder();
                                fundsTransferResponse.errors.forEach(s -> error.append("\n").append(s));
                                transferRequests.setErrorsDescription(error.toString());
                        }
                        fundsTransferRequestsRepository.save(transferRequests);
                }
                catch (Exception ex) {
                        log.error("Exception while doing funds transfer at {} for request Id {}", new Date(), transferRequests.getReferenceNumber());
                        transferRequests.setSendToKcbRetries(transferRequests.getSendToKcbRetries()+1);
                        fundsTransferRequestsRepository.save(transferRequests);
                        System.out.println("Error sending funds transfer record "+transferRequests.getId());
                        ex.printStackTrace();
                }
        }

}

@Scheduled(fixedDelay = (10000)) //10 s
public void scheduleFixedDelayTask() {
        log.info("Funds transfer scheduler activated at {}", new Date());
        submitFundsTransfers();
        log.info("Funds transfer scheduler closed at {}", new Date());
}

}
