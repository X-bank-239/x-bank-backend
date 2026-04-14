package com.example.xbankbackend.dtos.responses;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class LoanResponse {
    private UUID loanId;

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
}
