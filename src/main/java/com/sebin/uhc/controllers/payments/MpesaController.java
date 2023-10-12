package com.sebin.uhc.controllers.payments;

import com.google.gson.Gson;
import com.sebin.uhc.commons.EndPoints;
import com.sebin.uhc.commons.Helper;
import com.sebin.uhc.models.mpesa.WalletBalance;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.requests.onboarding.RequestHeader;
import com.sebin.uhc.models.requests.payments.Mpesa;
import com.sebin.uhc.models.requests.payments.WalletBalanceRequest;
import com.sebin.uhc.models.responses.notifications.MpesaResponse;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import com.sebin.uhc.services.payments.MpesaService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.annotation.HandlesTypes;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j(topic = ":: CONTROLLER :: BENEFICIARY :::")
@RestController
@RequestMapping(value = EndPoints.PAYMENTS)
public class MpesaController {
    @Autowired
    private MpesaService service;
    @Autowired
    private RequestLogTrailService requestLogTrailService;

    @PostMapping(value = EndPoints.MPESA_PAYMENT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> create(@RequestBody Request<Mpesa> request, HttpServletRequest servletRequest) {
        return service.processMpesaRequest(request, Helper.validateRequestHeader("Mpesa payment request", request, servletRequest, requestLogTrailService));
    }

    @Hidden
    @PostMapping(value = EndPoints.MPESA_PAYMENT_NOTIFICATION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void createNotification(HttpServletRequest servletRequest) {
        try {
        log.info("MPesa payment notification at {}", new Date());
        String notification_data = servletRequest.getReader().lines().collect(Collectors.joining());
        System.out.println("notification_data_"+notification_data);
        MpesaResponse mPesaResponse = new Gson().fromJson(notification_data, MpesaResponse.class);
        Request<MpesaResponse.RequestPayload> req = new Request<>();
        req.setBody(mPesaResponse.getRequestPayload());
        req.setHeader(new RequestHeader());
        req.getHeader().setRequestId(mPesaResponse.getRequestPayload().getAdditionalData().getNotificationData().getBusinessKey().replace("#","-"));

        String referenceNumber=(mPesaResponse.getRequestPayload().getAdditionalData().getNotificationData().getBusinessKey()).replace("#","-");
        service.createNotification(mPesaResponse, Helper.validateRequestHeader("MPesa payment callback", req, servletRequest, requestLogTrailService),referenceNumber);
        }catch (Exception ex) {
            log.error("Exception while creating MPesa notification at {}", new Date());
        }
    }


    @PostMapping(value = EndPoints.WALLET_BALANCE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<WalletBalance> getWalletBalance(@RequestBody Request<WalletBalanceRequest> request, HttpServletRequest servletRequest) {
        return service.getWalletBalance(request, Helper.validateRequestHeader("Wallet balance request", request, servletRequest, requestLogTrailService));
    }
}
