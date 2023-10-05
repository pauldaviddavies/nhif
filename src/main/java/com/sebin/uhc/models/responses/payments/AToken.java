package com.sebin.uhc.models.responses.payments;

import lombok.Data;

@Data
public class AToken {
    private String access_token;
    private Long expires_in;
}
