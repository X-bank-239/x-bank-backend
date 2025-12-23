package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.responses.RecentTransactionsResponse;
import com.example.xbankbackend.dtos.responses.TransactionResponse;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.exceptions.DifferentCurrencyException;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.repositories.UserRepository;
import com.example.xbankbackend.repositories.external.cbr.CurrencyRateRepository;
import com.example.xbankbackend.services.external.cbr.CurrencyRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class TransactionsServiceTest {
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrencyRateService currencyRateService;

    @InjectMocks
    private TransactionsService transactionsService;

    // validateDeposit
    @Test
    void validateDeposit_shouldThrowIfTransactionTypeIsNotDeposit() {
        UUID receiverId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.deposit(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfReceiverIdIsNull() {
        UUID receiverId = null;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.deposit(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfSenderIdIsNotNull() {
        UUID receiverId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(UUID.randomUUID());
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.deposit(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfReceiverIdNotFound() {
        UUID receiverId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.exists(receiverId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> transactionsService.deposit(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfAmountNotPositive() {
        UUID receiverId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(-5000.0));

        when(bankAccountRepository.exists(receiverId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.deposit(transaction));
    }

    @Test
    void validateDeposit_shouldIncreaseBalance() {
        UUID receiverId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.getCurrency(receiverId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.exists(receiverId)).thenReturn(true);
        when(currencyRateService.convert(any(), any(), any())).thenReturn(BigDecimal.valueOf(5000.0));

        transactionsService.deposit(transaction);

        verify(bankAccountRepository).increaseBalance(receiverId, BigDecimal.valueOf(5000.0));
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
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.transfer(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfSenderIdIsNull() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = null;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.transfer(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfReceiverIdIsNull() {
        UUID receiverId = null;
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.transfer(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfReceiverIdNotFound() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.exists(receiverId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> transactionsService.transfer(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfSenderIdNotFound() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.exists(receiverId)).thenReturn(true);
        when(bankAccountRepository.exists(senderId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> transactionsService.transfer(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfInsufficientFunds() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.exists(receiverId)).thenReturn(true);
        when(bankAccountRepository.exists(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(BigDecimal.valueOf(4000.0));

        assertThrows(InsufficientFundsException.class, () -> transactionsService.transfer(transaction));
    }

    @Test
    void validateTransfer_shouldThrowIfAmountNotPositive() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(-5000.0));

        when(bankAccountRepository.exists(receiverId)).thenReturn(true);
        when(bankAccountRepository.exists(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(BigDecimal.valueOf(6000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.transfer(transaction));
    }

    @Test
    void validateTransfer_shouldChangeBalance() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setReceiverId(receiverId);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.exists(receiverId)).thenReturn(true);
        when(bankAccountRepository.exists(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(BigDecimal.valueOf(6000.0));
        when(bankAccountRepository.getCurrency(receiverId)).thenReturn(CurrencyType.RUB);
        when(currencyRateService.convert(any(), any(), any())).thenReturn(BigDecimal.valueOf(5000.0));

        transactionsService.transfer(transaction);

        verify(bankAccountRepository).increaseBalance(receiverId, BigDecimal.valueOf(5000.0));
        verify(bankAccountRepository).decreaseBalance(senderId, BigDecimal.valueOf(5000.0));
    }

    // validatePayment
    @Test
    void validatePayment_shouldThrowIfTransactionTypeIsNotPayment() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfSenderIdIsNull() {
        UUID senderId = null;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfReceiverIdIsNotNull() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setReceiverId(UUID.randomUUID());
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfSenderIdNotFound() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.exists(senderId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfInsufficientFunds() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.exists(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(BigDecimal.valueOf(4000.0));

        assertThrows(InsufficientFundsException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfAmountNotPositive() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(-5000.0));

        when(bankAccountRepository.exists(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(BigDecimal.valueOf(6000.0));

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldDecreaseBalance() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(BigDecimal.valueOf(5000.0));

        when(bankAccountRepository.exists(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(BigDecimal.valueOf(6000.0));

        transactionsService.pay(transaction);

        verify(bankAccountRepository).decreaseBalance(senderId, BigDecimal.valueOf(5000.0));
    }

    // getRecent
    @Test
    void getRecent_shouldThrowIfBankAccountNotFound() {
        UUID accountId = UUID.randomUUID();
        int page = 1;
        int size = 2;

        when(bankAccountRepository.exists(accountId)).thenReturn(false);

        assertThrows(BankAccountNotFoundException.class, () -> transactionsService.getRecent(accountId, page, size));
    }

    @Test
    void getRecent_shouldThrowIfPageIsNegative() {
        UUID accountId = UUID.randomUUID();
        int page = -1;
        int size = 2;

        when(bankAccountRepository.exists(accountId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.getRecent(accountId, page, size));
    }

    @Test
    void getRecent_shouldThrowIfSizeIsNegative() {
        UUID accountId = UUID.randomUUID();
        int page = 1;
        int size = -2;

        when(bankAccountRepository.exists(accountId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.getRecent(accountId, page, size));
    }

    @Test
    void getRecentTransactions_shouldReturnRecent() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int total = 2;
        int page = 0;
        int size = 2;

        User user = new User();
        user.setUserId(userId);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@xbank.ru");
        user.setBirthdate(new Date());

        Transaction transaction1 = new Transaction();
        transaction1.setTransactionType(TransactionType.DEPOSIT);
        transaction1.setCurrency(CurrencyType.RUB);
        transaction1.setAmount(BigDecimal.valueOf(5000.0));
        transaction1.setReceiverId(accountId);

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionType(TransactionType.PAYMENT);
        transaction2.setCurrency(CurrencyType.RUB);
        transaction2.setAmount(BigDecimal.valueOf(200.0));
        transaction2.setSenderId(accountId);

        TransactionResponse transactionResponse1 = new TransactionResponse();
        transactionResponse1.setTransactionType(TransactionType.DEPOSIT);
        transactionResponse1.setCurrency(CurrencyType.RUB);
        transactionResponse1.setAmount(BigDecimal.valueOf(5000.0));
        transactionResponse1.setReceiverName("Test");

        TransactionResponse transactionResponse2 = new TransactionResponse();
        transactionResponse2.setTransactionType(TransactionType.PAYMENT);
        transactionResponse2.setCurrency(CurrencyType.RUB);
        transactionResponse2.setAmount(BigDecimal.valueOf(200.0));
        transactionResponse2.setSenderName("Test");

        List<Transaction> transactions = List.of(transaction1, transaction2);
        List<TransactionResponse> transactionResponses = List.of(transactionResponse1, transactionResponse2);
        RecentTransactionsResponse recentTransactionsResponse = new RecentTransactionsResponse(total, page, size, transactionResponses);

        when(bankAccountRepository.exists(accountId)).thenReturn(true);
        when(bankAccountRepository.getUserId(accountId)).thenReturn(userId);
        when(transactionsRepository.getTransactions(accountId, page, size)).thenReturn(transactions);
        when(transactionsRepository.getTransactionsCount(accountId)).thenReturn(total);
        when(userRepository.getUser(userId)).thenReturn(user);

        RecentTransactionsResponse result = transactionsService.getRecent(accountId, page, size);

        assertNotNull(result);
        assertEquals(recentTransactionsResponse, result);

        verify(bankAccountRepository).exists(accountId);
        verify(transactionsRepository).getTransactions(accountId, page, size);
    }
}
