package com.sebin.uhc.controllers.onboarding;

import com.sebin.uhc.commons.EndPoints;
import com.sebin.uhc.commons.Helper;
import com.sebin.uhc.models.Subscription;
import com.sebin.uhc.models.requests.onboarding.ChangePin;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.requests.onboarding.SetPin;
import com.sebin.uhc.models.requests.onboarding.ValidatePin;
import com.sebin.uhc.models.requests.payments.WalletBalanceRequest;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.models.responses.onboarding.SubscriptionInquiry;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import com.sebin.uhc.services.onboarding.SubscriptionsService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping(EndPoints.SUBSCRIPTION)
public class SubscriptionsController {
    @Autowired
    private SubscriptionsService service;
    @Autowired
    private RequestLogTrailService requestLogTrailService;

    @PostMapping(value = EndPoints.INQUIRE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> inquiry(HttpServletRequest servletRequest, @RequestBody Request<WalletBalanceRequest> request) {
        return service.subscriptionInquiry(request, Helper.validateRequestHeader("Subscription inquiry request", request, servletRequest, requestLogTrailService));
    }
    @PostMapping(value = EndPoints.CREATE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> subscribe(HttpServletRequest servletRequest, @RequestBody Request<Subscription> request) {
        return service.create(request, Helper.validateRequestHeader("Subscription request", request, servletRequest, requestLogTrailService));
    }

    @PostMapping(value = EndPoints.INQUIRE_MOBILE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<SubscriptionInquiry> inquireMobile(HttpServletRequest servletRequest, @RequestBody Request<String> request) {
        return service.inquireMobile(request, Helper.validateRequestHeader("Inquiry request by mobile",request, servletRequest, requestLogTrailService));
    }

    @PostMapping(value = EndPoints.INQUIRE_ID, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<SubscriptionInquiry> inquireId(HttpServletRequest servletRequest, @RequestBody Request<String> request) {
        return service.inquireId(request, Helper.validateRequestHeader("Subscription request", request, servletRequest, requestLogTrailService));
    }

    @PostMapping(value = EndPoints.UNSUBSCRIBE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> unsubscribe(HttpServletRequest servletRequest, @RequestBody Request<String> request) {
        return service.unsubscribe(request, Helper.validateRequestHeader("Unsubscribe request", request, servletRequest, requestLogTrailService));
    }

    @PostMapping(value = EndPoints.SETPIN, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> setPin(HttpServletRequest httpServletRequest, @RequestBody Request<SetPin> request) {
        return service.setPin(request, Helper.validateRequestHeader("Pin-set request", request, httpServletRequest, requestLogTrailService));
    }

    @PostMapping(value = EndPoints.VALIDATEPIN, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> validatePin(HttpServletRequest servletRequest, @RequestBody Request<ValidatePin> request) {
        return service.validatePin(request, Helper.validateRequestHeader("Pin validation request", request, servletRequest, requestLogTrailService));
    }

    @PostMapping(value = EndPoints.CHANGEPIN, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> changePin(HttpServletRequest servletRequest, @RequestBody Request<ChangePin> request) {
        return service.changePin(request, Helper.validateRequestHeader("Pin change request", request, servletRequest, requestLogTrailService));
    }

    @Hidden
    @PostMapping(value = EndPoints.LOAD_TO_VOOMA_NOTIFICATION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void createNotification(HttpServletRequest servletRequest) {
        log.info("Vooma subscription callback at {}", new Date());
        try {
            BufferedReader stream = servletRequest.getReader();
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = stream.readLine()) != null)
                stringBuilder.append(line);
        } catch (Exception exception) {
            log.error("Vooma notification exception {}", exception.getMessage());
        }
    }
}
