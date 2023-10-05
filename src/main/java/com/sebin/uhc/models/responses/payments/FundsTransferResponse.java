package com.sebin.uhc.models.responses.payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundsTransferResponse {
    public String requestId;
    public String statusCode;
    public String statusMessage;
    public String statusDescription;
    public ResponsePayload responsePayload;
    public List<String> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponsePayload{
        public double amount;
        public String beneficiaryName;
        public String nhifNo;
        public String ftTransactionID;
    }
}
