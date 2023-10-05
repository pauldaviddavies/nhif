package com.sebin.uhc.controllers.onboarding;

import com.sebin.uhc.commons.EndPoints;
import com.sebin.uhc.commons.Helper;
import com.sebin.uhc.models.Beneficiary;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.requests.onboarding.SponsorBeneficiary;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.services.onboarding.BeneficiariesService;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j(topic = ":: CONTROLLER :: BENEFICIARY :::")
@RestController
@RequestMapping(value = EndPoints.BENEFICIARIES)
public class BeneficiariesController {
    @Autowired
    private BeneficiariesService service;
    @Autowired
    private RequestLogTrailService requestLogTrailService;

    @PostMapping(value = EndPoints.ADD_BENEFICIARY, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<?> create(@RequestBody Request<Beneficiary> request, HttpServletRequest servletRequest) {
        return service.addBeneficiary(request, Helper.validateRequestHeader("Beneficiary addition request", request, servletRequest, requestLogTrailService));
    }

    @PostMapping(value = EndPoints.LIST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<List<Beneficiary>> getBeneficiaries(@RequestBody Request<String> request, HttpServletRequest servletRequest) {
        return service.getBeneficiaries(request, Helper.validateRequestHeader("Beneficiary listing request", request, servletRequest, requestLogTrailService));
    }

    @PostMapping(value = EndPoints.REMOVE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<List<Beneficiary>> removeBeneficiary(@RequestBody Request<SponsorBeneficiary> request, HttpServletRequest servletRequest) {
        return service.removeBeneficiary(request, Helper.validateRequestHeader("Beneficiary removal request", request, servletRequest, requestLogTrailService));
    }
}
