package com.sebin.uhc.entities.onboarding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(name = "beneficiarySponsorCSTR", columnNames = {"personId", "subscription_id"}))
public class Beneficiaries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String personId;
    private String firstName;
    private String middleName;
    private String surname;
    private String memberNumber;
    private String dateOfBirth;
    private boolean isInNHS=true;
    private LocalDateTime dateCreated = LocalDateTime.now();
    private LocalDateTime lastUpdate = LocalDateTime.now();
    private LocalDateTime dateRemoved;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Subscriptions subscriptions;

    public Beneficiaries(String personId, String firstName, String middleName, String surname, String memberNumber, String status) {
        this.personId = personId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.surname = surname;
        this.memberNumber = memberNumber;
        this.status = status;
    }
}
