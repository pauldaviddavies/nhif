package com.sebin.uhc.exceptions;

import com.sebin.uhc.commons.ResponseCodes;
import com.sebin.uhc.models.responses.onboarding.Header;
import com.sebin.uhc.models.responses.onboarding.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(":: EXCEPTION :: HANDLER :::");

    @ExceptionHandler(value = Exception.class)
    public Response<?> exception(Exception exception) {
        LOGGER.error(exception.getMessage());
        String message;
        String code;
        String exceptionMessage = exception.getMessage();
        if(exceptionMessage != null && exceptionMessage.contains("::")) {
            int index = exceptionMessage.indexOf("::");
            Map<String, String> response = returnCodeAndMessageFrom(exceptionMessage, index);
            message = response.get("message");
            code = response.get("code");
        } else {
            code = ResponseCodes.GENERAL_FAILURE.getCode();
            message = "Sorry, something went wrong.";
        }

        return new Response<>(new Header(message, code));
    }

    @ExceptionHandler(value = ExceptionManager.class)
    public Response<?> exception(ExceptionManager exception) {
        LOGGER.error(exception.getMessage());
        String message;
        String code;
        String exceptionMessage = exception.getMessage();
        if(exceptionMessage != null && exceptionMessage.contains("::")) {
            int index = exceptionMessage.indexOf("::");
            Map<String, String> response = returnCodeAndMessageFrom(exceptionMessage, index);
            message = response.get("message");
            code = response.get("code");
        } else {
            code = ResponseCodes.GENERAL_FAILURE.getCode();
            message = "Sorry, something went wrong.";
        }

        return new Response<>(new Header(message, code));
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public Response<?> exception(ConstraintViolationException exception) {
        LOGGER.error(exception.getMessage());
        return new Response<>(new Header("Sorry, we can not complete the request at this time", ResponseCodes.UNACCEPTABLE_REQUEST_FORMAT.getCode()));
    }

    private Map<String, String> returnCodeAndMessageFrom(String message, int index) {
        Map<String, String> res = new HashMap<>();
        res.put("message", message.substring(0, index));
        res.put("code", message.substring(index + 2));

        return res;
    }

}
