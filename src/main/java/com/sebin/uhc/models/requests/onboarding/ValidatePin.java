package com.sebin.uhc.models.requests.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ValidatePin {
    private String idNumber;
    private String mobileNumber;
    private String PIN;
}
