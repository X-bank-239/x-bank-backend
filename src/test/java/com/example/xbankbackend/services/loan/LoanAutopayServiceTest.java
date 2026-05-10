package com.example.xbankbackend.services.loan;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.LoanStatus;
import com.example.xbankbackend.models.Loan;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.LoanRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.repositories.UserRepository;
import com.example.xbankbackend.services.external.notification.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanAutopayServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionsRepository transactionsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailSender emailSender;
    @Mock
    private LoanValidationService loanValidationService;

    @InjectMocks
    private LoanAutopayService loanAutopayService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loanAutopayService, "repaymentCalculationService", new LoanRepaymentCalculationService());
    }

    @Test
    void processDueLoans_updatesRepaymentStateForRegularAutopay() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDate paymentDate = LocalDate.of(2026, 5, 5);

        Loan loan = Loan.builder()
                .loanId(loanId)
                .userId(userId)
                .debitAccountId(senderId)
                .serviceAccountId(serviceId)
                .autopayEnabled(true)
                .currency(CurrencyType.RUB)
                .annualInterestRate(new BigDecimal("0.12"))
                .monthlyPayment(new BigDecimal("1000.0000"))
                .outstandingPrincipal(new BigDecimal("10000.0000"))
                .nextPaymentDate(paymentDate)
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findDueActiveLoans(paymentDate)).thenReturn(List.of(loan));
        User user = new User();
        user.setEmail("user@test.com");
        when(userRepository.getUser(userId)).thenReturn(user);
        loanAutopayService.processDueLoans(paymentDate);

        verify(loanValidationService).validateAutopayHasEnoughFunds(senderId, new BigDecimal("1000.0000"));
        verify(transactionsRepository).addTransaction(any());
        verify(bankAccountRepository).decreaseBalance(senderId, new BigDecimal("1000.0000"));
        verify(bankAccountRepository).increaseBalance(serviceId, new BigDecimal("1000.0000"));
        verify(loanRepository).updateRepaymentState(eq(loanId), eq(new BigDecimal("9100.0000")), eq(paymentDate.plusMonths(1)));
        verify(loanRepository, never()).close(eq(loanId), any(OffsetDateTime.class));
        verify(emailSender).sendLoanRepaymentReceipt(eq("user@test.com"), eq(loanId), eq(new BigDecimal("1000.0000")),
                eq(new BigDecimal("9100.0000")), eq(paymentDate.plusMonths(1)), eq(false));
    }

    @Test
    void processDueLoans_closesLoanWhenOutstandingCleared() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDate paymentDate = LocalDate.of(2026, 5, 5);

        Loan loan = Loan.builder()
                .loanId(loanId)
                .userId(userId)
                .debitAccountId(senderId)
                .serviceAccountId(serviceId)
                .autopayEnabled(true)
                .currency(CurrencyType.RUB)
                .annualInterestRate(new BigDecimal("0.12"))
                .monthlyPayment(new BigDecimal("1000.0000"))
                .outstandingPrincipal(new BigDecimal("100.0000"))
                .nextPaymentDate(paymentDate)
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findDueActiveLoans(paymentDate)).thenReturn(List.of(loan));
        User user = new User();
        user.setEmail("user@test.com");
        when(userRepository.getUser(userId)).thenReturn(user);
        loanAutopayService.processDueLoans(paymentDate);

        verify(loanValidationService).validateAutopayHasEnoughFunds(senderId, new BigDecimal("1000.0000"));
        verify(loanRepository).close(eq(loanId), any(OffsetDateTime.class));
        verify(loanRepository, never()).updateRepaymentState(eq(loanId), any(), any());
        verify(emailSender).sendLoanRepaymentReceipt(eq("user@test.com"), eq(loanId), eq(new BigDecimal("1000.0000")),
                eq(new BigDecimal("0.0000")), eq(null), eq(true));
    }
}
