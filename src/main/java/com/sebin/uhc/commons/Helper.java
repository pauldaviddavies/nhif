package com.sebin.uhc.commons;

import com.sebin.uhc.exceptions.ExceptionManager;
import com.sebin.uhc.models.RequestLogModel;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.services.onboarding.RequestLogTrailService;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j(topic = ":: HELPER :::")
public class Helper {
    public static DateFormat refFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static RequestLogModel validateRequestHeader(String requestType,  Request<?> request, HttpServletRequest servletRequest, RequestLogTrailService requestLogTrailService) {
        log.info(requestType + " at {} ", new Date());
        log.info("Request header validation started at {}", new Date());

        RequestLogModel requestLogModel = new RequestLogModel();
        requestLogModel.setRequest(requestType);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(new Date()).append("\n").append(requestType);
        try {
            stringBuilder.append("\n").append("Header validation started.");
            Predicate<Request<?>> requestPredicate = r -> r == null || r.getHeader() == null;
            Predicate<String> stringPredicate = s -> s == null || s.isBlank();
            if(requestPredicate.test(request)) {
                log.info("Request or request header null at {}", new Date());
                stringBuilder.append("\n").append("Request is null or did not have header.");
                throw new ExceptionManager("Null request or header, cannot proceed.", ResponseCodes.NULL_REQUEST.getCode());
            }
            if(stringPredicate.test(request.getHeader().getRequestId())) {
                log.info("Null or empty request Id at {}", new Date());
                stringBuilder.append("\n").append("Request Id not found!");
                throw new ExceptionManager("Null or empty request Id, cannot proceed.", ResponseCodes.NULL_REQUEST_ID.getCode());
            }

            stringBuilder.append("\n").append("Request header validation successful");
            log.info("Request header validation completed at {}", new Date());

            requestLogModel.setRequestingIP(servletRequest.getRemoteAddr());
            requestLogModel.setDateRequested(LocalDateTime.now());
            requestLogModel.setRequestType(servletRequest.getMethod());
            requestLogModel.setRequestingUrl(servletRequest.getRequestURI());
            requestLogModel.setRequestingPort(String.valueOf(servletRequest.getRemotePort()));
            requestLogModel.setRequestId(request.getHeader().getRequestId());
            requestLogModel.setDescription(stringBuilder.toString());

            return requestLogModel;

        } catch (Exception exception) {
            log.error("Exception while validating the request header at {}. Message {}", new Date(), exception.getMessage());
            throw new ExceptionManager(exception.getMessage());
        }
    }

    public static boolean isPhoneNumberValid(String phoneNumber) {
        if(phoneNumber.startsWith("+")) {
            Pattern pattern = Pattern.compile("^\\+\\d{12}$");
            Matcher matcher = pattern.matcher(phoneNumber);
            return !matcher.matches();
        }

        /*if(phoneNumber.startsWith("0")) {
            Pattern pattern = Pattern.compile("^\\d{10}$");
            Matcher matcher = pattern.matcher(phoneNumber);
            return matcher.matches();
        }*/
        return true;
    }

    public static boolean isDateValid(String dateStr) {
        try {
            refFormat.setLenient(false);
            refFormat.parse(dateStr);
        } catch (DateTimeParseException | ParseException e) {
            return false;
        }
        return true;
    }

    public static boolean isAmountValid(String amount) {
            Pattern pattern = Pattern.compile("^(\\d+)?$");
            Matcher matcher = pattern.matcher(amount);
        return matcher.matches();
    }


    public static boolean isPinFormatValid(String pin) {
        if(pin.isBlank())
            return  false;

        return pin.length() >= 4;
    }

    public static void saveTrailLog(RequestLogModel requestLogModel, RequestLogTrailService requestLogTrailService) {
        try {
            log.info("Saving request trail {} to the db at {} for the request {}", requestLogModel.getRequestId(), new Date(), requestLogModel.getRequest());
            requestLogModel.setDescription(requestLogModel.getDescription() +"\n" + new Date());
            requestLogTrailService.saveLog(requestLogModel);
        } catch (Exception exception) {
            log.error("Request log trail could not be saved at {}. Exception message is {}", new Date(), exception.getMessage());
        }
    }

    private static byte[] getSaltFrom(byte[] hash) {
        byte[] salt = new byte[16];
        System.arraycopy(hash, 0, salt, 0, 16);
        return salt;
    }
}
