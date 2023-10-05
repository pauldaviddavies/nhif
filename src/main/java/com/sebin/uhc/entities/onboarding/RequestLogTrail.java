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
@NoArgsConstructor
@AllArgsConstructor
public class RequestLogTrail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String requestId;
    private String requestType;
    @Column(columnDefinition = "text")
    private String description;
    private LocalDateTime dateRequested = LocalDateTime.now();
    private LocalDateTime lastUpdated = LocalDateTime.now();
    private String responseMessage;
    private String hasCallBack;
    private LocalDateTime callbackTime;
    private String callbackMessage;
    private long callBackId;
    private String requestingChannel;
    private String syncAsync;
    private String requestingIP;
    private String requestingPort;
    private String requestingUrl;
}
