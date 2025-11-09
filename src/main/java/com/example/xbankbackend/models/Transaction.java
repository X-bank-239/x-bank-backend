package com.example.xbankbackend.models;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Transaction {
    @NotNull(message = "transactionType cannot be null")
    private TransactionType transactionType;

    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;

    @NotNull(message = "amount cannot be null")
    private Float amount;

    @NotNull(message = "currency cannot be blank")
    private CurrencyType currency;
    private OffsetDateTime transactionDate;
    private String comment;
}
