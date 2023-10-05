package com.sebin.uhc.models.requests.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangePin {
    private String mobileNumber;
    private String PIN;
    private String newPIN;
    private String confirmNewPIN;
}
