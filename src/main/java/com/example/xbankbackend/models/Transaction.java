package com.example.xbankbackend.models;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.TransactionStatus;
import com.example.xbankbackend.enums.TransactionType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Transaction {

    @NotNull(message = "transactionType cannot be null")
    private TransactionType transactionType;
    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;

    @NotNull(message = "amount cannot be null")
    private BigDecimal amount;

    @NotNull(message = "currency cannot be blank")
    private CurrencyType currency;
    private OffsetDateTime transactionDate;
    private String comment;
    private TransactionStatus status;
}
