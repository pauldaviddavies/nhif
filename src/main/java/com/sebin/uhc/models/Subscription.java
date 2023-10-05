package com.sebin.uhc.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Subscription {
    @JsonProperty("IdOrPassportNumber")
    private String personId;
    private String mobileNumber;
    private String firstName;
    private String middleName;
    private String surname;
    @JsonProperty("isNHIFMember")
    private boolean isNHIFMember;
    private String memberNumber;
    private String dateOfBirth;
    private String gender;
}
