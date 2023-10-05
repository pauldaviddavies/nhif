package com.sebin.uhc.models.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StatementRequest {
    private String idNumber;
    private String mobileNumber;
    private String statementType;
    private long numberOfMonths;
}
