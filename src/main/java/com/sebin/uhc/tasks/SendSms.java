package com.sebin.uhc.tasks;

import com.google.gson.Gson;
import com.sebin.uhc.commons.AccessTokenLoader;
import com.sebin.uhc.commons.AppConstants;
import com.sebin.uhc.commons.Configs;
import com.sebin.uhc.commons.General;
import com.sebin.uhc.entities.notifications.Sms;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.models.requests.notifications.SmsRequest;
import com.sebin.uhc.models.requests.onboarding.Vooma;
import com.sebin.uhc.models.responses.notifications.SmsResponse;
import com.sebin.uhc.models.responses.onboarding.VoomaResp;
import com.sebin.uhc.repositories.SmsRepository;
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
public class SendSms {
        @Autowired
        private SmsRepository smsRepository;
        @Autowired
        private Configs configs;

        private void sendSms() {
                //log.info("Scheduler activated at {}", new Date());
                List<Sms> smsList =   smsRepository.findByProcessedAndSendretriesLessThan(false, 2);
                //log.info("Found {} records to send to Tilil sms", smsList.size());
                for(Sms sms : smsList) {
                        try {
                                SmsRequest smsRequest = new SmsRequest();
                                smsRequest.setApi_key(configs.getTilil_sms_api_key());
                                smsRequest.setMessage(sms.getMessage());
                                smsRequest.setMobile(sms.getMobileNumber().substring(1));

                                String request = new Gson().toJson(smsRequest);
                                log.info("Sending sms id {} to tilil at {}", sms.getId() , new Date());
                                String sms_resp = General.send_request(sms.getReferenceNumber(),  configs.getTilil_sms_url(), request, AppConstants.APPLICATION_JSON, null);
                                log.info("Tilil sms response {} at {}", sms_resp, new Date());

                                if (sms_resp.isEmpty()) {
                                        log.info("Tilil response was null or empty at {}", new Date());
                                        sms.setSendretries(sms.getSendretries()+1);
                                        smsRepository.save(sms);
                                        continue;
                                }

                                SmsResponse smsResponse = new Gson().fromJson(sms_resp, SmsResponse.class);
                                if (smsResponse == null) {
                                        sms.setSendretries(sms.getSendretries()+1);
                                        smsRepository.save(sms);
                                        log.info("Null or empty Tilil response at {} for {}", new Date(), sms.getReferenceNumber());
                                        continue;
                                }

                                if (smsResponse.getStatus_code() == 1000) {
                                        sms.setProcessed(true);
                                        sms.setSent(true);
                                        sms.setTililResponse(smsResponse.getStatus_code() +" - "+ smsResponse.getStatus_desc());
                                        sms.setDateSent(LocalDateTime.now());
                                        sms.setTilil_message_id(smsResponse.getMessage_id());
                                        smsRepository.save(sms);
                                }
                                if (smsResponse.getStatus_code() == 1001) {
                                        sms.setProcessed(true);
                                        sms.setTililResponse(smsResponse.getStatus_code() +" - "+ smsResponse.getStatus_desc());
                                        sms.setDateSent(LocalDateTime.now());
                                        sms.setTilil_message_id(smsResponse.getMessage_id());
                                        smsRepository.save(sms);
                                }
                        }
                        catch (Exception ex) {
                                sms.setSendretries(sms.getSendretries()+1);
                                smsRepository.save(sms);
                                log.error("Exception sending sms to {} at {}",sms.getMobileNumber(), new Date());
                                ex.printStackTrace();
                        }
                };

        }

@Scheduled(fixedDelay = 2000) //2s
public void scheduleFixedDelayTask() {
        sendSms();
}

}
