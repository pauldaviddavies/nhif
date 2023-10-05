package com.sebin.uhc.repositories.onboarding;

import com.sebin.uhc.entities.onboarding.Beneficiaries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeneficiariesRepository extends JpaRepository<Beneficiaries, Long> {
}
