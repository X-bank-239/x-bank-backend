package com.example.xbankbackend.models;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SavingsAccount {
    private UUID accountId;
    private BigDecimal accruedInterest;
    private BigDecimal interestRate;
    private LocalDate maturityDate;
    private LocalDate lastInterestCalculation;
    private boolean allowWithdrawal;
    private boolean allowTopup;
    private BigDecimal earlyWithdrawalPenalty;
    private String status;
    private boolean autoProlong;
}
