package com.example.xbankbackend.services;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceTest {
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BankAccountService bankAccountService;

    @Test
    void createBankAccount_shouldThrowIfUserNotFound() {
        UUID userId = UUID.randomUUID();
        CurrencyType currency = CurrencyType.RUB;
        BankAccountType accountType = BankAccountType.CREDIT;

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUserId(userId);
        bankAccount.setCurrency(currency);
        bankAccount.setAccountType(accountType);

        when(userRepository.haveUserId(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> bankAccountService.createBankAccount(bankAccount));
    }

    @Test
    void createBankAccount_shouldCreateWithDefaultValues() {
        UUID userId = UUID.randomUUID();
        CurrencyType currency = CurrencyType.RUB;
        BankAccountType accountType = BankAccountType.CREDIT;

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUserId(userId);
        bankAccount.setCurrency(currency);
        bankAccount.setAccountType(accountType);

        when(userRepository.haveUserId(userId)).thenReturn(true);

        bankAccountService.createBankAccount(bankAccount);

        assertEquals(0.0f, bankAccount.getBalance());
        assertNotNull(bankAccount.getAccountId());

        verify(bankAccountRepository).createBankAccount(bankAccount);
    }
}
