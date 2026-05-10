package com.example.xbankbackend.services.savings;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.SavingsAccount;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.SavingsAccountRepository;
import com.example.xbankbackend.services.transaction.TransactionsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsAccountService")
class SavingsAccountServiceTest {

    @Mock
    private SavingsAccountRepository savingsRepo;

    @Mock
    private BankAccountRepository bankRepo;

    @Mock
    private TransactionsService transactionsService;

    @Mock
    private SavingsAccountValidationService validationService;

    @InjectMocks
    private SavingsAccountService service;

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        void shouldThrow_WhenBaseAccountNotSavingsType() {
            UUID id = UUID.randomUUID();
            SavingsAccount savings = new SavingsAccount();
            savings.setAccountId(id);
            BankAccount base = new BankAccount();
            base.setAccountType(BankAccountType.DEBIT);

            doNothing().when(validationService).validateSavingsAccountExists(id);
            when(bankRepo.get(id)).thenReturn(base);

            assertThatThrownBy(() -> service.create(savings))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldCreate_WhenValid() {
            UUID id = UUID.randomUUID();
            SavingsAccount savings = new SavingsAccount();
            savings.setAccountId(id);
            savings.setInterestRate(BigDecimal.valueOf(10));
            savings.setMaturityDate(LocalDate.now().plusMonths(6));

            BankAccount base = new BankAccount();
            base.setAccountType(BankAccountType.SAVINGS);

            doNothing().when(validationService).validateSavingsAccountExists(id);
            when(bankRepo.get(id)).thenReturn(base);

            service.create(savings);
            verify(savingsRepo).create(savings);
        }
    }

    @Nested
    @DisplayName("prolong")
    class ProlongTests {

