package com.sebin.uhc.repositories.payments;

import com.fasterxml.jackson.annotation.OptBoolean;
import com.sebin.uhc.entities.payments.FundsTransferRequests;
import com.sebin.uhc.models.requests.payments.FundsTransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FundsTransferRequestsRepository extends JpaRepository<FundsTransferRequests,Long> {
    List<FundsTransferRequests> findByProcessed(boolean processed);
    @Query(value = "select * from funds_transfer_requests where mobile_number=?1 and amount =?2 and beneficiary_id_or_passport_number=?3 and purpose=?4 and  processed=?5 order by id desc limit 1",nativeQuery = true)
    Optional<FundsTransferRequests> findPendingTransactions(String mobileNumber, Double amount, String  beneficiaryIdOrPassportNumber, String purpose, boolean processed);
}
