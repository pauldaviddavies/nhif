package com.sebin.uhc.entities.notifications;

import com.sebin.uhc.models.SmsContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String mobileNumber;
    @Column(columnDefinition = "TEXT")
    private String message;
    private SmsContext smsContext;
    private boolean sent;
    private LocalDateTime dateCreated;
    private LocalDateTime dateSent;
}
