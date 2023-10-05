package com.sebin.uhc.controllers.reports;

import com.sebin.uhc.commons.EndPoints;
import com.sebin.uhc.commons.Helper;
import com.sebin.uhc.models.reports.Statement;
import com.sebin.uhc.models.reports.StatementRequest;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import com.sebin.uhc.services.reports.ReportingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = EndPoints.REPORTS)
public class ReportingController {
    @Autowired
    private ReportingService service;
    @Autowired
    private RequestLogTrailService requestLogTrailService;


    @PostMapping(value = EndPoints.STATEMENT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<List<Statement>> miniStatement(@RequestBody Request<StatementRequest> request, HttpServletRequest servletRequest) {
        return service.statement(request, Helper.validateRequestHeader("Statement request", request, servletRequest, requestLogTrailService));
    }
}
