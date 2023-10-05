package com.sebin.uhc.models.requests.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SponsorBeneficiary {
    private String sponsorMobileNumber;
    private String BeneficiaryIdOrPassportNumber;
}
