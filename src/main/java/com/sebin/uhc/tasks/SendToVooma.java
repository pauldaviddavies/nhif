package com.sebin.uhc.tasks;

import com.google.gson.Gson;
import com.sebin.uhc.commons.AccessTokenLoader;
import com.sebin.uhc.commons.AppConstants;
import com.sebin.uhc.commons.Configs;
import com.sebin.uhc.commons.General;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.models.requests.onboarding.Vooma;
import com.sebin.uhc.models.responses.onboarding.VoomaResp;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepository;
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
public class SendToVooma {
        @Autowired
        private SubscriptionsRepository subscriptionsRepository;
        @Autowired
        private Configs configs;
        @Autowired
        private AccessTokenLoader accessTokenLoader;
        private void submitNewMembers() {
                log.info("Scheduler activated at {}", new Date());
                List<Subscriptions> subscriptionsRecords =   subscriptionsRepository.findBySentToKcb(false);
                log.info("Found {} records to send to vooma", subscriptionsRecords.size());
                for(Subscriptions subscriptions : subscriptionsRecords) {
                        try {
                                Vooma vooma = new Vooma();
                                vooma.setMsisdn(subscriptions.getMobileNumber().substring(1));
                                vooma.setFirstName(subscriptions.getFirstName());
                                vooma.setMiddleName(subscriptions.getMiddleName());
                                vooma.setLastName(subscriptions.getSurname());
                                if(subscriptions.getDateOfBirth() != null) {
                                        String dateOfBirth = subscriptions.getDateOfBirth().replace("-","");
                                        if(dateOfBirth.length() >= 8)
                                                vooma.setDateOfBirth(dateOfBirth.substring(0,8));
                                } else continue;

                                vooma.setIdNumber(subscriptions.getPersonId());
                                Vooma.LoadInitiator loadInitiator = new Vooma.LoadInitiator();
                                loadInitiator.setIdentifier(subscriptions.getMobileNumber().substring(1));
                                vooma.setInitiator(loadInitiator);
                                vooma.setMessageId(subscriptions.getKcbMessageId());
                                vooma.setExternalId(subscriptions.getKcbExternalId());
                                vooma.setCallBackURL(configs.getLoad_to_vooma_notification_url());

                                String accessToken = new GetToken().getSecret(configs.getApis_consumer_key(),configs.getApis_consumer_secret());
                                String request = new Gson().toJson(vooma);
                                log.info("Sending request {} to vooma at {}", request , new Date());
                                String vooma_resp = General.send_request(vooma.getExternalId(),  configs.getVooma_opt_in_url(), request, AppConstants.APPLICATION_JSON, accessToken);
                                log.info("Vooma response {} at {}", vooma_resp, new Date());

                                if (vooma_resp.isEmpty()) {
                                        log.info("Vooma response was null or empty at {}", new Date());
                                        subscriptions.setSendToKcbRetries(subscriptions.getSendToKcbRetries()+1);
                                        subscriptionsRepository.save(subscriptions);
                                        continue;
                                }

                                VoomaResp voomaResp = new Gson().fromJson(vooma_resp, VoomaResp.class);
                                if (voomaResp == null) {
                                        subscriptions.setSendToKcbRetries(subscriptions.getSendToKcbRetries()+1);
                                        subscriptionsRepository.save(subscriptions);
                                        log.info("Null or empty vooma response at {} for {}", new Date(), vooma.getExternalId());
                                        continue;
                                }

                                if (voomaResp.getResponseCode().equals("SFUN001")) {
                                        subscriptions.setSentToKcb(true);
                                        subscriptions.setBankNotificationDate(LocalDateTime.now());
                                        subscriptionsRepository.save(subscriptions);
                                }
                        }
                        catch (Exception ex) {
                                subscriptions.setSendToKcbRetries(subscriptions.getSendToKcbRetries()+1);
                                subscriptionsRepository.save(subscriptions);
                                log.error("Exception sending details to vooma at {} with message {}", new Date(), ex.getMessage());
                                ex.printStackTrace();
                        }
                };

        }

@Scheduled(fixedDelay = (60000L)) //1 minute
public void scheduleFixedDelayTask() {
        submitNewMembers();
}

}
