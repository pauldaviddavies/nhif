package com.sebin.uhc.entities.onboarding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sebin.uhc.commons.Statuses;
import com.sebin.uhc.entities.Wallet;
import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Subscriptions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String personId; // Passport or national ID
    private String mobileNumber;
    private String firstName;
    private String middleName;
    private String surname;
    private boolean isNHIFMember = false;
    private String dateOfBirth;
    private String gender;
    private String memberNumber;
    private String KcbMessageId;
    private String KcbExternalId;
    private boolean isInNHS=true;
    private boolean sentToKcb=false;
    @Column(nullable = false)
    @ColumnDefault("0")
    private Long sendToKcbRetries=0L;
    private String password;
    private String status = Statuses.ACTIVE.getStatus();
    private LocalDateTime subscriptionDate = LocalDateTime.now();
    private LocalDateTime lastUpdatedDate = LocalDateTime.now();
    private LocalDateTime bankNotificationDate;
    private LocalDateTime unSubscriptionDate;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription", referencedColumnName = "id")
    private Collection<OTPs> otPs;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "subscriptions", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    private Collection<Beneficiaries> beneficiaries;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "wallet", referencedColumnName = "id")
    private Wallet wallet;

    public Subscriptions(String mobileNumber, String personId, String firstName, String middleName, String surname,String status,boolean isNHIFMember, Wallet wallet) {
        this.mobileNumber = mobileNumber;
        this.personId = personId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.surname = surname;
        this.status = status;
        this.isNHIFMember = isNHIFMember;
        this.wallet = new Wallet();
    }
}
