package com.sebin.uhc.models.requests.payments;

import lombok.Data;

@Data
public class TokenRequest {
    private String grant_type;
    private String username;
    private String password;
}
