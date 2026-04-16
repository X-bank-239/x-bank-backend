package com.example.xbankbackend.services.bankAccount;

import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.mappers.BankAccountMapper;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.services.user.UserValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountService")
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Mock
    private BankAccountValidationService bankAccountValidationService;

    @Mock
    private UserValidationService userValidationService;

    @InjectMocks
    private BankAccountService service;

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        void shouldThrowUserNotFoundException_WhenUserNotExists() {
            UUID userId = UUID.randomUUID();
            BankAccount account = new BankAccount();
            account.setUserId(userId);

            doThrow(new UserNotFoundException("User not found"))
                    .when(userValidationService).validateUserExists(userId);

            assertThatThrownBy(() -> service.create(account))
                    .isInstanceOf(UserNotFoundException.class);

            verify(bankAccountRepository, never()).create(any());
        }

        @Test
        void shouldCreateAccount_WithDefaultValues() {
            UUID userId = UUID.randomUUID();
            BankAccount account = new BankAccount();
            account.setUserId(userId);

            doNothing().when(userValidationService).validateUserExists(userId);

            service.create(account);

            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(account.getAccountId()).isNotNull();
            assertThat(account.getActive()).isTrue();

            verify(bankAccountRepository).create(account);
        }
    }

    @Nested
    @DisplayName("get")
    class GetTests {

        @Test
        void shouldThrowBankAccountNotFoundException_WhenAccountNotExists() {
            UUID accountId = UUID.randomUUID();

            doThrow(new BankAccountNotFoundException("Account not found"))
                    .when(bankAccountValidationService).validateBankAccountExists(accountId);

            assertThatThrownBy(() -> service.get(accountId))
                    .isInstanceOf(BankAccountNotFoundException.class);

            verify(bankAccountRepository, never()).get(accountId);
            verify(bankAccountMapper, never()).accountToResponse(any());
        }

        @Test
        void shouldReturnBankAccountResponse_WhenAccountExists() {
            UUID accountId = UUID.randomUUID();
            BankAccount entity = new BankAccount();
            BankAccountResponse expected = new BankAccountResponse();

            doNothing().when(bankAccountValidationService).validateBankAccountExists(accountId);
            when(bankAccountRepository.get(accountId)).thenReturn(entity);
            when(bankAccountMapper.accountToResponse(entity)).thenReturn(expected);

            BankAccountResponse result = service.get(accountId);

            assertThat(result).isEqualTo(expected);
            verify(bankAccountRepository).get(accountId);
            verify(bankAccountMapper).accountToResponse(entity);
        }
    }

    @Nested
    @DisplayName("getAccountsByUser")
    class GetAccountsByUserTests {

        @Test
        void shouldThrowUserNotFoundException_WhenUserNotExists() {
            UUID userId = UUID.randomUUID();

            doThrow(new UserNotFoundException("User not found"))
                    .when(userValidationService).validateUserExists(userId);

            assertThatThrownBy(() -> service.getAccountsByUser(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(bankAccountRepository, never()).getBankAccounts(userId);
        }

        @Test
        void shouldReturnAccountList_WhenUserExists() {
            UUID userId = UUID.randomUUID();
            List<BankAccount> entities = List.of(new BankAccount(), new BankAccount());
            List<BankAccountResponse> expected = List.of(new BankAccountResponse(), new BankAccountResponse());

            doNothing().when(userValidationService).validateUserExists(userId);
            when(bankAccountRepository.getBankAccounts(userId)).thenReturn(entities);
            when(bankAccountMapper.accountsToResponses(entities)).thenReturn(expected);

            List<BankAccountResponse> result = service.getAccountsByUser(userId);

            assertThat(result).hasSize(2).containsExactlyElementsOf(expected);
            verify(bankAccountRepository).getBankAccounts(userId);
            verify(bankAccountMapper).accountsToResponses(entities);
        }
    }

    @Nested
    @DisplayName("deactivateAccount")
    class DeactivateAccountTests {

        @Test
        void shouldThrowBankAccountNotFoundException_WhenAccountNotExists() {
            UUID accountId = UUID.randomUUID();

            doThrow(new BankAccountNotFoundException("Account not found"))
                    .when(bankAccountValidationService).validateBankAccountExists(accountId);

            assertThatThrownBy(() -> service.deactivateAccount(accountId))
                    .isInstanceOf(BankAccountNotFoundException.class);

            verify(bankAccountRepository, never()).deactivate(accountId);
        }

        @Test
        void shouldDeactivateAccount_WhenExists() {
            UUID accountId = UUID.randomUUID();

            doNothing().when(bankAccountValidationService).validateBankAccountExists(accountId);

            service.deactivateAccount(accountId);

            verify(bankAccountRepository).deactivate(accountId);
        }
    }
}
