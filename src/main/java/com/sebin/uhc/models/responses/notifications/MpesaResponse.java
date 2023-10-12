package com.sebin.uhc.models.responses.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpesaResponse {

    public Header header;
    public RequestPayload requestPayload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalData{
        public NotificationData notificationData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header{
        public String messageID;
        public String originatorConversationID;
        public String channelCode;
        public String timeStamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationData{
        public String businessKey;
        public String businessKeyType;
        public String debitMSISDN;
        public String transactionAmt;
        public String transactionDate;
        public String transactionID;
        public String firstName;
        public String middleName;
        public String lastName;
        public String currency;
        public String narration;
        public String transactionType;
        public String balance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryData{
        public String businessKey;
        public String businessKeyType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestPayload{
        public PrimaryData primaryData;
        public AdditionalData additionalData;
    }

}
