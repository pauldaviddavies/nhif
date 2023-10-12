package com.sebin.uhc.models.requests.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    private String api_key;
    private String mobile;
    private String shortcode="NHIF";
    private String message;
}
