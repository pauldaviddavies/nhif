package com.sebin.uhc.models.requests.payments;

import lombok.Data;

@Data
public class FundsTransferRequest {
    private String idNumber;
    private String mobileNumber;
    private String amount;
    private String beneficiaryIdOrPassportNumber;
    private String description;
    private String PIN;
}
