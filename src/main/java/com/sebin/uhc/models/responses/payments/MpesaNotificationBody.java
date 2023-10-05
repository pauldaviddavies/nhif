package com.sebin.uhc.models.responses.payments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MpesaNotificationBody {
    @JsonProperty("MerchantRequestID")
    public String MerchantRequestID;
    @JsonProperty("ResponseCode")
    public String ResponseCode;
    @JsonProperty("CustomerMessage")
    public String CustomerMessage;
    @JsonProperty("CheckoutRequestID")
    public boolean CheckoutRequestID;
    @JsonProperty("ResponseDescription")
    public String ResponseDescription;
}
