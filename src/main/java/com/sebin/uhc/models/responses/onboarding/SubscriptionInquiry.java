package com.sebin.uhc.models.responses.onboarding;

import com.sebin.uhc.models.Subscription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubscriptionInquiry {
    private boolean isSubscribed;
    private boolean isPinSet;
    private Subscription profile;
}
