package com.sebin.uhc.models.responses.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse {
    private int status_code;
    private String status_desc;
    private String message_id;
}
