package com.sebin.uhc.repositories.onboarding;

import com.sebin.uhc.entities.onboarding.SubscriptionsArch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionsRepositoryArch extends JpaRepository<SubscriptionsArch, Long> {
}
