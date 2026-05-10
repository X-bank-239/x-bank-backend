package com.example.xbankbackend.services.savings;

import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.models.SavingsAccount;
import com.example.xbankbackend.repositories.SavingsAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsAccountValidationService")
class SavingsAccountValidationServiceTest {

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    @InjectMocks
    private SavingsAccountValidationService service;

    @Nested
    @DisplayName("validateSavingsAccountExists")
    class ValidateExistsTests {

        @Test
        void shouldNotThrow_WhenAccountExists() {
            UUID id = UUID.randomUUID();

            when(savingsAccountRepository.exists(id)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateSavingsAccountExists(id));
        }

        @Test
        void shouldThrowBankAccountNotFoundException_WhenAccountNotExists() {
            UUID id = UUID.randomUUID();

            when(savingsAccountRepository.exists(id)).thenReturn(false);

            assertThatThrownBy(() -> service.validateSavingsAccountExists(id))
                    .isInstanceOf(BankAccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateSavingsAccountExistsSoft")
    class ValidateExistsSoftTests {

        @Test
        void shouldReturnTrue_WhenAccountExists() {
            UUID id = UUID.randomUUID();

            when(savingsAccountRepository.exists(id)).thenReturn(true);

            assertThat(service.validateSavingsAccountExistsSoft(id)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenAccountNotExists() {
            UUID id = UUID.randomUUID();

            when(savingsAccountRepository.exists(id)).thenReturn(false);

            assertThat(service.validateSavingsAccountExistsSoft(id)).isFalse();
        }
    }

    @Nested
    @DisplayName("validateTopUpAllowed")
    class ValidateTopUpAllowedTests {

        @Test
        void shouldNotThrow_WhenTopUpAllowed() {
            UUID id = UUID.randomUUID();
            SavingsAccount acc = new SavingsAccount();
            acc.setAllowTopUp(true);

            when(savingsAccountRepository.get(id)).thenReturn(acc);

            assertThatNoException().isThrownBy(() -> service.validateTopUpAllowed(id));
        }

        @Test
        void shouldThrowAccessDeniedException_WhenTopUpNotAllowed() {
            UUID id = UUID.randomUUID();
            SavingsAccount acc = new SavingsAccount();
            acc.setAllowTopUp(false);

            when(savingsAccountRepository.get(id)).thenReturn(acc);
            assertThatThrownBy(() -> service.validateTopUpAllowed(id))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("validateWithdrawalAllowed")
    class ValidateWithdrawalAllowedTests {

        @Test
        void shouldNotThrow_WhenWithdrawalAllowed() {
            UUID id = UUID.randomUUID();
            SavingsAccount acc = new SavingsAccount();
            acc.setAllowWithdrawal(true);

            when(savingsAccountRepository.get(id)).thenReturn(acc);

            assertThatNoException().isThrownBy(() -> service.validateWithdrawalAllowed(id));
        }

        @Test
        void shouldThrowAccessDeniedException_WhenWithdrawalNotAllowed() {
            UUID id = UUID.randomUUID();
            SavingsAccount acc = new SavingsAccount();
            acc.setAllowWithdrawal(false);

            when(savingsAccountRepository.get(id)).thenReturn(acc);

            assertThatThrownBy(() -> service.validateWithdrawalAllowed(id))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }
}
