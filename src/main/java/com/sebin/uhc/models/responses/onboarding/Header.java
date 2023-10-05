package com.sebin.uhc.models.responses.onboarding;

import com.sebin.uhc.commons.Messages;
import com.sebin.uhc.commons.ResponseCodes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Header {
    private boolean success = false;
    private String responseCode = ResponseCodes.GENERAL.getCode();
    private String message = Messages.GENERAL_FAILURE;

    public Header(final String message) {
        this.message = message;
    }

    public Header(String message, String responseCode) {
        this.message = message;
        this.responseCode = responseCode;
    }

}
