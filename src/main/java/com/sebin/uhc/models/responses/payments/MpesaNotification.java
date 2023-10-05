package com.sebin.uhc.models.responses.payments;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MpesaNotification {
    public MpesaNotificationBody response;
    public MpesaNotificationHeader header;
}
