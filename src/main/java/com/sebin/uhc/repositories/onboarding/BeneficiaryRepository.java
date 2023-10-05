package com.sebin.uhc.repositories.onboarding;

import com.sebin.uhc.entities.onboarding.Beneficiaries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiaries,Long> {
    Optional<Beneficiaries> findByPersonIdAndStatus(String IdOrPassport,String status);
}
