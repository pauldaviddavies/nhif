package com.sebin.uhc.models.responses.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoomaResp {
    public String responseCode;
    public String responseDesc;
}
