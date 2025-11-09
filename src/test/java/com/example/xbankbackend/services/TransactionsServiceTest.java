package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.RecentTransactionsDTO;
import com.example.xbankbackend.dtos.TransactionDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfReceiverIdIsNull() {
        UUID receiverId = null;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldThrowIfReceiverIdNotFound() {
        UUID receiverId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.USD);
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.getCurrency(receiverId)).thenReturn(CurrencyType.RUB);
        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);

        assertThrows(DifferentCurrencyException.class, () -> transactionsService.depositAccount(transaction));
    }

    @Test
    void validateDeposit_shouldIncreaseBalance() {
        UUID receiverId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReceiverId(receiverId);
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.getCurrency(receiverId)).thenReturn(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.USD);
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);
        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(6000.0f);
        when(bankAccountRepository.getCurrency(receiverId)).thenReturn(CurrencyType.RUB);

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
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(receiverId)).thenReturn(true);
        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(6000.0f);
        when(bankAccountRepository.getCurrency(receiverId)).thenReturn(CurrencyType.RUB);

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
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfSenderIdIsNull() {
        UUID senderId = null;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(5000.0f);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.pay(transaction));
    }

    @Test
    void validatePayment_shouldThrowIfSenderIdNotFound() {
        UUID senderId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setSenderId(senderId);
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
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
        transaction.setCurrency(CurrencyType.RUB);
        transaction.setAmount(5000.0f);

        when(bankAccountRepository.haveUUID(senderId)).thenReturn(true);
        when(bankAccountRepository.getBalance(senderId)).thenReturn(6000.0f);

        transactionsService.pay(transaction);

        verify(bankAccountRepository).decreaseBalance(senderId, 5000.0f);
    }

    // getRecentTransactions
    @Test
    void getRecentTransactions_shouldThrowIfBankAccountNotFound() {
        UUID accountId = UUID.randomUUID();
        int page = 1;
        int size = 2;

        when(bankAccountRepository.haveUUID(accountId)).thenReturn(false);

        assertThrows(BankAccountNotFoundException.class, () -> transactionsService.getRecentTransactions(accountId, page, size));
    }

    @Test
    void getRecentTransactions_shouldThrowIfPageIsNegative() {
        UUID accountId = UUID.randomUUID();
        int page = -1;
        int size = 2;

        when(bankAccountRepository.haveUUID(accountId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.getRecentTransactions(accountId, page, size));
    }

    @Test
    void getRecentTransactions_shouldThrowIfSizeIsNegative() {
        UUID accountId = UUID.randomUUID();
        int page = 1;
        int size = -2;

        when(bankAccountRepository.haveUUID(accountId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> transactionsService.getRecentTransactions(accountId, page, size));
    }

    @Test
    void getRecentTransactions_shouldReturnRecentTransactions() {
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
        transaction1.setAmount(5000.0f);
        transaction1.setReceiverId(accountId);

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionType(TransactionType.PAYMENT);
        transaction2.setCurrency(CurrencyType.RUB);
        transaction2.setAmount(200.0f);
        transaction2.setSenderId(accountId);

        TransactionDTO transactionDTO1 = new TransactionDTO();
        transactionDTO1.setTransactionType(TransactionType.DEPOSIT);
        transactionDTO1.setCurrency(CurrencyType.RUB);
        transactionDTO1.setAmount(5000.0f);
        transactionDTO1.setReceiverName("Test");

        TransactionDTO transactionDTO2 = new TransactionDTO();
        transactionDTO2.setTransactionType(TransactionType.PAYMENT);
        transactionDTO2.setCurrency(CurrencyType.RUB);
        transactionDTO2.setAmount(200.0f);
        transactionDTO2.setSenderName("Test");

        List<Transaction> transactions = List.of(transaction1, transaction2);
        List<TransactionDTO> transactionDTOS = List.of(transactionDTO1, transactionDTO2);
        RecentTransactionsDTO recentTransactionsDTO = new RecentTransactionsDTO(total, page, size, transactionDTOS);

        when(bankAccountRepository.haveUUID(accountId)).thenReturn(true);
        when(bankAccountRepository.getUser(accountId)).thenReturn(userId);
        when(transactionsRepository.getTransactions(accountId, page, size)).thenReturn(transactions);
        when(transactionsRepository.getTransactionsCount(accountId)).thenReturn(total);
        when(userRepository.getUser(userId)).thenReturn(user);

        RecentTransactionsDTO result = transactionsService.getRecentTransactions(accountId, page, size);

        assertNotNull(result);
        assertEquals(recentTransactionsDTO, result);

        verify(bankAccountRepository).haveUUID(accountId);
        verify(transactionsRepository).getTransactions(accountId, page, size);
    }
}