        @Test
        void shouldThrow_WhenAccountNotExpired() {
            UUID id = UUID.randomUUID();
            SavingsAccount savings = new SavingsAccount();
            savings.setStatus("ACTIVE");

            doNothing().when(validationService).validateSavingsAccountExists(id);
            when(savingsRepo.get(id)).thenReturn(savings);

            assertThatThrownBy(() -> service.prolong(id, LocalDate.now().plusMonths(6)))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void shouldProlong_WhenExpired() {
            UUID id = UUID.randomUUID();
            LocalDate newDate = LocalDate.now().plusMonths(6);
            SavingsAccount savings = new SavingsAccount();
            savings.setStatus("EXPIRED");
            savings.setAccruedInterest(BigDecimal.valueOf(100));

            doNothing().when(validationService).validateSavingsAccountExists(id);
            when(savingsRepo.get(id)).thenReturn(savings);

            service.prolong(id, newDate);

            assertThat(savings.getMaturityDate()).isEqualTo(newDate);
            assertThat(savings.getAccruedInterest()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(savingsRepo).update(savings);
        }
    }

    @Nested
    @DisplayName("closeAccount")
    class CloseAccountTests {

        @Test
        void shouldThrow_WhenAccountAlreadyClosed() {
            UUID id = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            SavingsAccount savings = new SavingsAccount();
            savings.setStatus("CLOSED");

            doNothing().when(validationService).validateSavingsAccountExists(id);
            when(savingsRepo.get(id)).thenReturn(savings);

            assertThatThrownBy(() -> service.closeAccount(id, target, UUID.randomUUID()))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void shouldApplyPenalty_WhenClosingBeforeMaturity() {
            UUID id = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            BankAccount source = new BankAccount();
            source.setAccountId(id);
            source.setBalance(BigDecimal.valueOf(10000));
            source.setCurrency(CurrencyType.RUB);

            SavingsAccount savings = new SavingsAccount();
            savings.setStatus("ACTIVE");
            savings.setMaturityDate(LocalDate.now().plusMonths(3));
            savings.setEarlyWithdrawalPenalty(BigDecimal.valueOf(10));
            savings.setAccruedInterest(BigDecimal.valueOf(1000));

            doNothing().when(validationService).validateSavingsAccountExists(id);
            when(savingsRepo.get(id)).thenReturn(savings);
            when(bankRepo.get(id)).thenReturn(source);
            when(bankRepo.getCurrency(id)).thenReturn(source.getCurrency());

            service.closeAccount(id, target, userId);

            verify(transactionsService).transfer(
                    argThat(tx -> tx.getAmount().compareTo(BigDecimal.valueOf(10900)) == 0),
                    eq(userId),
                    eq(true)
            );
            assertThat(savings.getStatus()).isEqualTo("CLOSED");
        }

        @Test
        void shouldNotApplyPenalty_WhenClosingAfterMaturity() {
            UUID id = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            BankAccount source = new BankAccount();
            source.setAccountId(id);
            source.setBalance(BigDecimal.valueOf(10000));
            source.setCurrency(CurrencyType.RUB);

            SavingsAccount savings = new SavingsAccount();
            savings.setStatus("EXPIRED");
            savings.setMaturityDate(LocalDate.now().minusDays(1));
            savings.setAccruedInterest(BigDecimal.valueOf(1000));

            doNothing().when(validationService).validateSavingsAccountExists(id);
            when(savingsRepo.get(id)).thenReturn(savings);
            when(bankRepo.get(id)).thenReturn(source);
            when(bankRepo.getCurrency(id)).thenReturn(source.getCurrency());

            service.closeAccount(id, target, userId);

            verify(transactionsService).transfer(
                    argThat(tx -> tx.getAmount().compareTo(BigDecimal.valueOf(11000)) == 0),
                    eq(userId),
                    eq(true)
            );
        }
    }

    @Nested
    @DisplayName("calculateDailyInterest")
    class CalculateDailyInterestTests {

        @Test
        void shouldAccrueInterest_ForActiveAccounts() {
            LocalDate today = LocalDate.now();
            UUID id = UUID.randomUUID();

            BankAccount base = new BankAccount();
            base.setAccountId(id);
            base.setBalance(BigDecimal.valueOf(100000));

            SavingsAccount savings = new SavingsAccount();
            savings.setAccountId(id);
            savings.setInterestRate(BigDecimal.valueOf(12));
            savings.setMaturityDate(today.plusMonths(6));
            savings.setLastInterestCalculation(today.minusDays(1));

            when(savingsRepo.findForInterestCalculation(today)).thenReturn(List.of(savings));
            when(bankRepo.get(id)).thenReturn(base);

            service.calculateDailyInterest();

            verify(savingsRepo).increaseAccruedInterest(
                    eq(id), argThat(amount -> amount.compareTo(BigDecimal.valueOf(32)) > 0)
            );
            verify(savingsRepo).updateLastInterestCalculation(id, today);
        }

        @Test
        void shouldSetExpired_WhenMaturityDateIsToday() {
            LocalDate today = LocalDate.now();
            UUID id = UUID.randomUUID();

            BankAccount base = new BankAccount();
            base.setAccountId(id);
            base.setBalance(BigDecimal.valueOf(100000));

            SavingsAccount savings = new SavingsAccount();
            savings.setAccountId(id);
            savings.setInterestRate(BigDecimal.valueOf(12));
            savings.setMaturityDate(today);
            savings.setLastInterestCalculation(today.minusDays(1));

            when(savingsRepo.findForInterestCalculation(today)).thenReturn(List.of(savings));
            when(bankRepo.get(id)).thenReturn(base);

            service.calculateDailyInterest();
            verify(savingsRepo).setExpired(id);
        }
    }

    @Nested
    @DisplayName("makeMonthlyAccrual")
    class MakeMonthlyAccrualTests {

        @Test
        void shouldPayoutAccruedInterest() {
            UUID id = UUID.randomUUID();
            BigDecimal accrued = BigDecimal.valueOf(500);

            SavingsAccount savings = new SavingsAccount();
            savings.setAccountId(id);
            savings.setAccruedInterest(accrued);

            BankAccount bankAccount = new BankAccount();
            bankAccount.setAccountId(id);

            when(savingsRepo.findForInterestCalculation(any())).thenReturn(List.of(savings));
            when(bankRepo.get(id)).thenReturn(bankAccount);

            service.makeMonthlyAccrual();

            verify(savingsRepo).setAccruedInterestToZero(id);
        }
    }
}
