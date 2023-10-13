package com.sebin.uhc.services.onboarding;

import com.sebin.uhc.commons.Helper;
import com.sebin.uhc.commons.ResponseCodes;
import com.sebin.uhc.commons.Statuses;
import com.sebin.uhc.entities.notifications.Sms;
import com.sebin.uhc.entities.onboarding.Beneficiaries;
import com.sebin.uhc.entities.onboarding.BeneficiariesArch;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.exceptions.ExceptionManager;
import com.sebin.uhc.models.Beneficiary;
import com.sebin.uhc.models.RequestLogModel;
import com.sebin.uhc.models.SmsContext;
import com.sebin.uhc.models.requests.onboarding.Request;
import com.sebin.uhc.models.requests.onboarding.SponsorBeneficiary;
import com.sebin.uhc.models.responses.onboarding.Header;
import com.sebin.uhc.models.responses.onboarding.Response;
import com.sebin.uhc.repositories.SmsRepository;
import com.sebin.uhc.repositories.onboarding.BeneficiaryRepository;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

@Slf4j(topic = ":: SERVICE :: BENEFICIARY :::")
@Service
@Transactional
public class BeneficiariesService {

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;
    @Autowired
    private BeneficiaryRepository repository;
    @Autowired
    private RequestLogTrailService requestLogTrailService;
    @Autowired
    private BeneficiaryArchService beneficiaryArchService;
    @Autowired
    private SmsRepository smsRepository;

