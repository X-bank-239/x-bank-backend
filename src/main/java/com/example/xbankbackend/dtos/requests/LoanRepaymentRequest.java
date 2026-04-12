package com.example.xbankbackend.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LoanRepaymentRequest {
    @NotNull(message = "payerAccountId cannot be null")
    private UUID payerAccountId;

    @NotNull(message = "amount cannot be null")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;
}
