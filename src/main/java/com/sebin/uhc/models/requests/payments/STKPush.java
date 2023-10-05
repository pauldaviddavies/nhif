package com.sebin.uhc.models.requests.payments;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class STKPush {
    public String phoneNumber;
    public String amount;
    public String invoiceNumber;
    public boolean sharedShortCode;
    public String orgShortCode;
    public String orgPassKey;
    public String callbackUrl;
    public String transactionDescription;
}
