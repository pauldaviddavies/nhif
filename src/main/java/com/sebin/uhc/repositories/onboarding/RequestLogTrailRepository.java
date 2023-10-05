package com.sebin.uhc.repositories.onboarding;

import com.sebin.uhc.entities.onboarding.RequestLogTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLogTrailRepository extends JpaRepository<RequestLogTrail, Long> {
}
