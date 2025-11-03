package com.example.xbankbackend.services;

import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.DifferentCurrencyException;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
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

    // validateDeposit
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
        UUID receiverId = null;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
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

    // validateTransfer
    @Test
    void validateTransfer_shouldThrowIfTransactionTypeIsNotTransfer() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.transferMoney(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfSenderIdIsNull() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = null;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.transferMoney(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfReceiverIdIsNull() {
        UUID receiverId = null;
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.transferMoney(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfReceiverIdNotFound() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> transactionsService.transferMoney(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfSenderIdNotFound() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);
        when(bankAccountRepository.haveUUID(senderId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> transactionsService.transferMoney(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfInsufficientFunds() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);
        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(4000.0f);

        assertThrows(InsufficientFundsException.class, () -> transactionsService.transferMoney(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfAmountNotPositive() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(-5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);
        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(6000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.transferMoney(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfDifferentCurrency() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("USD");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);
        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(6000.0f);
        when(bankAccountRepository.getCurrency(receiverId)).thenReturn("RUB");

        assertThrows(DifferentCurrencyException.class, () -> transactionsService.transferMoney(transaction));
    }

    @Test
    void validateTransfer_shouldChangeBalance() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);
        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(6000.0f);
        when(bankAccountRepository.getCurrency(receiverId)).thenReturn("RUB");

        transactionsService.transferMoney(transaction);

        verify(bankAccountRepository).increaseBalance(receiverId, 5000.0f);
        verify(bankAccountRepository).decreaseBalance(senderId, 5000.0f);
    }

    // validatePayment
    @Test
    void validatePayment_shouldThrowIfTransactionTypeIsNotPayment() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfSenderIdIsNull() {
        UUID senderId = null;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfReceiverIdIsNotNull() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setReceiverId(UUID.randomUUID());
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfSenderIdNotFound() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(senderId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfInsufficientFunds() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(4000.0f);

        assertThrows(InsufficientFundsException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfAmountNotPositive() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(-5000.0f);

        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(6000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldDecreaseBalance() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency("RUB");
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(6000.0f);

        transactionsService.pay(transaction);

        verify(bankAccountRepository).decreaseBalance(senderId, 5000.0f);
    }
}
