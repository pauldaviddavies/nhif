package com.sebin.uhc.models.requests.payments;

import lombok.Data;

@Data
public class Mpesa {
    private String idNumber;
    private String mobileNumber;
    private String amount;
    private String description;
}
