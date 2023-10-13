package com.sebin.uhc.entities.onboarding;

import com.sebin.uhc.commons.Statuses;
import com.sebin.uhc.models.Gender;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class UnSubscriptionsRequests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String personId;
    private String requestTo;
    private String callBackUrl;
    private LocalDateTime requestDate = LocalDateTime.now();
    private LocalDateTime lastUpdate = LocalDateTime.now();
    private String status = Statuses.PENDING.getStatus();
    private String requestId;


    private boolean isNHIFMember = false;
    private String dateOfBirth;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String memberNumber;
    private String mobileNumber;
    private String KcbMessageId;
    private String KcbExternalId;
    private boolean isInNHS=true;
    private String password;
    private LocalDateTime subscriptionDate = LocalDateTime.now();

    private String KcbResponse;
    private boolean sentToKcb=false;
    private boolean processed=false;
    @Column(nullable = false)
    @ColumnDefault("0")
    private Long sendToKcbRetries=0L;
    private LocalDateTime bankNotificationDate;


    public UnSubscriptionsRequests(String personId, String requestTo, String callBackUrl, LocalDateTime requestDate, LocalDateTime lastUpdate, String status, String requestId) {
        this.personId = personId;
        this.requestTo = requestTo;
        this.callBackUrl = callBackUrl;
        this.requestDate = requestDate;
        this.lastUpdate = lastUpdate;
        this.status = status;
        this.requestId = requestId;
    }

}
