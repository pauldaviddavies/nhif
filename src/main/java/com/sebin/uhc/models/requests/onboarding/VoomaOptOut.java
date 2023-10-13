package com.sebin.uhc.models.requests.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VoomaOptOut {
    private String messageId;
    private String msisdn;
    private String externalId;
    private String idType="01";
    private String idNumber;
}

