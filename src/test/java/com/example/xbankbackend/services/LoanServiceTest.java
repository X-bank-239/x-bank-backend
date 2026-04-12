package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.LoanRepaymentRequest;
import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.LoanStatus;
import com.example.xbankbackend.exceptions.LoanRepaymentAmountMismatchException;
import com.example.xbankbackend.mappers.LoanMapper;
import com.example.xbankbackend.models.Loan;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.LoanRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private LoanMapper loanMapper;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, bankAccountRepository, transactionsRepository, loanMapper);
        ReflectionTestUtils.setField(loanService, "serviceAccountId",
                UUID.fromString("00000000-0000-4000-8000-000000000002"));
    }

    @Test
    void calculateAnnuityPayment_shouldUseFifteenPercentAnnualRate() {
        BigDecimal payment = loanService.calculateAnnuityPayment(new BigDecimal("100000.00"), 12);
        assertEquals(new BigDecimal("9025.83"), payment.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void repayMonthly_shouldRejectArbitraryAmounts() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID payerAccountId = UUID.randomUUID();
        UUID serviceAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setUserId(userId);
        loan.setServiceAccountId(serviceAccountId);
        loan.setMonthlyPayment(new BigDecimal("1000.0000"));
        loan.setOutstandingPrincipal(new BigDecimal("10000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setCurrency(CurrencyType.RUB);

        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setPayerAccountId(payerAccountId);
        request.setAmount(new BigDecimal("900.0000"));

        when(loanRepository.exists(loanId)).thenReturn(true);
        when(loanRepository.get(loanId)).thenReturn(loan);
        when(bankAccountRepository.exists(payerAccountId)).thenReturn(true);
        when(bankAccountRepository.getUserId(payerAccountId)).thenReturn(userId);
        when(bankAccountRepository.getAccountType(payerAccountId)).thenReturn(BankAccountType.DEBIT);
        when(bankAccountRepository.getCurrency(payerAccountId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.isActive(payerAccountId)).thenReturn(true);

        assertThrows(LoanRepaymentAmountMismatchException.class, () -> loanService.repayMonthly(loanId, request, userId));
    }
}
