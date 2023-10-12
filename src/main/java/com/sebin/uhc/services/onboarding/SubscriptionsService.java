package com.sebin.uhc.services.onboarding;

import com.sebin.uhc.commons.*;
import com.sebin.uhc.entities.Wallet;
import com.sebin.uhc.entities.notifications.Sms;
import com.sebin.uhc.entities.onboarding.Beneficiaries;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.entities.onboarding.UnSubscriptionsRequests;
import com.sebin.uhc.exceptions.ExceptionManager;
import com.sebin.uhc.models.Gender;
import com.sebin.uhc.models.RequestLogModel;
import com.sebin.uhc.models.SmsContext;
import com.sebin.uhc.models.Subscription;
import com.sebin.uhc.models.requests.onboarding.ChangePin;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.requests.onboarding.SetPin;
import com.sebin.uhc.models.requests.onboarding.ValidatePin;
import com.sebin.uhc.models.requests.payments.WalletBalanceRequest;
import com.sebin.uhc.models.responses.onboarding.Header;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.models.responses.onboarding.SubscriptionInquiry;
import com.sebin.uhc.repositories.SmsRepository;
import com.sebin.uhc.repositories.onboarding.BeneficiaryRepository;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepository;
import com.sebin.uhc.repositories.onboarding.UnsubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;



@Slf4j(topic = ":: SERVICE :: SUBSCRIPTION :::")
@Service
@Transactional
public class SubscriptionsService {
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;
    @Autowired
    private RequestLogTrailService requestLogTrailService;
    @Autowired
    private UnsubscriptionRepository unsubscriptionRepository;
    @Autowired
    private SubscriptionsRepository repository;
    @Autowired
    private SmsRepository smsRepository;
    @Autowired
    private SubscriptionArchService subscriptionArchService;

