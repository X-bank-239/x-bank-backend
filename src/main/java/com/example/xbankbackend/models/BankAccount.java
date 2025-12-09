package com.example.xbankbackend.models;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BankAccount {

    private UUID accountId;

    @NotNull(message = "userId cannot be null")
    private UUID userId;

    private Float balance;

    @NotNull(message = "currency cannot be null")
    private CurrencyType currency;

    @NotNull(message = "accountType cannot be null")
    private BankAccountType accountType;
}
