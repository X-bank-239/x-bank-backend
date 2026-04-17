package com.example.xbankbackend.services.bankAccount;

import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.repositories.BankAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountValidationService")
class BankAccountValidationServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private BankAccountValidationService service;

    @Nested
    @DisplayName("validateBankAccountExists")
    class ValidateBankAccountExistsTests {

        @Test
        void shouldNotThrow_WhenAccountExists() {
            UUID accountId = UUID.randomUUID();

            when(bankAccountRepository.exists(accountId)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateBankAccountExists(accountId));
        }

        @Test
        void shouldThrowBankAccountNotFoundException_WhenAccountNotExists() {
            UUID accountId = UUID.randomUUID();

            when(bankAccountRepository.exists(accountId)).thenReturn(false);

            assertThatThrownBy(() -> service.validateBankAccountExists(accountId))
                    .isInstanceOf(BankAccountNotFoundException.class);
        }
    }
}
