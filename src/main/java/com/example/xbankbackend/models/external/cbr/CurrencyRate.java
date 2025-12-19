package com.example.xbankbackend.models.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CurrencyRate {

    @NotNull(message = "currency cannot be null")
    private CurrencyType currency;

    @NotNull(message = "rate cannot be null")
    private Float rate;

    @NotNull(message = "date cannot be null")
    private LocalDate date;

    @NotNull(message = "createdAt cannot be null")
    private LocalDateTime createdAt;
}
