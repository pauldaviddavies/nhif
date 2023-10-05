package com.sebin.uhc.repositories.payments;

import com.sebin.uhc.entities.Wallet;
import com.sebin.uhc.entities.WalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactions, Long> {
    List<WalletTransactions> findTop5ByWalletOrderByDateCreatedDesc(Wallet wallet);
    List<WalletTransactions> findByWalletAndDateCreatedBetweenOrderByDateCreatedDesc(Wallet wallet, LocalDateTime from, LocalDateTime to);
    Optional<List<WalletTransactions>> findByReferenceNumber(String referenceNumber);
}
