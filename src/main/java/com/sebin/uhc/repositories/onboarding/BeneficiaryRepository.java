package com.sebin.uhc.repositories.onboarding;

import com.sebin.uhc.entities.onboarding.Beneficiaries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiaries,Long> {

    Optional<Beneficiaries> findByPersonIdAndStatus(String IdOrPassport,String status);

    @Query(value = "select * from beneficiaries where person_id=?1 and status=?2 and subscription_id=?3 order by id desc limit 1",nativeQuery = true)
    Optional<Beneficiaries> findBeneficiary(String IdOrPassport,String status,Long subscription_id);
}
