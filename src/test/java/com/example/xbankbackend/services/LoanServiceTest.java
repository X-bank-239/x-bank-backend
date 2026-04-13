package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.LoanRepaymentRequest;
import com.example.xbankbackend.dtos.responses.LoanPaymentAmountResponse;
import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.LoanStatus;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
import com.example.xbankbackend.exceptions.LoanRepaymentAmountMismatchException;
import com.example.xbankbackend.mappers.LoanMapper;
import com.example.xbankbackend.models.Loan;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.LoanRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    private static final UUID SERVICE_ACCOUNT_ID = UUID.fromString("00000000-0000-4000-8000-000000000002");

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loanService, "serviceAccountId", SERVICE_ACCOUNT_ID);
        ReflectionTestUtils.setField(loanService, "annualRate", new BigDecimal("0.15"));
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
        UUID creditAccountId = UUID.randomUUID();
        UUID loanServiceAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setUserId(userId);
        loan.setCreditAccountId(creditAccountId);
        loan.setServiceAccountId(loanServiceAccountId);
        loan.setMonthlyPayment(new BigDecimal("1000.0000"));
        loan.setOutstandingPrincipal(new BigDecimal("10000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setCurrency(CurrencyType.RUB);

        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setAmount(new BigDecimal("900.0000"));

        when(bankAccountRepository.exists(creditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, userId))
                .thenReturn(Optional.of(loan));
        when(bankAccountRepository.getUserId(creditAccountId)).thenReturn(userId);
        when(bankAccountRepository.getAccountType(creditAccountId)).thenReturn(BankAccountType.CREDIT);
        when(bankAccountRepository.getCurrency(creditAccountId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.isActive(creditAccountId)).thenReturn(true);

        assertThrows(LoanRepaymentAmountMismatchException.class, () -> loanService.repayMonthly(creditAccountId, request, userId));
    }

    @Test
    void repayMonthly_shouldUpdateOutstandingWhenPaymentMatchesAnnuity() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID creditAccountId = UUID.randomUUID();
        UUID loanServiceAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setUserId(userId);
        loan.setCreditAccountId(creditAccountId);
        loan.setServiceAccountId(loanServiceAccountId);
        loan.setTermMonths(12);
        loan.setMonthlyPayment(new BigDecimal("1000.0000"));
        loan.setOutstandingPrincipal(new BigDecimal("10000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNextPaymentDate(LocalDate.of(2026, 5, 13));
        loan.setCurrency(CurrencyType.RUB);

        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setAmount(new BigDecimal("1000.0000"));

        when(bankAccountRepository.exists(creditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, userId)).thenReturn(Optional.of(loan));
        when(bankAccountRepository.getUserId(creditAccountId)).thenReturn(userId);
        when(bankAccountRepository.getAccountType(creditAccountId)).thenReturn(BankAccountType.CREDIT);
        when(bankAccountRepository.getCurrency(creditAccountId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.isActive(creditAccountId)).thenReturn(true);
        when(bankAccountRepository.getBalance(creditAccountId)).thenReturn(new BigDecimal("50000.0000"));
        when(bankAccountRepository.isActive(loanServiceAccountId)).thenReturn(true);

        LoanResponse mapped = new LoanResponse();
        when(loanMapper.loanToResponse(any(Loan.class))).thenReturn(mapped);

        LoanResponse result = loanService.repayMonthly(creditAccountId, request, userId);

        assertEquals(mapped, result);
        verify(loanRepository).updateRepaymentState(eq(loanId), eq(new BigDecimal("9125.0000")), eq(LocalDate.of(2026, 6, 13)));
        verify(loanRepository, never()).close(eq(loanId), any(OffsetDateTime.class));
    }

    @Test
    void repayMonthly_shouldCloseLoanWhenOutstandingClearedByPayment() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID creditAccountId = UUID.randomUUID();
        UUID loanServiceAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setUserId(userId);
        loan.setCreditAccountId(creditAccountId);
        loan.setServiceAccountId(loanServiceAccountId);
        loan.setTermMonths(12);
        loan.setMonthlyPayment(new BigDecimal("1000.0000"));
        loan.setOutstandingPrincipal(new BigDecimal("100.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setCurrency(CurrencyType.RUB);

        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setAmount(new BigDecimal("1000.0000"));

        when(bankAccountRepository.exists(creditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, userId)).thenReturn(Optional.of(loan));
        when(bankAccountRepository.getUserId(creditAccountId)).thenReturn(userId);
        when(bankAccountRepository.getAccountType(creditAccountId)).thenReturn(BankAccountType.CREDIT);
        when(bankAccountRepository.getCurrency(creditAccountId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.isActive(creditAccountId)).thenReturn(true);
        when(bankAccountRepository.getBalance(creditAccountId)).thenReturn(new BigDecimal("50000.0000"));
        when(bankAccountRepository.isActive(loanServiceAccountId)).thenReturn(true);

        LoanResponse mapped = new LoanResponse();
        when(loanMapper.loanToResponse(any(Loan.class))).thenReturn(mapped);

        LoanResponse result = loanService.repayMonthly(creditAccountId, request, userId);

        assertEquals(mapped, result);
        verify(loanRepository).close(eq(loanId), any(OffsetDateTime.class));
        verify(loanRepository, never()).updateRepaymentState(any(), any(), any());
    }

    @Test
    void repayMonthly_shouldThrowInsufficientFundsWhenCreditBalanceTooLow() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID creditAccountId = UUID.randomUUID();
        UUID loanServiceAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setUserId(userId);
        loan.setCreditAccountId(creditAccountId);
        loan.setServiceAccountId(loanServiceAccountId);
        loan.setMonthlyPayment(new BigDecimal("1000.0000"));
        loan.setOutstandingPrincipal(new BigDecimal("10000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setCurrency(CurrencyType.RUB);

        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setAmount(new BigDecimal("1000.0000"));

        when(bankAccountRepository.exists(creditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, userId)).thenReturn(Optional.of(loan));
        when(bankAccountRepository.getUserId(creditAccountId)).thenReturn(userId);
        when(bankAccountRepository.getAccountType(creditAccountId)).thenReturn(BankAccountType.CREDIT);
        when(bankAccountRepository.getCurrency(creditAccountId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.isActive(creditAccountId)).thenReturn(true);
        when(bankAccountRepository.getBalance(creditAccountId)).thenReturn(new BigDecimal("100.0000"));

        assertThrows(InsufficientFundsException.class, () -> loanService.repayMonthly(creditAccountId, request, userId));
    }

    @Test
    void repayEarly_shouldCloseLoanWhenAmountEqualsOutstandingTimesTermMonths() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID creditAccountId = UUID.randomUUID();
        UUID loanServiceAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setUserId(userId);
        loan.setCreditAccountId(creditAccountId);
        loan.setServiceAccountId(loanServiceAccountId);
        loan.setTermMonths(12);
        loan.setOutstandingPrincipal(new BigDecimal("1000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setCurrency(CurrencyType.RUB);

        BigDecimal required = new BigDecimal("1000.0000").multiply(new BigDecimal("12")).setScale(4, RoundingMode.HALF_UP);

        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setAmount(required);

        when(bankAccountRepository.exists(creditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, userId)).thenReturn(Optional.of(loan));
        when(bankAccountRepository.getUserId(creditAccountId)).thenReturn(userId);
        when(bankAccountRepository.getAccountType(creditAccountId)).thenReturn(BankAccountType.CREDIT);
        when(bankAccountRepository.getCurrency(creditAccountId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.isActive(creditAccountId)).thenReturn(true);
        when(bankAccountRepository.getBalance(creditAccountId)).thenReturn(required.add(new BigDecimal("1")));
        when(bankAccountRepository.isActive(loanServiceAccountId)).thenReturn(true);

        LoanResponse mapped = new LoanResponse();
        when(loanMapper.loanToResponse(any(Loan.class))).thenReturn(mapped);

        LoanResponse result = loanService.repayEarly(creditAccountId, request, userId);

        assertEquals(mapped, result);
        verify(loanRepository).close(eq(loanId), any(OffsetDateTime.class));
    }

    @Test
    void repayEarly_shouldThrowInsufficientFundsWhenCreditBalanceTooLow() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID creditAccountId = UUID.randomUUID();
        UUID loanServiceAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setUserId(userId);
        loan.setCreditAccountId(creditAccountId);
        loan.setServiceAccountId(loanServiceAccountId);
        loan.setTermMonths(12);
        loan.setOutstandingPrincipal(new BigDecimal("1000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setCurrency(CurrencyType.RUB);

        BigDecimal required = new BigDecimal("1000.0000").multiply(new BigDecimal("12")).setScale(4, RoundingMode.HALF_UP);

        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setAmount(required);

        when(bankAccountRepository.exists(creditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, userId)).thenReturn(Optional.of(loan));
        when(bankAccountRepository.getUserId(creditAccountId)).thenReturn(userId);
        when(bankAccountRepository.getAccountType(creditAccountId)).thenReturn(BankAccountType.CREDIT);
        when(bankAccountRepository.getCurrency(creditAccountId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.isActive(creditAccountId)).thenReturn(true);
        when(bankAccountRepository.getBalance(creditAccountId)).thenReturn(required.subtract(new BigDecimal("0.0001")));

        assertThrows(InsufficientFundsException.class, () -> loanService.repayEarly(creditAccountId, request, userId));
    }

    @Test
    void repayMonthly_shouldThrowAccessDeniedWhenPathCreditAccountDoesNotMatchLoan() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID pathCreditAccountId = UUID.randomUUID();
        UUID loanCreditAccountId = UUID.randomUUID();
        UUID loanServiceAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setUserId(userId);
        loan.setCreditAccountId(loanCreditAccountId);
        loan.setServiceAccountId(loanServiceAccountId);
        loan.setMonthlyPayment(new BigDecimal("1000.0000"));
        loan.setOutstandingPrincipal(new BigDecimal("10000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setCurrency(CurrencyType.RUB);

        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setAmount(new BigDecimal("1000.0000"));

        when(bankAccountRepository.exists(pathCreditAccountId)).thenReturn(true);
        when(bankAccountRepository.exists(loanCreditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(pathCreditAccountId, userId)).thenReturn(Optional.of(loan));
        when(bankAccountRepository.getUserId(loanCreditAccountId)).thenReturn(userId);
        when(bankAccountRepository.getAccountType(loanCreditAccountId)).thenReturn(BankAccountType.CREDIT);
        when(bankAccountRepository.getCurrency(loanCreditAccountId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.isActive(loanCreditAccountId)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> loanService.repayMonthly(pathCreditAccountId, request, userId));
    }

    @Test
    void fullPaymentCost_shouldReturnOutstandingPrincipalMultipliedByTermMonths() {
        UUID userId = UUID.randomUUID();
        UUID creditAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setCreditAccountId(creditAccountId);
        loan.setTermMonths(12);
        loan.setOutstandingPrincipal(new BigDecimal("1000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setCurrency(CurrencyType.RUB);
        loan.setMonthlyPayment(new BigDecimal("100.0000"));

        when(bankAccountRepository.exists(creditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, userId)).thenReturn(Optional.of(loan));

        LoanPaymentAmountResponse response = loanService.fullPaymentCost(creditAccountId, userId);

        assertEquals(new BigDecimal("12000.0000"), response.getAmount().setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    void monthlyPaymentCost_shouldReturnAnnuityMonthlyPayment() {
        UUID userId = UUID.randomUUID();
        UUID creditAccountId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setCreditAccountId(creditAccountId);
        loan.setTermMonths(12);
        loan.setOutstandingPrincipal(new BigDecimal("1000.0000"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setCurrency(CurrencyType.RUB);
        loan.setMonthlyPayment(new BigDecimal("9025.8300"));

        when(bankAccountRepository.exists(creditAccountId)).thenReturn(true);
        when(loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, userId)).thenReturn(Optional.of(loan));

        LoanPaymentAmountResponse response = loanService.monthlyPaymentCost(creditAccountId, userId);

        assertEquals(new BigDecimal("9025.8300"), response.getAmount());
    }
}
