package com.sebin.uhc.tasks;

import com.google.gson.Gson;
import com.sebin.uhc.commons.*;
import com.sebin.uhc.entities.onboarding.Beneficiaries;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.entities.payments.FundsTransferRequests;
import com.sebin.uhc.models.PaymentPurpose;
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
        List<FundsTransferRequests> fundsTransferRequests =   fundsTransferRequestsRepository.findByProcessed(false);
        log.info("Found {} records for funds transfer", fundsTransferRequests.size());
        for(FundsTransferRequests transferRequests : fundsTransferRequests)
        {
                try {
                        Optional<Beneficiaries> beneficiary =  beneficiaryRepository.findByPersonIdAndStatus(transferRequests.getBeneficiaryIdOrPassportNumber(),Statuses.ACTIVE.getStatus());
                        if(beneficiary.isEmpty())
                                continue;

                        Optional<Subscriptions> subscriptions =  subscriptionsRepository.findByPersonId(transferRequests.getIdNumber());
                        if(subscriptions.isEmpty())
                                continue;

                        if(subscriptions.get().getWallet().getAmount() > 0) {
                                if(transferRequests.getAmount() > subscriptions.get().getWallet().getAmount()) {
                                        log.info("Amount({}) requested for {} at {} more than the wallet balance({}) for {}",
                                                transferRequests.getAmount(), beneficiary.get().getPersonId(), new Date(), subscriptions.get().getWallet().getAmount(), subscriptions.get().getPersonId());
                                } else {
                                        FundTransfer fundTransfer = new FundTransfer();
                                        fundTransfer.setRequestId(transferRequests.getReferenceNumber());
                                        fundTransfer.setAmount(transferRequests.getAmount()+"");
                                        fundTransfer.setNhifNo((transferRequests.getPurpose().equalsIgnoreCase(PaymentPurpose.CONTRIBUTION.name())) ? "M"+transferRequests.getBeneficiaryMemberNumber() : "P"+transferRequests.getBeneficiaryMemberNumber());
                                        fundTransfer.setPhoneNumber(transferRequests.getMobileNumber().substring(1));

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
                                                transferRequests.setPaid(true);
                                                transferRequests.setKcbFTTransactionID(fundsTransferResponse.getResponsePayload().ftTransactionID);

                                                subscriptions.get().getWallet().setAmount(subscriptions.get().getWallet().getAmount() - transferRequests.getAmount());
                                                log.info("Wallet updated for {} at {}", beneficiary.get().getPersonId(), new Date());
                                                subscriptionsRepository.save(subscriptions.get());
                                        } else {
                                                StringBuilder error = new StringBuilder();
                                                fundsTransferResponse.errors.forEach(s -> error.append("\n").append(s));
                                                transferRequests.setErrorsDescription(error.toString());

                                        }
                                        fundsTransferRequestsRepository.save(transferRequests);
                                }
                        } else {
                                log.info("Wallet balance not enough to initiate the payment request for beneficiaries at {}", new Date());
                        }
                }
                catch (Exception ex) {
                        log.error("Exception while doing funds transfer at {} for request Id {}", new Date(), transferRequests.getReferenceNumber());
                        transferRequests.setSendToKcbRetries(transferRequests.getSendToKcbRetries()+1);
                        fundsTransferRequestsRepository.save(transferRequests);
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
