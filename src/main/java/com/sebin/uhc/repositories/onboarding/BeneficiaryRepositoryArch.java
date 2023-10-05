package com.sebin.uhc.repositories.onboarding;

import com.sebin.uhc.entities.onboarding.BeneficiariesArch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeneficiaryRepositoryArch extends JpaRepository<BeneficiariesArch,Long> {
}
