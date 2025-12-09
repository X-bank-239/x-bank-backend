package com.example.xbankbackend.dtos.requests;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateBankAccountRequest {

    @NotNull(message = "userId cannot be null")
    private UUID userId;

    @NotNull(message = "currency cannot be null")
    private CurrencyType currency;

    @NotNull(message = "accountType cannot be null")
    private BankAccountType accountType;
}
