package com.example.xbankbackend.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(name = "CreateLoanRequest", description = "Создание кредита наличными: зачисление и погашение через дебетовый счёт.")
public class CreateLoanRequest {
    @NotNull(message = "debitAccountId cannot be null")
    private UUID debitAccountId;

    @NotNull(message = "principalAmount cannot be null")
    @Positive(message = "principalAmount must be positive")
    private BigDecimal principalAmount;

    @NotNull(message = "termMonths cannot be null")
    @Positive(message = "termMonths must be positive")
    private Integer termMonths;
}
