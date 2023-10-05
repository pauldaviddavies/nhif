package com.sebin.uhc.models.requests.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetPin {
    private String mobileNumber;
    private String PIN;
    private String confirmPIN;
}
