package com.sebin.uhc.repositories.onboarding;

import com.sebin.uhc.entities.onboarding.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {
    Optional<Subscriptions> findByPersonId(String IdOrPassport);
    Optional<Subscriptions> findByMobileNumber(String mobileNumber);
    List<Subscriptions> findByProcessed(boolean processed);
}
