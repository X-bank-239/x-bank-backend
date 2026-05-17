package com.example.xbankbackend.dtos.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LoanRepaymentResult {
    BigDecimal newOutstanding;
    private LocalDate nextPaymentDate;
    private  boolean shouldCloseLoan;
}
