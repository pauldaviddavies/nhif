package com.sebin.uhc.repositories;

import com.sebin.uhc.entities.notifications.Sms;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsRepository extends JpaRepository<Sms, Long> {
    List<Sms> findByProcessedAndSendretriesLessThan(boolean sent, int maxSendRetries);
}
