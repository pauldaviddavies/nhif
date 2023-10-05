package com.sebin.uhc.repositories.payments;

import com.sebin.uhc.entities.Wallet;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
