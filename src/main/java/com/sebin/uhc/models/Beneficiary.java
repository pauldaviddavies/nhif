package com.sebin.uhc.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Beneficiary {
    @JsonProperty("sponsorMobileNumber")
    private String sponsorMobileNumber;
    @JsonProperty("BeneficiaryIdOrPassportNumber")
    private String personId;
    private String firstName;
    private String middleName;
    private String surname;
    private String memberNumber;
    private String dateOfBirth;
}
