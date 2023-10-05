package com.sebin.uhc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sebin.uhc.entities.onboarding.Subscriptions;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double amount;
    private LocalDateTime lastUpdateTime = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wallet")
    @Hidden
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    private List<WalletTransactions> walletTransactions;

    public Wallet(double amount, LocalDateTime lastUpdateTime, Subscriptions subscriptions, List<WalletTransactions> walletTransactions) {
        this.amount = amount;
        this.lastUpdateTime = lastUpdateTime;
        this.walletTransactions = walletTransactions;
    }
}
