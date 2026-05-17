package com.example.xbankbackend.services.loan;

import com.example.xbankbackend.dtos.requests.CreateLoanRequest;
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
import com.example.xbankbackend.repositories.UserRepository;
import com.example.xbankbackend.services.external.notification.EmailSender;
import com.example.xbankbackend.services.FeeService;
import com.example.xbankbackend.services.bankAccount.BankAccountValidationService;
import com.example.xbankbackend.services.transaction.TransactionsService;
import com.example.xbankbackend.services.user.UserValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@DisplayName("Loan service")
class LoanServiceTest {

    private static final UUID SERVICE_ACCOUNT_ID = UUID.fromString("00000000-0000-4000-8000-000000000002");

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private LoanMapper loanMapper;
    @Mock
    private TransactionsService transactionsService;
    @Mock
    private UserValidationService userValidationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailSender emailSender;
    @Mock
    private FeeService feeService;

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        BankAccountValidationService accountValidationService = new BankAccountValidationService(bankAccountRepository, feeService);
        ReflectionTestUtils.setField(loanService, "loanValidationService",
                new LoanValidationService(bankAccountRepository, accountValidationService));
        ReflectionTestUtils.setField(loanService, "repaymentCalculationService", new LoanRepaymentCalculationService());
        ReflectionTestUtils.setField(loanService, "userValidationService", userValidationService);
        ReflectionTestUtils.setField(loanService, "serviceAccountId", SERVICE_ACCOUNT_ID);
        ReflectionTestUtils.setField(loanService, "annualRate", new BigDecimal("0.15"));
    }

    @Nested
    @DisplayName("Calculation")
    class CalculationTests {
        @Test
        @DisplayName("calculate annuity payment using annual rate")
        void calculateAnnuityPayment_shouldUseFifteenPercentAnnualRate() {
            BigDecimal payment = loanService.calculateAnnuityPayment(new BigDecimal("100000.00"), 12);
            assertEquals(new BigDecimal("9025.83"), payment.setScale(2, RoundingMode.HALF_UP));
        }
    }

    @Nested
    @DisplayName("Create loan")
    class CreateLoanTests {
        @Test
        @DisplayName("create cash loan and increase debit balance")
        void createLoan_withCashDisbursement_increasesOnlyDebitBalance() {
            UUID userId = UUID.randomUUID();
            UUID debitId = UUID.randomUUID();
            CreateLoanRequest request = new CreateLoanRequest();
            request.setDebitAccountId(debitId);
            request.setPrincipalAmount(new BigDecimal("10000.00"));
            request.setTermMonths(12);

            when(bankAccountRepository.exists(SERVICE_ACCOUNT_ID)).thenReturn(true);
            when(bankAccountRepository.exists(debitId)).thenReturn(true);
            when(bankAccountRepository.getUserId(debitId)).thenReturn(userId);
            when(bankAccountRepository.getAccountType(debitId)).thenReturn(BankAccountType.DEBIT);
            when(bankAccountRepository.isActive(debitId)).thenReturn(true);
            when(bankAccountRepository.getCurrency(debitId)).thenReturn(CurrencyType.RUB);

            LoanResponse mapped = new LoanResponse();
            when(loanMapper.loanToResponse(any(Loan.class))).thenReturn(mapped);

            LoanResponse result = loanService.createLoan(request, userId);

            assertEquals(mapped, result);
            verify(bankAccountRepository).increaseBalance(eq(debitId), eq(new BigDecimal("10000.0000")));
            verify(loanRepository).create(any(Loan.class));
        }

        @Test
        @DisplayName("reject create loan without debit account")
        void createLoan_withoutDebit_throwsValidationError() {
            UUID userId = UUID.randomUUID();
            CreateLoanRequest request = new CreateLoanRequest();
            request.setDebitAccountId(null);
            request.setPrincipalAmount(new BigDecimal("10000.00"));
            request.setTermMonths(12);

            when(bankAccountRepository.exists(SERVICE_ACCOUNT_ID)).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> loanService.createLoan(request, userId));
        }
    }

    @Nested
    @DisplayName("Repayment")
    class RepaymentTests {
        @Test
        @DisplayName("repay monthly and update outstanding principal")
        void repayMonthly_shouldUpdateOutstandingWhenPaymentMatchesAnnuity() {
            UUID loanId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID debitId = UUID.randomUUID();
            UUID loanServiceAccountId = UUID.randomUUID();

            Loan loan = new Loan();
            loan.setLoanId(loanId);
            loan.setUserId(userId);
            loan.setDebitAccountId(debitId);
            loan.setServiceAccountId(loanServiceAccountId);
            loan.setTermMonths(12);
            loan.setMonthlyPayment(new BigDecimal("1000.0000"));
            loan.setOutstandingPrincipal(new BigDecimal("10000.0000"));
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setNextPaymentDate(LocalDate.of(2026, 5, 13));
            loan.setCurrency(CurrencyType.RUB);

            LoanRepaymentRequest request = new LoanRepaymentRequest();
            request.setAmount(new BigDecimal("1000.0000"));

            when(loanRepository.findActiveByLoanIdAndUserId(loanId, userId)).thenReturn(Optional.of(loan));
            when(bankAccountRepository.exists(debitId)).thenReturn(true);
            when(bankAccountRepository.getUserId(debitId)).thenReturn(userId);
            when(bankAccountRepository.getAccountType(debitId)).thenReturn(BankAccountType.DEBIT);
            when(bankAccountRepository.getCurrency(debitId)).thenReturn(CurrencyType.RUB);
            when(bankAccountRepository.isActive(debitId)).thenReturn(true);
            when(bankAccountRepository.getBalance(debitId)).thenReturn(new BigDecimal("50000.0000"));
            when(bankAccountRepository.isActive(loanServiceAccountId)).thenReturn(true);
            com.example.xbankbackend.models.User user = new com.example.xbankbackend.models.User();
            user.setEmail("user@test.com");
            when(userRepository.getUser(userId)).thenReturn(user);

            LoanResponse mapped = new LoanResponse();
            when(loanMapper.loanToResponse(any(Loan.class))).thenReturn(mapped);

            LoanResponse result = loanService.repayMonthly(loanId, request, userId);

            assertEquals(mapped, result);
            verify(transactionsService).transfer(any(), eq(userId));
            verify(loanRepository).updateRepaymentState(eq(loanId), eq(new BigDecimal("9125.0000")), eq(LocalDate.of(2026, 6, 13)));
            verify(loanRepository, never()).close(eq(loanId), any(OffsetDateTime.class));
            verify(emailSender).sendLoanRepaymentReceipt(eq("user@test.com"), eq(loanId), eq(new BigDecimal("1000.0000")),
                    eq(new BigDecimal("9125.0000")), eq(LocalDate.of(2026, 6, 13)), eq(false));
        }

        @Test
        @DisplayName("reject arbitrary monthly repayment amount")
        void repayMonthly_shouldRejectArbitraryAmounts() {
            UUID loanId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID debitId = UUID.randomUUID();

            Loan loan = new Loan();
            loan.setLoanId(loanId);
            loan.setUserId(userId);
            loan.setDebitAccountId(debitId);
            loan.setServiceAccountId(UUID.randomUUID());
            loan.setMonthlyPayment(new BigDecimal("1000.0000"));
            loan.setOutstandingPrincipal(new BigDecimal("10000.0000"));
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
            loan.setCurrency(CurrencyType.RUB);

            LoanRepaymentRequest request = new LoanRepaymentRequest();
            request.setAmount(new BigDecimal("900.0000"));

            when(loanRepository.findActiveByLoanIdAndUserId(loanId, userId)).thenReturn(Optional.of(loan));
            when(bankAccountRepository.exists(debitId)).thenReturn(true);
            when(bankAccountRepository.getUserId(debitId)).thenReturn(userId);
            when(bankAccountRepository.getAccountType(debitId)).thenReturn(BankAccountType.DEBIT);
            when(bankAccountRepository.getCurrency(debitId)).thenReturn(CurrencyType.RUB);
            when(bankAccountRepository.isActive(debitId)).thenReturn(true);

            assertThrows(LoanRepaymentAmountMismatchException.class, () -> loanService.repayMonthly(loanId, request, userId));
        }

        @Test
        @DisplayName("repay early fails on insufficient funds")
        void repayEarly_shouldThrowInsufficientFundsWhenDebitBalanceTooLow() {
            UUID loanId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID debitId = UUID.randomUUID();
            UUID loanServiceAccountId = UUID.randomUUID();

            Loan loan = new Loan();
            loan.setLoanId(loanId);
            loan.setUserId(userId);
            loan.setDebitAccountId(debitId);
            loan.setServiceAccountId(loanServiceAccountId);
            loan.setTermMonths(12);
            loan.setMonthlyPayment(new BigDecimal("1000.0000"));
            loan.setOutstandingPrincipal(new BigDecimal("1000.0000"));
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setCurrency(CurrencyType.RUB);
            loan.setCreatedAt(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
            loan.setNextPaymentDate(LocalDate.of(2026, 2, 1));

            BigDecimal required = new BigDecimal("12000.0000");
            LoanRepaymentRequest request = new LoanRepaymentRequest();
            request.setAmount(required);

            when(loanRepository.findActiveByLoanIdAndUserId(loanId, userId)).thenReturn(Optional.of(loan));
            when(bankAccountRepository.exists(debitId)).thenReturn(true);
            when(bankAccountRepository.getUserId(debitId)).thenReturn(userId);
            when(bankAccountRepository.getAccountType(debitId)).thenReturn(BankAccountType.DEBIT);
            when(bankAccountRepository.getCurrency(debitId)).thenReturn(CurrencyType.RUB);
            when(bankAccountRepository.isActive(debitId)).thenReturn(true);
            when(bankAccountRepository.getBalance(debitId)).thenReturn(new BigDecimal("100.0000"));

            assertThrows(InsufficientFundsException.class, () -> loanService.repayEarly(loanId, request, userId));
        }
    }

    @Nested
    @DisplayName("Payment cost")
    class PaymentCostTests {
        @Test
        @DisplayName("calculate full payment cost from remaining monthly payments")
        void fullPaymentCost_shouldReturnRemainingMonthlyPayments() {
            UUID userId = UUID.randomUUID();
            UUID loanId = UUID.randomUUID();

            Loan loan = new Loan();
            loan.setLoanId(loanId);
            loan.setUserId(userId);
            loan.setTermMonths(12);
            loan.setMonthlyPayment(new BigDecimal("1000.0000"));
            loan.setOutstandingPrincipal(new BigDecimal("1000.0000"));
            loan.setCreatedAt(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
            loan.setNextPaymentDate(LocalDate.of(2026, 4, 1));
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setCurrency(CurrencyType.RUB);

            when(loanRepository.findActiveByLoanIdAndUserId(loanId, userId)).thenReturn(Optional.of(loan));

            LoanPaymentAmountResponse response = loanService.fullPaymentCost(loanId, userId);

            assertEquals(new BigDecimal("10000.0000"), response.getAmount().setScale(4, RoundingMode.HALF_UP));
        }
    }

    @Nested
    @DisplayName("Autopay")
    class AutopayTests {
        @Test
        @DisplayName("enable autopay by account")
        void enableAutopayByAccount_shouldEnableAutopayForOwnedActiveLoan() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID loanId = UUID.randomUUID();

            Loan loan = new Loan();
            loan.setLoanId(loanId);
            loan.setUserId(userId);
            loan.setDebitAccountId(accountId);
            loan.setCurrency(CurrencyType.RUB);
            loan.setStatus(LoanStatus.ACTIVE);

            when(loanRepository.findActiveByDebitAccountIdAndUserId(accountId, userId)).thenReturn(Optional.of(loan));
            when(loanRepository.findActiveByLoanIdAndUserId(loanId, userId)).thenReturn(Optional.of(loan));
            when(bankAccountRepository.exists(accountId)).thenReturn(true);
            when(bankAccountRepository.getUserId(accountId)).thenReturn(userId);
            when(bankAccountRepository.getAccountType(accountId)).thenReturn(BankAccountType.DEBIT);
            when(bankAccountRepository.getCurrency(accountId)).thenReturn(CurrencyType.RUB);
            when(bankAccountRepository.isActive(accountId)).thenReturn(true);

            LoanResponse mapped = new LoanResponse();
            when(loanMapper.loanToResponse(any(Loan.class))).thenReturn(mapped);

            LoanResponse result = loanService.enableAutopayByAccount(accountId, userId);

            assertEquals(mapped, result);
            verify(loanRepository).setAutopayEnabled(loanId, true);
        }
    }
}
