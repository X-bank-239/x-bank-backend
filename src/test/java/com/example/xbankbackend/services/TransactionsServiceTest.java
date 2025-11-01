package com.example.xbankbackend.services;

import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.DifferentCurrencyException;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class TransactionsServiceTest {
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionsRepository transactionsRepository;

    @InjectMocks
    private TransactionsService transactionsService;

    @Test
    void validateDeposit_shouldThrowIfTransactionTypeIsNotDeposit() {
        UUID receiverId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfReceiverIdIsNull() {
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfSenderIdIsNotNull() {
        UUID receiverId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(UUID.randomUUID());
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfReceiverIdNotFound() {
        UUID receiverId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfAmountNotPositive() {
        UUID receiverId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency("RUB");
        transaction.setAmount(-5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfDifferentCurrency() {
        UUID receiverId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency("USD");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.getCurrency(receiverId)).thenReturn("RUB");
        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);

        assertThrows(DifferentCurrencyException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldIncreaseBalance() {
        UUID receiverId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.getCurrency(receiverId)).thenReturn("RUB");
        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);

        transactionsService.depositAccount(transaction);

        verify(bankAccountRepository).increaseBalance(receiverId, 5000.0f);
    }
}
