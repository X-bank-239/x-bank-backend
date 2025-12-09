package com.example.xbankbackend.dtos.requests;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.TransactionType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateTransactionRequest {

    @NotNull(message = "transactionType cannot be null")
    private TransactionType transactionType;

    @NotNull(message = "amount cannot be null")
    private Float amount;

    @NotNull(message = "currency cannot be blank")
    private CurrencyType currency;

    private UUID senderId;
    private UUID receiverId;
    private String comment;
}
