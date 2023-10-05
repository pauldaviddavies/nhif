package com.sebin.uhc.repositories.payments;

import com.sebin.uhc.entities.payments.MpesaRequests;
import com.sebin.uhc.entities.payments.Token;
import com.sebin.uhc.models.requests.payments.Mpesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MpesaRequestsRepository extends JpaRepository<MpesaRequests,Long> {
    Optional<MpesaRequests> findByReferenceNumber(String referenceNumber);
}
