package com.sebin.uhc.entities.onboarding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OTPs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String OTP;
    private LocalDateTime dateCreated = LocalDateTime.now();
    private LocalDateTime expiryDate;
    private boolean isUtilized;
    private LocalDateTime dateUtilized;
}
