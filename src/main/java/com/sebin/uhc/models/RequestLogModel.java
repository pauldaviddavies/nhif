package com.sebin.uhc.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestLogModel {
    private String requestId;
    private String request = "";
    private String requestType = "";
    private String description = "";
    private LocalDateTime dateRequested = LocalDateTime.now();
    private LocalDateTime lastUpdated = LocalDateTime.now();
    private String responseMessage = "";
    private String hasCallBack = "";
    private LocalDateTime callbackTime;
    private String callbackMessage = "";
    private long callBackId = 0;
    private String requestingChannel = "";
    private String syncAsync = "";
    private String requestingIP = "";
    private String requestingPort = "";
    private String requestingUrl = "";
}
