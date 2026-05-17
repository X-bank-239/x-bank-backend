package com.example.xbankbackend.services.bankAccount;

import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.services.FeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountValidationService")
class BankAccountValidationServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private FeeService feeService;

    @InjectMocks
    private BankAccountValidationService validationService;

    @Nested
    @DisplayName("validateBankAccountExists")
    class ValidateBankAccountExistsTests {

        @Test
        void shouldNotThrow_WhenAccountExists() {
            UUID accountId = UUID.randomUUID();

            when(bankAccountRepository.exists(accountId)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> validationService.validateBankAccountExists(accountId));
        }

        @Test
        void shouldThrowBankAccountNotFoundException_WhenAccountNotExists() {
            UUID accountId = UUID.randomUUID();

            when(bankAccountRepository.exists(accountId)).thenReturn(false);

            assertThatThrownBy(() -> validationService.validateBankAccountExists(accountId))
                    .isInstanceOf(BankAccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateBankAccountActive")
    class ValidateBankAccountActiveTests {

        @Test
        void shouldNotThrow_WhenAccountIsActive() {
            UUID accountId = UUID.randomUUID();

            when(bankAccountRepository.isActive(accountId)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> validationService.validateBankAccountActive(accountId));
        }

        @Test
        void shouldThrowAccessDeniedException_WhenAccountIsNotActive() {
            UUID accountId = UUID.randomUUID();
            when(bankAccountRepository.isActive(accountId)).thenReturn(false);

            assertThatThrownBy(() -> validationService.validateBankAccountActive(accountId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("validateSufficientFundsWithFee")
    class ValidateSufficientFundsWithFeeTests {

        @Test
        void shouldNotThrow_WhenBalanceIsSufficient() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(1000);
            BigDecimal balance = BigDecimal.valueOf(2000);
            BigDecimal amountWithFee = BigDecimal.valueOf(1015);

            when(bankAccountRepository.getBalance(accountId)).thenReturn(balance);
            when(feeService.applyBaseFee(amount)).thenReturn(amountWithFee);

            assertThatNoException().isThrownBy(() -> validationService.validateSufficientFundsWithFee(accountId, amount));
        }

        @Test
        void shouldNotThrow_WhenBalanceIsExactlyEqualToAmountWithFee() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(1000);
            BigDecimal balance = BigDecimal.valueOf(1015);
            BigDecimal amountWithFee = BigDecimal.valueOf(1015);

            when(bankAccountRepository.getBalance(accountId)).thenReturn(balance);
            when(feeService.applyBaseFee(amount)).thenReturn(amountWithFee);

            assertThatNoException().isThrownBy(() -> validationService.validateSufficientFundsWithFee(accountId, amount));
        }

        @Test
        void shouldThrowInsufficientFundsException_WhenBalanceIsLessThanAmountWithFee() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(1000);
            BigDecimal balance = BigDecimal.valueOf(1000);
            BigDecimal amountWithFee = BigDecimal.valueOf(1015);

            when(bankAccountRepository.getBalance(accountId)).thenReturn(balance);
            when(feeService.applyBaseFee(amount)).thenReturn(amountWithFee);

            assertThatThrownBy(() -> validationService.validateSufficientFundsWithFee(accountId, amount))
                    .isInstanceOf(InsufficientFundsException.class);
        }

        @Test
        void shouldThrowInsufficientFundsException_WhenBalanceIsZero() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(1000);
            BigDecimal balance = BigDecimal.ZERO;
            BigDecimal amountWithFee = BigDecimal.valueOf(1015);

            when(bankAccountRepository.getBalance(accountId)).thenReturn(balance);
            when(feeService.applyBaseFee(amount)).thenReturn(amountWithFee);

            assertThatThrownBy(() -> validationService.validateSufficientFundsWithFee(accountId, amount))
                    .isInstanceOf(InsufficientFundsException.class);
        }

        @Test
        void shouldConsiderFeeInValidation() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(10000);
            BigDecimal balance = BigDecimal.valueOf(10100);
            BigDecimal amountWithFee = BigDecimal.valueOf(10150);

            when(bankAccountRepository.getBalance(accountId)).thenReturn(balance);
            when(feeService.applyBaseFee(amount)).thenReturn(amountWithFee);

            assertThatThrownBy(() -> validationService.validateSufficientFundsWithFee(accountId, amount))
                    .isInstanceOf(InsufficientFundsException.class);
        }

        @Test
        void shouldHandleLargeAmounts() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(1_000_000);
            BigDecimal balance = BigDecimal.valueOf(1_015_000);
            BigDecimal amountWithFee = BigDecimal.valueOf(1_015_000);

            when(bankAccountRepository.getBalance(accountId)).thenReturn(balance);
            when(feeService.applyBaseFee(amount)).thenReturn(amountWithFee);

            assertThatNoException().isThrownBy(() -> validationService.validateSufficientFundsWithFee(accountId, amount));
        }

        @Test
        void shouldHandleSmallAmounts() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(0.01);
            BigDecimal balance = BigDecimal.valueOf(0.02);
            BigDecimal amountWithFee = BigDecimal.valueOf(0.01015);

            when(bankAccountRepository.getBalance(accountId)).thenReturn(balance);
            when(feeService.applyBaseFee(amount)).thenReturn(amountWithFee);

            assertThatNoException().isThrownBy(() -> validationService.validateSufficientFundsWithFee(accountId, amount));
        }
    }
}
