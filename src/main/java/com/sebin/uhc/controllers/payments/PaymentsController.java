package com.sebin.uhc.controllers.payments;

import com.sebin.uhc.commons.EndPoints;
import com.sebin.uhc.commons.Helper;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.requests.payments.FundsTransferRequest;
import com.sebin.uhc.models.requests.payments.Mpesa;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import com.sebin.uhc.services.payments.PaymentsService;
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

@Slf4j(topic = ":: CONTROLLER :: BENEFICIARY :::")
@RestController
@RequestMapping(value = EndPoints.PAYMENTS)
public class PaymentsController {

    @Autowired
    private PaymentsService service;
    @Autowired
    private RequestLogTrailService requestLogTrailService;

    @PostMapping(value = EndPoints.BANK_WALLET_NHIF_PAYMENT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> create(@RequestBody Request<FundsTransferRequest> request, HttpServletRequest servletRequest) {
        return service.processTransferRequest(request, Helper.validateRequestHeader("Wallet to NHIF payment request", request, servletRequest, requestLogTrailService));
    }

    @Hidden
    @PostMapping(value = EndPoints.KCB_FUNDS_TRANSFER_NOTIFICATION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void createNotification(HttpServletRequest servletRequest) {
        log.info("KCB Bank funds transfer notification request at {}", new Date());
        try {
            BufferedReader stream = servletRequest.getReader();
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while((line = stream.readLine()) != null)
                stringBuilder.append(line);

        } catch (Exception exception) {

        }
    }
}
