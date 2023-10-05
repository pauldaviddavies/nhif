package com.sebin.uhc.entities.onboarding;

import com.sebin.uhc.commons.Statuses;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
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