    public Response<?> addBeneficiary(Request<Beneficiary> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\n").append("Validation started.");
            log.info("Validation for request addition request started {} for {}", new Date(), requestLogModel.getRequestId());

            Predicate<Beneficiary> beneficiaryPredicate = Objects::isNull;
            Predicate<String> stringPredicate = str -> str == null || str.isBlank();

            if(beneficiaryPredicate.test(request.getBody())) {
                stringBuilder.append("\n").append("Null or empty request.");
                log.info("Null request request {}", new Date());
                throw new ExceptionManager("Null request, could not proceed.", ResponseCodes.NULL_REQUEST.getCode());
            }

            if (stringPredicate.test(request.getBody().getMemberNumber())) {
                stringBuilder.append("\n").append("Missing NHIF number");
                log.info("Beneficiary request missing NHIF member number {} for request {}", new Date(), request.getHeader().getRequestId());
                throw new ExceptionManager("Member number is missing.", ResponseCodes.MEMBER_NUMBER_MISSING.getCode());
            }

            if(stringPredicate.test(request.getBody().getPersonId())) {
                stringBuilder.append("\n").append("Missing national Id or passport");
                log.info("Missing national Id or passport in the request request {}", new Date());
                throw new ExceptionManager("National Id or passport is missing, could not proceed.", ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            if(stringPredicate.test(request.getBody().getFirstName())) {
                stringBuilder.append("\n").append("Missing first name");
                log.info("Missing first name in the request request {}", new Date());
                throw new ExceptionManager("First name is missing, could not proceed.", ResponseCodes.FIRST_NAME_MISSING.getCode());
            }

            Optional<Subscriptions> sponsor = subscriptionsRepository.findByMobileNumber(request.getBody().getSponsorMobileNumber());
            if(sponsor.isEmpty()) {
                stringBuilder.append("\n").append("Sponsor missing.");
                log.info("No sponsor was found matching the identification {} {}", request.getBody().getSponsorMobileNumber(), new Date());
                throw new ExceptionManager(String.format("Sorry, you seem not to be subscribed with the Mobile %s. Cannot proceed!", request.getBody().getSponsorMobileNumber()), ResponseCodes.SPONSOR_MISSING.getCode());
            }

           if((sponsor.get().getBeneficiaries().stream().filter(m->m.getPersonId().equals(request.getBody().getPersonId())).findAny().orElse(null)) != null) {
               stringBuilder.append("\n").append("Beneficiary already enrolled.");
               throw new ExceptionManager("Beneficiary is already registered for the Id or Passport provided", ResponseCodes.BENEFICIARY_ALREADY_SUBSCRIBED.getCode());
           }

            log.info("Preparing to add the request {} for the sponsor {} at {}", request.getBody().getPersonId(), request.getBody().getSponsorMobileNumber(), new Date());
            stringBuilder.append("\n").append("Adding beneficiary.");
            Beneficiaries person = new Beneficiaries(request.getBody().getPersonId(), request.getBody().getFirstName(), request.getBody().getMiddleName(), request.getBody().getSurname(), request.getBody().getMemberNumber(), Statuses.ACTIVE.getStatus());
            person.setSubscriptions(sponsor.get());
            person.setDateOfBirth(request.getBody().getDateOfBirth());
            person = repository.save(person);
            if(person.getId() > 0) {

                Sms sms = new Sms();
                sms.setDateCreated(LocalDateTime.now());
                sms.setSmsContext(SmsContext.WELCOME);
                sms.setMobileNumber(sponsor.get().getMobileNumber());
                sms.setMessage("Dear "+sponsor.get().getFirstName()+",\nYou have successfully added "+ ObjectUtils.defaultIfNull(person.getFirstName(),"")+" "+ObjectUtils.defaultIfNull(person.getMiddleName(),"")+" "+ObjectUtils.defaultIfNull(person.getSurname(),"")+" as your beneficiary.");
                sms.setReferenceNumber("SMS"+sponsor.get().getPersonId());
                sms =smsRepository.save(sms);
                sms.setReferenceNumber(sms.getReferenceNumber()+"-"+sms.getId());
                sms = smsRepository.save(sms);

                stringBuilder.append("\n").append("Beneficiary added.");
                log.info("Beneficiary {} added successfully to sponsor {} at {}", request.getBody().getPersonId(), request.getBody().getSponsorMobileNumber(), new Date());
                return new Response<>(new Header(true,  ResponseCodes.SUCCESS.getCode(), "Beneficiary added successfully."));
            }
            else {
                stringBuilder.append("\n").append("Failed to add beneficiary.");
                log.info("Beneficiary {} could not be added to the sponsor {} at {} for {}", request.getBody().getPersonId(), request.getBody().getPersonId(), new Date(), requestLogModel.getRequestId());
                return new Response<>(new Header( "Beneficiary could not be added at this time, please try again later.", ResponseCodes.FAIL.getCode()));
            }
        } catch (Exception ex) {
            stringBuilder.append("\n").append(ex.getMessage());
            log.error("Exception occurred while adding a beneficiary {} for sponsor {}. Error message {} at {} for {}", request.getBody().getPersonId(), request.getBody().getSponsorMobileNumber(), ex.getMessage(), new Date(), requestLogModel.getRequestId());
            throw new ExceptionManager(ex.getMessage());
        } finally {
            try {
                stringBuilder.append("\n").append("Saving request log trail.");
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
            } catch (Exception exception) {
                log.error("Exception while saving request log trail for {} at {}", request.getHeader().getRequestId(), new Date());
            }
        }
    }

    public Response<List<Beneficiary>> getBeneficiaries(Request<String> request, RequestLogModel requestLogModel) {
        Predicate<Request<String>> stringPredicate = str -> str == null || str.getBody().isBlank();
        if(stringPredicate.test(request)) {
            log.info("Sponsor Id missing in the beneficiaries list request {}", new Date());
            throw new ExceptionManager("Sponsor Id or passport not provided.", ResponseCodes.SPONSOR_MISSING.getCode());
        }

        Optional<Subscriptions> subscriptions =  subscriptionsRepository.findByMobileNumber(request.getBody());
        if(subscriptions.isEmpty()) {
            log.info("Provided sponsor {} is not subscribed {}", request, new Date());
            throw new ExceptionManager(String.format("Sponsor Mobile Number %s could not be found.", request.getBody()), ResponseCodes.SPONSOR_MISSING.getCode());
        }

        Response<List<Beneficiary>> response = new Response<>();
        List<Beneficiary> list = new ArrayList<>();
        subscriptions.get().getBeneficiaries().forEach(bn -> {
            if(bn.getStatus().equals(Statuses.ACTIVE.getStatus())) {
                Beneficiary beneficiary = new Beneficiary();
                beneficiary.setPersonId(bn.getPersonId());
                beneficiary.setFirstName(bn.getFirstName());
                beneficiary.setMiddleName(bn.getMiddleName());
                beneficiary.setSurname(bn.getSurname());
                beneficiary.setMemberNumber(bn.getMemberNumber());
                beneficiary.setSponsorMobileNumber(request.getBody());

                list.add(beneficiary);
            }
        });

        response.getHeader().setSuccess(true);
        response.getHeader().setResponseCode(ResponseCodes.SUCCESS.getCode());
        response.getHeader().setMessage("Information fetched successfully");
        response.setBody(list);

        return response;
    }

    @Transactional
    public Response<List<Beneficiary>> removeBeneficiary(Request<SponsorBeneficiary> request, RequestLogModel requestLogModel) {
        StringBuilder stringBuilder = new StringBuilder("Validation started.");
        try {
            Predicate<Request<SponsorBeneficiary>> sponsorBeneficiaryPredicate = Objects::isNull;
            Predicate<String> stringPredicate = str -> str == null || str.isBlank();

            if(sponsorBeneficiaryPredicate.test(request)) {
                stringBuilder.append("\n").append("Null or empty request.");
                log.info("Request object empty {}", new Date());
                throw new ExceptionManager("Both sponsor and/or beneficiary missing in the request.", ResponseCodes.BENEFICIARY_OR_SPONSOR_MISSING.getCode());
            }

            if(stringPredicate.test(request.getBody().getSponsorMobileNumber())) {
                stringBuilder.append("\n").append("Sponsor Id missing in the request.");
                log.info("Sponsor Id missing in the request {}", new Date());
                throw new ExceptionManager("Your Id is missing in the request.", ResponseCodes.ID_PASSPORT_MISSING.getCode());
            }

            if(stringPredicate.test(request.getBody().getBeneficiaryIdOrPassportNumber())) {
                stringBuilder.append("\n").append("Beneficiary Id is missing in the request.");
                log.info("Beneficiary Id not provided, cannot remove.");
                throw new ExceptionManager("Beneficiary Id is missing in your request.", ResponseCodes.BENEFICIARY_ID_MISSING.getCode());
            }

            Optional<Subscriptions> subscriptions = subscriptionsRepository.findByMobileNumber(request.getBody().getSponsorMobileNumber());
            if(subscriptions.isEmpty()) {
                stringBuilder.append("\n").append("Sponsor not found.");
                log.info("Sponsor with phone number {} not found {}", request.getBody().getSponsorMobileNumber(), new Date());
                throw new ExceptionManager(String.format("Sponsor with mobile %s not found.", request.getBody().getSponsorMobileNumber()), ResponseCodes.SPONSOR_MISSING.getCode());
            }

            Optional<Beneficiaries> beneficiary =  repository.findBeneficiary(request.getBody().getBeneficiaryIdOrPassportNumber(),Statuses.ACTIVE.getStatus(),subscriptions.get().getId());
            if(beneficiary.isPresent()) {
                if(beneficiary.get().getSubscriptions().getId() != subscriptions.get().getId()) {
                    stringBuilder.append("\n").append("Sponsor beneficiary not found");
                    log.info("Sponsor Beneficiary with Id {} for the sponsor with phone number {} was not found. {}", request.getBody().getBeneficiaryIdOrPassportNumber(), request.getBody().getSponsorMobileNumber(), new Date());
                    return new Response<>(new Header(String.format("Sponsor beneficiary with Id %s was not found.", request.getBody().getBeneficiaryIdOrPassportNumber()),ResponseCodes.FAIL.getCode()));
                }

                if(beneficiary.get().getPersonId().equals(subscriptions.get().getPersonId())) {
                    log.info("Beneficiary is also a subscriber; cannot be removed. {} at {}", requestLogModel.getRequestId(), new Date());
                    stringBuilder.append("\n").append("Beneficiary is also a subscriber. Cannot be removed.");
                    throw new ExceptionManager("You cannot remove yourself from beneficiaries.", ResponseCodes.FAIL.getCode());
                }

                stringBuilder.append("\n").append("Beneficiary found, removal stated.");
                log.info("Removing beneficiary {} at {} for sponsor {} for request {}", beneficiary.get().getPersonId(), new Date(), beneficiary.get().getSubscriptions().getPersonId(), request.getHeader().getRequestId());
                archiveBeneficiary(beneficiary.get());
                repository.delete(beneficiary.get());
                stringBuilder.append("\n").append("Beneficiary removed successfully.");

                Sms sms = new Sms();
                sms.setDateCreated(LocalDateTime.now());
                sms.setSmsContext(SmsContext.WELCOME);
                sms.setMobileNumber(subscriptions.get().getMobileNumber());
                sms.setMessage("Dear "+subscriptions.get().getFirstName()+",\nYou have successfully removed "+ ObjectUtils.defaultIfNull(beneficiary.get().getFirstName(),"")+" "+ObjectUtils.defaultIfNull(beneficiary.get().getMiddleName(),"")+" "+ObjectUtils.defaultIfNull(beneficiary.get().getSurname(),"")+" as your beneficiary.");
                sms.setReferenceNumber("SMS"+subscriptions.get().getPersonId());
                sms =smsRepository.save(sms);
                sms.setReferenceNumber(sms.getReferenceNumber()+"-"+sms.getId());
                sms = smsRepository.save(sms);

                return new Response<>(new Header(true,ResponseCodes.SUCCESS.getCode(),String.format("Beneficiary with Id %s was removed successfully.", request.getBody().getBeneficiaryIdOrPassportNumber())));
            }

            stringBuilder.append("\n").append("Beneficiary not found");
            log.info("Beneficiary with Id {} for the sponsor with phone number {} was not found. {}", request.getBody().getBeneficiaryIdOrPassportNumber(), request.getBody().getSponsorMobileNumber(), new Date());
            return new Response<>(new Header(String.format("Beneficiary with Id %s was not found.", request.getBody().getBeneficiaryIdOrPassportNumber()),ResponseCodes.FAIL.getCode()));
        } catch (Exception exception) {
            stringBuilder.append("\n").append(exception.getMessage());
            log.error("Exception while removing beneficiary at {} for {}", new Date(), request.getHeader().getRequestId());
            throw new ExceptionManager(exception.getMessage());
        } finally {
            try {
                requestLogModel.setDescription(requestLogModel.getDescription() + "\n" + stringBuilder);
                Helper.saveTrailLog(requestLogModel, requestLogTrailService);
            } catch (Exception exception) {
                log.error("Exception while saving request log trail at {} for {}", new Date(), request.getHeader().getRequestId());
            }
        }
    }

    private void archiveBeneficiary(Beneficiaries beneficiaries) {
        BeneficiariesArch beneficiariesArch = new BeneficiariesArch();
        beneficiariesArch.setPersonId(beneficiaries.getPersonId());
        beneficiariesArch.setFirstName(beneficiaries.getFirstName());
        beneficiariesArch.setMiddleName(beneficiaries.getMiddleName());
        beneficiariesArch.setSurname(beneficiaries.getSurname());
        beneficiariesArch.setStatus(beneficiaries.getStatus());
        beneficiariesArch.setDateRemoved(LocalDateTime.now());
        beneficiaryArchService.archiveBeneficiary(beneficiariesArch);
    }
}
