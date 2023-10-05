package com.sebin.uhc.entities.payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class MpesaRequests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idNumber;
    private String mobileNumber;
    private String amount;
    private String description;
    private String referenceNumber;
    private LocalDateTime dateCreated;
    private boolean accepted=false;
    private boolean paid=false;
    @Column(columnDefinition = "text")
    private String request;
    @Column(columnDefinition = "text")
    private String response;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private String responseDescription;
    private String responseStatus;
}
