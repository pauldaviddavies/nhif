package com.sebin.uhc.repositories;

import com.sebin.uhc.entities.notifications.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsRepository extends JpaRepository<Sms, Long> {
}
