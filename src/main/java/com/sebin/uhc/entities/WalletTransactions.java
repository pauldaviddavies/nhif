package com.sebin.uhc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class WalletTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double amount;
    private String transactionType;
    private String fundSource;
    private String fundDestination;
    private String transactionId;
    private String referenceNumber;
    private String status;
    private double walletBalance;
    private String requestId;
    private String description;
    private LocalDateTime dateCreated = LocalDateTime.now();
    private LocalDateTime lastUpdatedOn = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    @Hidden
    @JsonIgnore
    private Wallet wallet;
}