    public Response<?> create(Request<Subscription> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            log.info("Subscription request validation started at {}", new Date());
            stringBuilder.append("\n").append("Request validation started.");

            Predicate<Subscription> subscriptionPredicate = Objects::isNull;
            Predicate<String> strPredicate = str -> str == null || str.isBlank();

            if(subscriptionPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Null or empty request body.");
                log.info("Validation failed fot the requires. Request is null or empty {}", new Date());
                throw new ExceptionManager("Basic details missing, cannot proceed.", ResponseCodes.BASIC_DETAILS_MISSING.getCode());
            }

            if(strPredicate.test(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Missing mobile number.");
                log.info("Validation failed. Missing mobile number {}", new Date());
                throw new ExceptionManager("Mobile number is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(Helper.isPhoneNumberValid(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Wrong mobile number format.");
                log.info("Validation failed. Wrong mobile number format {}", new Date());
                throw new ExceptionManager("Mobile number must be in the format +254...", ResponseCodes.INVALID_MOBILE_NUMBER_FORMAT.getCode());
            }

            if(strPredicate.test(request.getBody().getPersonId())) {
                stringBuilder.append("\n").append("Missing national Id or passport.");
                log.info("Validation failed. Missing national Id or passport {}", new Date());
                throw new ExceptionManager("National Id or passport missing.", ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            if(strPredicate.test(request.getBody().getFirstName())) {
                stringBuilder.append("\n").append("Missing first name.");
                log.info("Validation failed. Missing first name {}", new Date());
                throw new ExceptionManager("First name is missing", ResponseCodes.FIRST_NAME_MISSING.getCode());
            }

            if(strPredicate.test(request.getBody().getDateOfBirth())) {
                stringBuilder.append("\n").append("Missing date of birth.");
                log.info("Validation failed. Missing date of birth {}", new Date());
                throw new ExceptionManager("Date of birth is missing", ResponseCodes.DATE_OF_BIRTH_MISSING.getCode());
            }

            if(strPredicate.test(request.getBody().getGender())) {
                stringBuilder.append("\n").append("Missing gender.");
                log.info("Validation failed. Missing gender {}", new Date());
                throw new ExceptionManager("Gender is missing", ResponseCodes.MISSING_GENDER.getCode());
            }

            if(!(EnumUtils.isValidEnum(Gender.class, request.getBody().getGender().toUpperCase())))
            {
                stringBuilder.append("\n").append("Invalid gender");
                log.info("Invalid gender in the request {}", new Date());
                throw new ExceptionManager("Invalid gender in the request.", ResponseCodes.INVALID_GENDER.getCode());
            }

            if(!Helper.isDateValid(request.getBody().getDateOfBirth()))
            {
                stringBuilder.append("\n").append("Invalid date of birth.");
                log.info("Validation failed. Invalid date of birth {}", new Date());
                throw new ExceptionManager("Invalid Date of birth", ResponseCodes.DATE_OF_BIRTH_MISSING.getCode());
            }

            if(inquireMobile(new Request<>(request.getBody().getMobileNumber()), requestLogModel).getBody().isSubscribed()) {
                stringBuilder.append("\nMobile number already subscribed.");
                log.info("Attempt to create an existing mobile number subscription request at {}", new Date());
                throw new ExceptionManager(String.format("Mobile number %s already subscribed.", request.getBody().getMobileNumber()), ResponseCodes.SUBSCRIBER_ALREADY_EXISTS.getCode());
            }

            if(inquireId(new Request<>(request.getBody().getPersonId()), requestLogModel).getBody().isSubscribed()) {
                stringBuilder.append("\n").append("National Id or passport already subscribed.");
                log.info("National Id or Passport '{}' already subscribed, cannot subscribe again.", request.getBody().getPersonId());
                throw new ExceptionManager(String.format("National Id or Passport %s is already subscribed.", request.getBody().getPersonId()), ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            if(request.getBody().isNHIFMember())
            {
                if (strPredicate.test(request.getBody().getMemberNumber())) {
                    stringBuilder.append("\n").append("Missing NHIF number");
                    log.info("Beneficiary request missing NHIF member number {} for request {}", new Date(), request.getHeader().getRequestId());
                    throw new ExceptionManager("Member number is missing.", ResponseCodes.MEMBER_NUMBER_MISSING.getCode());
                }
            }

            Subscriptions person = new Subscriptions(request.getBody().getMobileNumber(), request.getBody().getPersonId(), request.getBody().getFirstName(), request.getBody().getMiddleName(), request.getBody().getSurname(), Statuses.ACTIVE.getStatus(), request.getBody().isNHIFMember(), new Wallet());
            person.setMemberNumber(request.getBody().getMemberNumber());
            person.setDateOfBirth(request.getBody().getDateOfBirth());
            person.setGender(Gender.valueOf(request.getBody().getGender().toUpperCase()));
            person.setKcbMessageId("S"+request.getBody().getPersonId().trim());
            person.setKcbExternalId("X"+request.getBody().getPersonId().trim());
            person = repository.save(person);
            person.setKcbMessageId(person.getKcbMessageId()+"-"+person.getId());
            person.setKcbExternalId(person.getKcbExternalId()+"-"+person.getId());
            person=repository.save(person);

            Sms sms = new Sms();
            sms.setDateCreated(LocalDateTime.now());
            sms.setSmsContext(SmsContext.WELCOME);
            sms.setMobileNumber(person.getMobileNumber());
            sms.setMessage("Dear "+person.getFirstName()+",\nWelcome to NHIF Premium, You can now make contributions to your wallet. Call 0700000000 for any enquiries");
            sms.setReferenceNumber("SMS"+person.getPersonId());
            smsRepository.save(sms);
            sms.setReferenceNumber(sms.getReferenceNumber()+"-"+sms.getId());
            smsRepository.save(sms);

            stringBuilder.append("\nSeems to have saved the record for subscription; one more check to ascertain.");

            if(person.getId() > 0) {
                if(request.getBody().isNHIFMember()) {
                    log.info("Subscriber is also a member of the scheme, auto-creating as beneficiary");
                    stringBuilder.append("\n").append("Is member of scheme, creating them as beneficiary");
                    if(strPredicate.test(request.getBody().getMemberNumber())) {
                        log.info("It was indicated that the subscriber is a member of the scheme but member not was not provided; could not create them as beneficiary at {} for {}", new Date(), requestLogModel.getRequestId());
                        stringBuilder.append("\n").append("Scheme member number was not provided.");
                    } else {
                        stringBuilder.append("\n").append("Member number provided");
                        Beneficiaries beneficiaries = new Beneficiaries();
                        beneficiaries.setSubscriptions(person);
                        beneficiaries.setStatus(Statuses.ACTIVE.getStatus());
                        beneficiaries.setMemberNumber(request.getBody().getMemberNumber());
                        beneficiaries.setDateOfBirth(request.getBody().getDateOfBirth());
                        beneficiaries.setFirstName(request.getBody().getFirstName());
                        beneficiaries.setMiddleName(request.getBody().getMiddleName());
                        beneficiaries.setSurname(request.getBody().getSurname());
                        beneficiaries.setPersonId(request.getBody().getPersonId());
                        beneficiaries.setLastUpdate(LocalDateTime.now());
                        beneficiaries.setDateCreated(LocalDateTime.now());
                        person.setBeneficiaries(List.of(beneficiaries));
                        stringBuilder.append("\n").append("Created as beneficiary");
                    }
                }
                stringBuilder.append("\nSubscription was successful.");
                log.info("Subscription for {} was successful {}", request.getBody().getPersonId(), new Date());
                return new Response<>(new Header(true, ResponseCodes.SUCCESS.getCode(), "Subscription was successful."));
            }
            else {
                stringBuilder.append("\nCould not save the subscription to the DB");
                log.info("Subscription for {} failed at {}", request.getBody().getPersonId(), new Date());
                return new Response<>(new Header( "Subscription failed, try again later.", ResponseCodes.FAIL.getCode()));
            }
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error("Exception {} occurred while creating a request at {}", exception.getMessage(), new Date());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log.");
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
                log.info("Request {} log saved", request.getHeader().getRequestId());
            } catch (Exception exception) {
                log.error("There was an exception while saving the log trail at subscription time.");
            }
        }
    }

    public Response<SubscriptionInquiry> inquireMobile(Request<String> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Predicate<String> stringPredicate = str -> str == null || str.isBlank();
            if(stringPredicate.test(request.getBody())) {
                stringBuilder.append("\nMobile number not found in the request.");
                log.info("Could not find mobile number in the request {}", new Date());
                throw new ExceptionManager("Mobile number required.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(Helper.isPhoneNumberValid(request.getBody())) {
                stringBuilder.append("\n").append("Mobile number not valid");
                log.info("Mobile number not valid for request id {}", request.getHeader().getRequestId());
                throw new ExceptionManager("Mobile number not valid", ResponseCodes.INVALID_MOBILE_NUMBER_FORMAT.getCode());
            }

            Optional<Subscriptions> person = repository.findByMobileNumber(request.getBody());
            SubscriptionInquiry subscriptionInquiry = new SubscriptionInquiry();
            if(person.isPresent()) {
                stringBuilder.append(String.format("%s already subscribed", request.getBody()));
                subscriptionInquiry.setSubscribed(true);
                subscriptionInquiry.setPinSet(!stringPredicate.test(person.get().getPassword()));
                subscriptionInquiry.setProfile(new Subscription(person.get().getPersonId(), person.get().getMobileNumber(), person.get().getFirstName(), person.get().getMiddleName(), person.get().getSurname(), person.get().isNHIFMember(),person.get().getMemberNumber(),person.get().getDateOfBirth(),person.get().getGender().name()));
            }

            if(!isHeaderNull(request))
                stringBuilder.append("\n").append("Responding");

            return new Response<>(new Header(true,  ResponseCodes.SUCCESS.getCode(), "Request successful"), subscriptionInquiry);
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error("Exception while doing inquiry using mobile number at {}", new Date());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                if(!isHeaderNull(request)) {
                    stringBuilder.append("\n").append("Saving request log.");
                    requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                    Helper.saveTrailLog(requestLogModel, requestLogTrailService);
                }

                log.info("Request {} log saved", isHeaderNull(request) ? "Subscription phone validation call within." : request.getHeader().getRequestId());
            } catch (Exception exception) {
                log.error("Exception while saving the request log trail at {}", new Date());
            }
        }
    }

    private boolean isHeaderNull(Request<?> request) {
        return request.getHeader() == null;
    }
    public Response<SubscriptionInquiry> subscriptionInquiry(Request<WalletBalanceRequest> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Predicate<WalletBalanceRequest> requestPredicate = Objects::isNull;
            Predicate<String> stringPredicate = str -> str == null || str.isBlank();
            if(requestPredicate.test(request.getBody())) {
                log.info("Subscription inquiry request is null at {}", new Date());
                stringBuilder.append("\n").append("Null request");
                throw new ExceptionManager("Null request body", ResponseCodes.NULL_REQUEST.getCode());
            }

            if(stringPredicate.test(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Mobile number not found in the request.");
                log.info("Could not find mobile number in the request {}", new Date());
                throw new ExceptionManager("Mobile number required.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(Helper.isPhoneNumberValid(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Mobile number not valid");
                log.info("Mobile number not valid for request id {}", request.getHeader().getRequestId());
                throw new ExceptionManager("Mobile number not valid", ResponseCodes.INVALID_MOBILE_NUMBER_FORMAT.getCode());
            }

            if(stringPredicate.test(request.getBody().getIdNumber())) {
                stringBuilder.append("\n").append("Missing national Id or passport");
                log.info("Missing national Id or passport in the request {} at {}", request.getHeader().getRequestId(), new Date());
                throw new ExceptionManager("National Id or passport missing in the request", ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            Optional<Subscriptions> person = repository.findByMobileNumber(request.getBody().getMobileNumber());
            SubscriptionInquiry subscriptionInquiry = new SubscriptionInquiry();
            if(person.isPresent()) {
                stringBuilder.append("\n").append(String.format("Mobile number %s is already subscribed.", request.getBody().getMobileNumber()));
                subscriptionInquiry.setSubscribed(true);
                subscriptionInquiry.setPinSet(!stringPredicate.test(person.get().getPassword()));
                subscriptionInquiry.setProfile(getSubscription(person.get()));
            } else {
                person = repository.findByPersonId(request.getBody().getIdNumber());
                if(person.isPresent()) {
                    stringBuilder.append("\n").append(String.format("ID or passport %s is already subscribed.", request.getBody().getMobileNumber()));
                    subscriptionInquiry.setSubscribed(true);
                    subscriptionInquiry.setPinSet(!stringPredicate.test(person.get().getPassword()));
                    subscriptionInquiry.setProfile(getSubscription(person.get()));
                }
            }
            stringBuilder.append("\n").append("Not subscribed yet.");
            stringBuilder.append("\n").append("Responding");

            return new Response<>(new Header(true,  ResponseCodes.SUCCESS.getCode(), "Request successful"), subscriptionInquiry);
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error("Exception while doing inquiry using mobile number at {}", new Date());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log");
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
                log.info("Request {} log saved", request.getHeader().getRequestId());
            } catch (Exception exception) {
                log.error("Exception while saving the request log trail at {}", new Date());
            }
        }
    }

    private Subscription getSubscription(Subscriptions person) {
        return new Subscription(person.getPersonId(), person.getMobileNumber(), person.getFirstName(), person.getMiddleName(), person.getSurname(), person.isNHIFMember(),person.getMemberNumber(),person.getDateOfBirth(),person.getGender().name());
    }

    public Response<SubscriptionInquiry> inquireId(Request<String> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Predicate<String> stringPredicate = str -> str == null || str.isBlank();
            if(stringPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Missing national Id or passport.");
                log.info("National Id or passport not provided {}", new Date());
                throw new ExceptionManager("National Id or passport not provided.", ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            Optional<Subscriptions> person = repository.findByPersonId(request.getBody());
            SubscriptionInquiry subscriptionInquiry = new SubscriptionInquiry();
            if(person.isPresent()) {
                stringBuilder.append("\n").append(String.format("%s is already subscribed.", request.getBody()));
                subscriptionInquiry.setSubscribed(true);
                subscriptionInquiry.setPinSet(!stringPredicate.test(person.get().getPassword()));
                subscriptionInquiry.setProfile(getSubscription(person.get()));
            }
            if(!isHeaderNull(request))
                stringBuilder.append("\n").append("Responding");

            return new Response<>(new Header(true,  ResponseCodes.SUCCESS.getCode(), "Request successful"), subscriptionInquiry);
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error("Exception while handling inquiry at {} with message {}", new Date(), exception.getMessage());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                if(!isHeaderNull(request)) {
                    stringBuilder.append("\n").append("Saving request log");
                    requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                    Helper.saveTrailLog(requestLogModel, requestLogTrailService);
                }

                log.info("Request {}", request.getHeader() == null ? "Subscription Id or passport validation call within" : request.getHeader().getRequestId());
            } catch (Exception exception) {
                log.info("Exception {} while saving request log trail to DB at {}", exception.getMessage(), new Date());
            }
        }
    }

    public Response<?> unsubscribe(Request<String> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder("Validation started.");
        try {
            Predicate<Request<String>> stringPredicate = str -> str == null || str.getBody().isBlank();
            if(stringPredicate.test(request)) {
                stringBuilder.append("\n").append("Missing national Id or passport.");
                log.info("National Id or passport missing in the request to unsubscribe {}", new Date());
                throw new ExceptionManager( "National Id or passport is required to unsubscribe", ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            Optional<Subscriptions> person = repository.findByMobileNumber(request.getBody());
            if(person.isEmpty()) {
                stringBuilder.append("\n").append(String.format("Subscriber %s not found", request.getBody()));
                log.info("Subscriber ({}) not found.", request);
                throw new ExceptionManager(String.format("Subscriber %s not found.", request.getBody()), ResponseCodes.SUBSCRIBER_ALREADY_EXISTS.getCode());
            }

            if(person.get().getWallet().getAmount() > 0) {
                stringBuilder.append("\n").append("Balance has to be zero");
                log.info("Wallet balance not zero, cannot opt out for subscriber {} at {} for {}", request.getBody(), new Date(), requestLogModel.getRequestId());
                throw new ExceptionManager("Balance has to be zero to unsubscribe.", ResponseCodes.FAIL.getCode());
            }

            // Check with KCB whether the person should be allowed to un-subscribe; taking into account the loan effect
            Collection<UnSubscriptionsRequests> unSubscriptionsRequests = unsubscriptionRepository.findByPersonIdAndStatus(person.get().getPersonId(), Statuses.PENDING.getStatus());
            if(!unSubscriptionsRequests.isEmpty()) {
                stringBuilder.append("\n").append("Similar request pending.");
                return new Response<>(new Header("There is a similar request pending.", ResponseCodes.SIMILAR_REQUEST_PENDING.getCode()));
            }

            UnSubscriptionsRequests unsubscriptionsRequests = new UnSubscriptionsRequests(person.get().getPersonId(), "KCB", "callbackURl", LocalDateTime.now(), LocalDateTime.now(), Statuses.REMOVED.getStatus(), "requestId");
            unsubscriptionsRequests = unsubscriptionRepository.save(unsubscriptionsRequests);
            if(unsubscriptionsRequests.getId() > 0) {
                stringBuilder.append("\n").append("Request to unsubscribe logged.");
                log.info("Subscriber requested logged in request logs successfully.");
                if(removeSubscriber(person.get())) {
                    stringBuilder.append("\n").append("Opted out successfully.");
                    log.info("{} for request {} un-subscribed successfully at {}", person.get().getPersonId(), request.getHeader().getRequestId(), new Date());
                    return new Response<>(new Header("Successfully opted out of this service.", ResponseCodes.SUCCESS.getCode()));
                } else {
                    stringBuilder.append("\n").append("Could not opt out");
                    log.info("{} for request {} could not be subscribed at {}", person.get().getPersonId(), request.getHeader().getRequestId(), new Date());
                    return new Response<>(new Header("Could not opt out at the moment, please try again later.", ResponseCodes.FAIL.getCode()));
                }
            } else {
                stringBuilder.append("\n").append("Could not place un-subscription request.");
                log.info("Opting out request could not be placed at {} for the request Id {}", new Date(), request.getHeader().getRequestId());
                return new Response<>(new Header("Could not opt out at the moment, please try again later.", ResponseCodes.FAIL.getCode()));
            }
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error("Exception while unsubscribing at {}", new Date());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log.");
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
                assert request != null;
                log.info("Request {} log saved", request.getHeader().getRequestId());
            } catch (Exception exception) {
                log.info("Exception occurred while saving the request tail log at {} with message {}", new Date(), exception.getMessage());
            }
        }
    }



    public Response<?> setPin(Request<SetPin> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder("Request validation");
        try {
            log.info("Subscription request validation started at {}", new Date());

            Predicate<SetPin> pinPredicate = Objects::isNull;
            Predicate<String> strPredicate = str -> str == null || str.isBlank();

            if(pinPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Null or empty request body.");
                log.info("Validation failed for the requires. Request is null or empty {}", new Date());
                throw new ExceptionManager("Basic details missing, cannot proceed.", ResponseCodes.BASIC_DETAILS_MISSING.getCode());
            }

            if(strPredicate.test(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Missing mobile number.");
                log.info("Validation failed. Missing mobile number {}", new Date());
                throw new ExceptionManager("Mobile number is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(Helper.isPhoneNumberValid(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Wrong mobile number format.");
                log.info("Validation failed. Wrong mobile number format {}", new Date());
                throw new ExceptionManager("Mobile number must be in the format +254...", ResponseCodes.INVALID_MOBILE_NUMBER_FORMAT.getCode());
            }

            if(strPredicate.test(request.getBody().getPIN())) {
                stringBuilder.append("\n").append("Missing PIN.");
                log.info("Validation failed. Missing PIN number {}", new Date());
                throw new ExceptionManager("PIN is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(!Helper.isPinFormatValid(request.getBody().getPIN())) {
                stringBuilder.append("\n").append("PIN length must be greater than four characters without spaces.");
                log.info("Validation failed. PIN length must be greater than four characters without spaces {}", new Date());
                throw new ExceptionManager("PIN length must be greater than four characters without spaces", ResponseCodes.PIN_FORMAT_INVALID.getCode());
            }

            if(strPredicate.test(request.getBody().getConfirmPIN())) {
                stringBuilder.append("\n").append("Missing ConfirmPIN.");
                log.info("Validation failed. Missing ConfirmPIN {}", new Date());
                throw new ExceptionManager("ConfirmPIN is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(!Helper.isPinFormatValid(request.getBody().getConfirmPIN())) {
                stringBuilder.append("\n").append("Confirmation PIN length must be greater than four characters.");
                log.info("Validation failed. confirmation PIN length must be greater than four characters. {}", new Date());
                throw new ExceptionManager("confirmation PIN length must be greater than four characters.", ResponseCodes.PIN_FORMAT_INVALID.getCode());
            }

            Optional<Subscriptions> person = repository.findByMobileNumber(request.getBody().getMobileNumber());
            if(person.isEmpty()) {
                stringBuilder.append("\n").append(String.format("Subscriber %s not found", request.getBody()));
                log.info("Subscriber {} not found.", request);
                return new Response<>(new Header(String.format("Subscriber %s not found.", request.getBody().getMobileNumber())));
            }

            if(person.get().getPassword() != null) {
                stringBuilder.append("\n").append("PIN already exists.");
                log.info("Validation failed. PIN already exists. {}", new Date());
                throw new ExceptionManager("PIN already exists.", ResponseCodes.PIN_EXISTS.getCode());
            }

            if(!(request.getBody().getPIN().equals(request.getBody().getConfirmPIN()))) {
                throw new ExceptionManager("New PINs do not match", ResponseCodes.PIN_MISMATCH.getCode());
            }

            person.get().setPassword(PasswordHandler.createHash(request.getBody().getPIN()));
            repository.save(person.get());

            stringBuilder.append("\n").append("PIN set was successful.");
            log.info("PIN set for {} was successful {}", request.getBody().getMobileNumber(), new Date());
            return new Response<>(new Header(true, ResponseCodes.SUCCESS.getCode(), "PIN set successfully."));
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error(exception.getMessage());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log to db");
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
                log.info("Request {} log saved", request.getHeader().getRequestId());
            } catch (Exception exception) {
                log.error("Exception occurred while saving the request trail log into the DB at {} for {}", new Date(), request.getHeader().getRequestId());
            }
        }
    }


    public Response<?> validatePin(Request<ValidatePin> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            log.info("Subscription request validation started at {}", new Date());
            stringBuilder.append("\n").append(String.format("Request validation in the service started at %s.", new Date()));

            Predicate<ValidatePin> pinPredicate = Objects::isNull;
            Predicate<String> strPredicate = str -> str == null || str.isBlank();

            if(pinPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Null or empty request body.");
                log.info("Validation failed for the requires. Request is null or empty {}", new Date());
                throw new ExceptionManager("Basic details missing, cannot proceed.", ResponseCodes.BASIC_DETAILS_MISSING.getCode());
            }

            if(strPredicate.test(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Missing mobile number.");
                log.info("Validation failed. Missing mobile number {}", new Date());
                throw new ExceptionManager("Mobile number is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(strPredicate.test(request.getBody().getIdNumber())) {
                stringBuilder.append("\n").append("Missing ID number.");
                log.info("Validation failed. Missing ID number {}", new Date());
                throw new ExceptionManager("ID number is missing.", ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            if(Helper.isPhoneNumberValid(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Wrong mobile number format.");
                log.info("Validation failed. Wrong mobile number format {}", new Date());
                throw new ExceptionManager("Mobile number must be in the format +254...", ResponseCodes.INVALID_MOBILE_NUMBER_FORMAT.getCode());
            }

            if(strPredicate.test(request.getBody().getPIN())) {
                stringBuilder.append("\n").append("Missing PIN.");
                log.info("Validation failed. Missing PIN number {}", new Date());
                throw new ExceptionManager("PIN is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(!Helper.isPinFormatValid(request.getBody().getPIN())) {
                stringBuilder.append("\n").append("PIN length must be greater than four characters without spaces.");
                log.info("Validation failed. PIN length must be greater than four characters without spaces {}", new Date());
                throw new ExceptionManager("PIN length must be greater than four characters without spaces", ResponseCodes.PIN_FORMAT_INVALID.getCode());
            }


            Optional<Subscriptions> person = repository.findByPersonId(request.getBody().getIdNumber());
            if(person.isEmpty()) {
                stringBuilder.append("\n").append(String.format("Subscriber %s not found", request.getBody()));
                log.info("Subscriber {} not found.", request);
                return new Response<>(new Header(String.format("Subscriber %s not found.", request.getBody().getIdNumber())));
            }


            if(!PasswordHandler.validatePassword(request.getBody().getPIN(), person.get().getPassword())) {
                throw new ExceptionManager("Incorrect PIN", ResponseCodes.PIN_INCORRECT.getCode());
            }

            stringBuilder.append("\n").append("PIN validation was successful.");
            log.info("PIN set for {} was successful {}", request.getBody().getMobileNumber(), new Date());
            return new Response<>(new Header(true,  ResponseCodes.SUCCESS.getCode(), "PIN validated successfully."));
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error("Exception {}", exception.getMessage());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log.");
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
                log.info("Request {} log saved", request.getHeader().getRequestId());
            } catch (Exception exception) {
                log.error("Exception while saving the request log trail at {} for {}", new Date(), request.getHeader().getRequestId());
            }
        }
    }



    @Transactional
    public Response<?> changePin(Request<ChangePin> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            log.info("Subscription request validation started at {}", new Date());
            stringBuilder.append("\n").append(String.format("Request validation in the service started at %s.", new Date()));

            Predicate<ChangePin> pinPredicate = Objects::isNull;
            Predicate<String> strPredicate = str -> str == null || str.isBlank();

            if(pinPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Null or empty request body.");
                log.info("Validation failed for the requires. Request is null or empty {}", new Date());
                throw new ExceptionManager("Basic details missing, cannot proceed.", ResponseCodes.BASIC_DETAILS_MISSING.getCode());
            }

            if(strPredicate.test(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Missing mobile number.");
                log.info("Validation failed. Missing mobile number {}", new Date());
                throw new ExceptionManager("Mobile number is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(Helper.isPhoneNumberValid(request.getBody().getMobileNumber())) {
                stringBuilder.append("\n").append("Wrong mobile number format.");
                log.info("Validation failed. Wrong mobile number format {}", new Date());
                throw new ExceptionManager("Mobile number must be in the format +254...", ResponseCodes.INVALID_MOBILE_NUMBER_FORMAT.getCode());
            }

            if(strPredicate.test(request.getBody().getPIN())) {
                stringBuilder.append("\n").append("Missing PIN.");
                log.info("Validation failed. Missing PIN number {}", new Date());
                throw new ExceptionManager("PIN is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(!Helper.isPinFormatValid(request.getBody().getPIN())) {
                stringBuilder.append("\n").append("PIN length must be greater than four characters without spaces.");
                log.info("Validation failed. PIN length must be greater than four characters without spaces {}", new Date());
                throw new ExceptionManager("PIN length must be greater than four characters without spaces", ResponseCodes.PIN_FORMAT_INVALID.getCode());
            }

            if(strPredicate.test(request.getBody().getNewPIN())) {
                stringBuilder.append("\n").append("Missing ConfirmPIN.");
                log.info("Validation failed. Missing NewPIN {}", new Date());
                throw new ExceptionManager("New pin is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(!Helper.isPinFormatValid(request.getBody().getNewPIN())) {
                stringBuilder.append("\n").append("NewPIN length must be greater than four characters.");
                log.info("Validation failed. NewPIN length must be greater than four characters. {}", new Date());
                throw new ExceptionManager("NewPIN length must be greater than four characters.", ResponseCodes.PIN_FORMAT_INVALID.getCode());
            }

            if(strPredicate.test(request.getBody().getConfirmNewPIN())) {
                stringBuilder.append("\n").append("Missing ConfirmPIN.");
                log.info("Validation failed. Missing ConfirmNewPIN {}", new Date());
                throw new ExceptionManager("ConfirmNewPIN is missing.", ResponseCodes.MOBILE_NUMBER_MISSING.getCode());
            }

            if(!Helper.isPinFormatValid(request.getBody().getConfirmNewPIN())) {
                stringBuilder.append("\n").append("ConfirmNewPIN length must be greater than four characters.");
                log.info("Validation failed. ConfirmNewPIN length must be greater than four characters. {}", new Date());
                throw new ExceptionManager("ConfirmNewPIN length must be greater than four characters.", ResponseCodes.PIN_FORMAT_INVALID.getCode());
            }

            Optional<Subscriptions> person = repository.findByMobileNumber(request.getBody().getMobileNumber());
            if(person.isEmpty()) {
                stringBuilder.append("\n").append(String.format("Subscriber %s not found", request.getBody()));
                log.info("Subscriber {} not found.", request);
                return new Response<>(new Header(String.format("Subscriber %s not found.", request.getBody().getMobileNumber())));
            }

            if(!(request.getBody().getNewPIN().equals(request.getBody().getConfirmNewPIN()))) {
                throw new ExceptionManager("New PINs do not match", ResponseCodes.PIN_MISMATCH.getCode());
            }

            if(!PasswordHandler.validatePassword(request.getBody().getPIN(), person.get().getPassword())) {
                throw new ExceptionManager("Incorrect PIN", ResponseCodes.PIN_INCORRECT.getCode());
            }

            person.get().setPassword(PasswordHandler.createHash(request.getBody().getNewPIN()));

            stringBuilder.append("\n").append("PIN update was successful.");
            log.info("PIN update for {} was successful {}", request.getBody().getMobileNumber(), new Date());
            return new Response<>(new Header(true, ResponseCodes.SUCCESS.getCode(), "PIN updated successfully."));
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error("Exception: {}", exception.getMessage());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log");
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
                log.info("Request {} log saved", request.getHeader().getRequestId());
            } catch (Exception exception) {
                log.error("Exception while saving the request log trail at {} for {}", new Date(), request.getHeader().getRequestId());
            }
        }
    }

    @Transactional
    public boolean removeSubscriber(Subscriptions subscriptions) {
        try {
            subscriptions.setStatus(Statuses.INACTIVE.getStatus());
            log.info("Subscriber {} opted out, record is being archived at {}", subscriptions.getPersonId(), new Date());
            subscriptionArchService.archiveSubscription(subscriptions);
            repository.deleteById(subscriptions.getId());
            return true;
        } catch (Exception exception) {
            log.error("Exception while flagging of subscriber at opting out at {}", new Date());
            throw new ExceptionManager("Sorry, we are unable to unsubscribe you at the moment. Try again later.", ResponseCodes.FAIL.getCode());
        }
    }
}
