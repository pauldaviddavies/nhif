package com.sebin.uhc.models.requests.payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletBalanceRequest {
    private String idNumber;
    private String mobileNumber;
}
