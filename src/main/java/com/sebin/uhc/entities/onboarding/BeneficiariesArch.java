package com.sebin.uhc.entities.onboarding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiariesArch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String personId;
    private String firstName;
    private String middleName;
    private String surname;
    private LocalDateTime dateCreated = LocalDateTime.now();
    private LocalDateTime lastUpdate = LocalDateTime.now();
    private LocalDateTime dateRemoved;
    private String status;

    public BeneficiariesArch(String personId, String firstName, String middleName, String surname, String status) {
        this.personId = personId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.surname = surname;
        this.status = status;
    }
}
