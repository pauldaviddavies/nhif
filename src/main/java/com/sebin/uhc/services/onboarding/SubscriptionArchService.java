package com.sebin.uhc.services.onboarding;

import com.sebin.uhc.entities.onboarding.Beneficiaries;
import com.sebin.uhc.entities.onboarding.BeneficiariesArch;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import com.sebin.uhc.entities.onboarding.SubscriptionsArch;
import com.sebin.uhc.repositories.onboarding.BeneficiaryRepositoryArch;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepositoryArch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j(topic = ":: SERVICE :: ARCH-REPOSITORY :::")
@Service
public class SubscriptionArchService {
    @Autowired
    private SubscriptionsRepositoryArch repository;
    @Autowired
    private BeneficiaryRepositoryArch beneficiaryRepositoryArch;

    public void archiveSubscription(Subscriptions subscriptions) {
        try {
            SubscriptionsArch subscriptionsArch = new SubscriptionsArch();
            subscriptionsArch.setMiddleName(subscriptions.getMiddleName());
            subscriptionsArch.setFirstName(subscriptions.getFirstName());
            subscriptionsArch.setSurname(subscriptions.getSurname());
            subscriptionsArch.setPassword(subscriptions.getPassword());
            subscriptionsArch.setPersonId(subscriptions.getPersonId());
            subscriptionsArch.setStatus(subscriptions.getStatus());
            subscriptionsArch.setMobileNumber(subscriptions.getMobileNumber());
            subscriptionsArch.setSubscriptionDate(subscriptions.getSubscriptionDate());
            subscriptionsArch.setUnSubscriptionDate(LocalDateTime.now());
            subscriptionsArch.setLastUpdatedDate(LocalDateTime.now());
            subscriptionsArch.setNHIFMember(subscriptions.isNHIFMember());

            List<BeneficiariesArch> beneficiariesArchList = new ArrayList<>();
            if(!subscriptions.getBeneficiaries().isEmpty()) {
                for (Beneficiaries b: subscriptions.getBeneficiaries()) {
                    BeneficiariesArch beneficiariesArch = getBeneficiariesArch(b, subscriptionsArch);
                    beneficiariesArch.setDateRemoved(LocalDateTime.now());
                    beneficiariesArchList.add(beneficiariesArch);
                }
            }

            beneficiaryRepositoryArch.saveAll(beneficiariesArchList);
            repository.save(subscriptionsArch);
        } catch (Exception e) {
            log.error("Exception while archiving subscription at {}", new Date());
        }
    }

    private static BeneficiariesArch getBeneficiariesArch(Beneficiaries b, SubscriptionsArch subscriptionsArch) {
        BeneficiariesArch beneficiariesArch = new BeneficiariesArch();
        // beneficiariesArch.setSubscriptions(subscriptionsArch);
        beneficiariesArch.setDateRemoved(beneficiariesArch.getDateRemoved());
        beneficiariesArch.setLastUpdate(beneficiariesArch.getLastUpdate());
        beneficiariesArch.setFirstName(b.getFirstName());
        beneficiariesArch.setMiddleName(b.getMiddleName());
        beneficiariesArch.setSurname(b.getSurname());
        beneficiariesArch.setPersonId(b.getPersonId());
        beneficiariesArch.setStatus(b.getStatus());
        return beneficiariesArch;
    }

}
