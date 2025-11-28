package com.example.xbankbackend.dtos.requests;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBankAccountRequest {

    @NotNull(message = "userId cannot be null")
    private UUID userId;

    @NotNull(message = "currency cannot be null")
    private CurrencyType currency;

    @NotNull(message = "accountType cannot be null")
    private BankAccountType accountType;
}
