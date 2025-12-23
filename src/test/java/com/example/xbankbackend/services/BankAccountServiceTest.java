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

import java.math.BigDecimal;
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
    void create_shouldThrowIfUserNotFound() {
        UUID userId = UUID.randomUUID();
        CurrencyType currency = CurrencyType.RUB;
        BankAccountType accountType = BankAccountType.CREDIT;

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUserId(userId);
        bankAccount.setCurrency(currency);
        bankAccount.setAccountType(accountType);

        when(userRepository.exists(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> bankAccountService.create(bankAccount));
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

        when(userRepository.exists(userId)).thenReturn(true);

        bankAccountService.create(bankAccount);

        assertEquals(BigDecimal.ZERO, bankAccount.getBalance());
        assertNotNull(bankAccount.getAccountId());

        verify(bankAccountRepository).create(bankAccount);
    }
}
