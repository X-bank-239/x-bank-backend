package com.example.xbankbackend.services.loan;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class LoanRepaymentCalculationService {
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");

    public LoanRepaymentResult calculateMonthlyRepaymentResult(BigDecimal outstandingPrincipal, BigDecimal annualRate,
                                                               BigDecimal paymentAmount, LocalDate nextPaymentDate) {
        BigDecimal currentOutstanding = scaleMoney(outstandingPrincipal);
        BigDecimal monthlyInterest = scaleMoney(currentOutstanding.multiply(monthlyRate(annualRate)));
        BigDecimal principalReduction = scaleMoney(paymentAmount.subtract(monthlyInterest));
        if (principalReduction.compareTo(BigDecimal.ZERO) < 0) {
            principalReduction = BigDecimal.ZERO;
        }

        BigDecimal newOutstanding = scaleMoney(currentOutstanding.subtract(principalReduction));
        LocalDate newNextPaymentDate = nextPaymentDate.plusMonths(1);
        boolean shouldCloseLoan = newOutstanding.compareTo(BigDecimal.ZERO) <= 0;

        return new LoanRepaymentResult(newOutstanding, newNextPaymentDate, shouldCloseLoan);
    }

    public BigDecimal scaleMoney(BigDecimal amount) {
        return amount.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal monthlyRate(BigDecimal annualRate) {
        return annualRate.divide(MONTHS_IN_YEAR, 16, RoundingMode.HALF_UP);
    }

    public record LoanRepaymentResult(BigDecimal newOutstanding, LocalDate nextPaymentDate, boolean shouldCloseLoan) {
    }
}
