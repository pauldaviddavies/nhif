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
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionsArch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String personId; // Passport or national ID
    private String mobileNumber;
    private String firstName;
    private String middleName;
    private String surname;
    private boolean isNHIFMember = false;
    private String password;
    private String status = Statuses.ACTIVE.getStatus();
    private LocalDateTime subscriptionDate = LocalDateTime.now();
    private LocalDateTime lastUpdatedDate = LocalDateTime.now();
    private LocalDateTime unSubscriptionDate;

    public SubscriptionsArch(String mobileNumber, String personId, String firstName, String middleName, String surname, String status, boolean isNHIFMember) {
        this.mobileNumber = mobileNumber;
        this.personId = personId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.surname = surname;
        this.status = status;
        this.isNHIFMember = isNHIFMember;
    }
}
