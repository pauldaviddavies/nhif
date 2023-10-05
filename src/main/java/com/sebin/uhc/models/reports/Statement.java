package com.sebin.uhc.models.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Statement {
    private double amount;
    private double balance;
    private String type;
    private LocalDateTime transactionDate;
}
