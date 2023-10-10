package com.sebin.uhc.entities.payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FundsTransferRequests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idNumber;
    private String mobileNumber;
    private Double amount;
    private String description;
    private String purpose;
    private String beneficiaryIdOrPassportNumber;
    private String beneficiaryMemberNumber;
    private String referenceNumber;
    private String kcbFTTransactionID;
    private LocalDateTime dateCreated;
    private boolean sentToKcb=false;
    private boolean processed=false;
    private boolean paid=false;
    @Column(nullable = false)
    @ColumnDefault("0")
    private Long sendToKcbRetries=0L;
    private LocalDateTime dateSentToCB;
    private String responseStatus;
    private String statusDescription;
    private LocalDateTime responseDate;
    private String responseCode;
    private String statusMessage;
    private String errorsDescription;

    @Column(columnDefinition = "text")
    private String request;
    @Column(columnDefinition = "text")
    private String response;
}
