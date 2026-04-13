package com.example.xbankbackend.models;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.LoanStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Loan {
    private UUID loanId;
    private UUID userId;
    private UUID creditAccountId;
    private UUID serviceAccountId;
    private CurrencyType currency;
    private BigDecimal principalAmount;
    private BigDecimal annualInterestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal outstandingPrincipal;
    private LocalDate nextPaymentDate;
    private LoanStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime closedAt;
}
