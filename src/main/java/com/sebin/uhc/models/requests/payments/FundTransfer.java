package com.sebin.uhc.models.requests.payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundTransfer {
//    public String companyCode;
//    public String transactionType;
//    public String debitAccountNumber;
//    public String creditAccountNumber;
//    public int debitAmount;
//    public String paymentDetails;
//    public String transactionReference;
//    public String currency;
//    public String beneficiaryDetails;
//    public String beneficiaryBankCode;
//    public String memberNumber;
//    public String memberName;

    private String requestId;
    private String nhifNo;
    private String amount;
    private String ftType="NHIF_PAYMENT";
    private String phoneNumber;
}
