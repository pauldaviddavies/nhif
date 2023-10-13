package com.sebin.uhc.tasks;

import com.google.gson.Gson;
import com.sebin.uhc.commons.AccessTokenLoader;
import com.sebin.uhc.commons.AppConstants;
import com.sebin.uhc.commons.Configs;
import com.sebin.uhc.commons.General;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.entities.onboarding.UnSubscriptionsRequests;
import com.sebin.uhc.models.requests.onboarding.Vooma;
import com.sebin.uhc.models.requests.onboarding.VoomaOptOut;
import com.sebin.uhc.models.responses.onboarding.VoomaResp;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepository;
import com.sebin.uhc.repositories.onboarding.UnsubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@EnableScheduling
@Slf4j(topic = ":: VOOMA-UPDATE ::: TASK ::")
public class SendUnsubscriptionToVooma {
        @Autowired
        private UnsubscriptionRepository unsubscriptionRepository;
        @Autowired
        private Configs configs;
        @Autowired
        private AccessTokenLoader accessTokenLoader;
        private void submitOptOutMembers() {
                //log.info("Scheduler activated at {}", new Date());
                List<UnSubscriptionsRequests> unSubscriptionsRequests =   unsubscriptionRepository.findByProcessed(false);
                //log.info("Found {} records to send to vooma", subscriptionsRecords.size());
                for(UnSubscriptionsRequests unsubscriptions : unSubscriptionsRequests) {
                        try {
                                VoomaOptOut vooma = new VoomaOptOut();
                                vooma.setMessageId(unsubscriptions.getRequestId());
                                vooma.setMsisdn(unsubscriptions.getMobileNumber().substring(1));
                                vooma.setExternalId(unsubscriptions.getKcbExternalId());
                                vooma.setIdNumber(unsubscriptions.getPersonId());

                                String accessToken = new GetToken().getSecret(configs.getApis_consumer_key(),configs.getApis_consumer_secret());
                                String request = new Gson().toJson(vooma);
                                log.info("Sending opt out request {} to vooma at {}", request , new Date());
                                String vooma_resp = General.send_request(vooma.getExternalId(),  configs.getKcb_opt_out_url(), request, AppConstants.APPLICATION_JSON, accessToken);
                                log.info("Vooma opt out response {} at {}", vooma_resp, new Date());

                                if (vooma_resp.isEmpty()) {
                                        log.info("Vooma opt out response was null or empty at {}", new Date());
                                        unsubscriptions.setSendToKcbRetries(unsubscriptions.getSendToKcbRetries()+1);
                                        unsubscriptionRepository.save(unsubscriptions);
                                        continue;
                                }

                                VoomaResp voomaResp = new Gson().fromJson(vooma_resp, VoomaResp.class);
                                if (voomaResp == null) {
                                        unsubscriptions.setSendToKcbRetries(unsubscriptions.getSendToKcbRetries()+1);
                                        unsubscriptionRepository.save(unsubscriptions);
                                        log.info("Null or empty vooma opt out response at {} for {}", new Date(), vooma.getExternalId());
                                        continue;
                                }

                                if (voomaResp.getStatusCode().equals("0")) {
                                        unsubscriptions.setSentToKcb(true);
                                        unsubscriptions.setProcessed(true);
                                        unsubscriptions.setKcbResponse(voomaResp.getStatusMessage() +" - "+voomaResp.getStatusDescription());
                                        unsubscriptions.setBankNotificationDate(LocalDateTime.now());
                                        unsubscriptionRepository.save(unsubscriptions);
                                }
                                if (voomaResp.getStatusCode().equals("01")) {
                                        unsubscriptions.setProcessed(true);
                                        unsubscriptions.setKcbResponse(voomaResp.getStatusMessage() +" - "+voomaResp.getStatusDescription());
                                        unsubscriptions.setBankNotificationDate(LocalDateTime.now());
                                        unsubscriptionRepository.save(unsubscriptions);
                                }
                        }
                        catch (Exception ex) {
                                unsubscriptions.setSendToKcbRetries(unsubscriptions.getSendToKcbRetries()+1);
                                unsubscriptionRepository.save(unsubscriptions);
                                log.error("Exception sending details to vooma at {} with message {}", new Date(), ex.getMessage());
                                ex.printStackTrace();
                        }
                };

        }

@Scheduled(fixedDelay = 2000) //2s
public void scheduleFixedDelayTask() {
        submitOptOutMembers();
}

}
