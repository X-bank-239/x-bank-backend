package com.example.xbankbackend.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCurrencyRateRequest {

    @DecimalMin("0.01")
    private BigDecimal rate;

    private String comment;
}
