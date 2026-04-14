package com.example.xbankbackend.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "LoanRepaymentRequest", description = "Тело запроса погашения: сумма платежа. UUID кредитного счёта передаётся в пути URL (см. POST /loans/credit-accounts/{creditAccountId}/repay/...).")
public class LoanRepaymentRequest {
    @NotNull(message = "amount cannot be null")
    @Positive(message = "amount must be positive")
    @Schema(description = "Сумма платежа в валюте кредита.", example = "9025.83", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;
}
