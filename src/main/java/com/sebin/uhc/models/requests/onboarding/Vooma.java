package com.sebin.uhc.models.requests.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Vooma {
    private String msisdn;
    private String firstName;
    private String middleName;
    private String lastName;
    private String dateOfBirth;
    private String idNumber;
    private String externalId;
    private String messageId;
    private String callBackURL;
    private String nhifMemberNo;

    private String idType="01";
    private String gender;
    private LoadInitiator initiator;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoadInitiator
    {
        private String identifier;
        private String identifierType="01";
    }

}

