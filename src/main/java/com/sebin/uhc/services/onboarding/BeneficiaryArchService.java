package com.sebin.uhc.services.onboarding;

import com.sebin.uhc.entities.onboarding.BeneficiariesArch;
import com.sebin.uhc.entities.onboarding.SubscriptionsArch;
import com.sebin.uhc.repositories.onboarding.BeneficiaryRepositoryArch;
import com.sebin.uhc.repositories.onboarding.SubscriptionsRepositoryArch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BeneficiaryArchService {
    @Autowired
    private SubscriptionsRepositoryArch subscriptionsRepositoryArch;
    @Autowired
    private BeneficiaryRepositoryArch beneficiaryRepositoryArch;

    public void archiveSubscription(SubscriptionsArch subscriptionsArch) {
        subscriptionsRepositoryArch.save(subscriptionsArch);
    }

    public void archiveBeneficiary(BeneficiariesArch beneficiariesArch) {
        beneficiaryRepositoryArch.save(beneficiariesArch);
    }
}
