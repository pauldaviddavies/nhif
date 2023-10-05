package com.sebin.uhc.models.responses.payments;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MpesaNotificationHeader {
    public String statusDescription;
    public String statusCode;
}
