package com.example.xbankbackend.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionCategory {
    private String code;
    private String displayName;
    private String colorCode;
    private Boolean isActive;
}
