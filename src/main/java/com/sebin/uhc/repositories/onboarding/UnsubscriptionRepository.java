package com.sebin.uhc.repositories.onboarding;

import com.sebin.uhc.entities.onboarding.UnSubscriptionsRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UnsubscriptionRepository extends JpaRepository<UnSubscriptionsRequests, Long> {
    @Query(value = "select u from UnSubscriptionsRequests u where u.personId =:personId and u.status =:status")
    Collection<UnSubscriptionsRequests> findByPersonIdAndStatus(String personId, String status);
}
