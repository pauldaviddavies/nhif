package com.sebin.uhc.services.onboarding;

import com.sebin.uhc.entities.onboarding.RequestLogTrail;
import com.sebin.uhc.models.RequestLogModel;
import com.sebin.uhc.repositories.onboarding.RequestLogTrailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j(topic = ":: SERVICE :: LOG :::")
@Service
public class RequestLogTrailService {
    @Autowired
    private RequestLogTrailRepository repository;

    public String testLog() {
        try {
            log.info("Creating log trail object for saving at {}", new Date());
            RequestLogTrail requestLogTrail = new RequestLogTrail();

            log.info("Object created, saving log trail to database at {}", new Date());

            repository.save(requestLogTrail);

            log.info("Request log trail saved to database");

            return "Test log saved successfully";

        } catch (Exception exception) {
            log.error("Request log trail could not be saved at {}. Exception message is {}", new Date(), exception.getMessage());

            return "Exception: " + exception.getMessage();
        }
    }
    public void saveLog(RequestLogModel logModel) {
        try {
            log.info("Creating log trail object for saving at {}", new Date());

            RequestLogTrail requestLogTrail = new RequestLogTrail();
            requestLogTrail.setDateRequested(logModel.getDateRequested());
            requestLogTrail.setRequestingChannel(logModel.getRequestingChannel());
            requestLogTrail.setDescription(logModel.getDescription());
            requestLogTrail.setCallbackTime(logModel.getCallbackTime());
            requestLogTrail.setRequestingIP(logModel.getRequestingIP());
            requestLogTrail.setCallBackId(logModel.getCallBackId());
            requestLogTrail.setRequestingUrl(logModel.getRequestingUrl());
            requestLogTrail.setRequestingPort(logModel.getRequestingPort());
            requestLogTrail.setRequestId(logModel.getRequestId());
            requestLogTrail.setRequestType(logModel.getRequestType());
            requestLogTrail.setCallbackMessage(logModel.getCallbackMessage());
            requestLogTrail.setHasCallBack(logModel.getHasCallBack());
            requestLogTrail.setLastUpdated(LocalDateTime.now());
            requestLogTrail.setSyncAsync(logModel.getSyncAsync());
            requestLogTrail.setResponseMessage(logModel.getResponseMessage());

            log.info("Object created, saving log trail to database at {}", new Date());

            repository.save(requestLogTrail);
            log.info("Request log saved.");

        } catch (Exception exception) {
            log.error("Request log trail could not be saved at {}. Exception message is {}", new Date(), exception.getMessage());
        }
    }
}
