package com.example.xbankbackend.dtos.requests;

import com.example.xbankbackend.enums.CurrencyType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateCurrencyRateRequest {

    @NotNull(message = "currency cannot be null")
    private CurrencyType currency;

    @NotNull(message = "rate cannot be null")
    @DecimalMin("0.01")
    private BigDecimal rate;

    @NotNull(message = "date cannot be null")
    private LocalDate date;

    private String comment;
}
