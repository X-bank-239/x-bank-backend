package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.RecentTransactionsDTO;
import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.exceptions.DifferentCurrencyException;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransactionsService {
    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    public void depositAccount(Transaction deposit) {
        validateDeposit(deposit);

        UUID receiverId = deposit.getReceiverId();
        float amount = deposit.getAmount();

        deposit.setTransactionId(UUID.randomUUID());
        deposit.setTransactionDate(OffsetDateTime.now());

        transactionsRepository.addTransaction(deposit);
        bankAccountRepository.increaseBalance(receiverId, amount);
    }

    public void transferMoney(Transaction transfer) {
        validateTransfer(transfer);

        UUID senderId = transfer.getSenderId();
        UUID receiverId = transfer.getReceiverId();
        float amount = transfer.getAmount();

        transfer.setTransactionId(UUID.randomUUID());
        transfer.setTransactionDate(OffsetDateTime.now());

        transactionsRepository.addTransaction(transfer);
        bankAccountRepository.decreaseBalance(senderId, amount);
        bankAccountRepository.increaseBalance(receiverId, amount);
    }

    public void pay(Transaction payment) {
        validatePayment(payment);

        UUID senderId = payment.getSenderId();
        float amount = payment.getAmount();

        payment.setTransactionId(UUID.randomUUID());
        payment.setTransactionDate(OffsetDateTime.now());

        transactionsRepository.addTransaction(payment);
        bankAccountRepository.decreaseBalance(senderId, amount);
    }

    private void validateDeposit(Transaction deposit) {
        String currency = deposit.getCurrency();
        UUID receiverId = deposit.getReceiverId();
        UUID senderId = deposit.getSenderId();
        Float amount = deposit.getAmount();
        String transactionType = deposit.getTransactionType().toString();

        if (!transactionType.equals("DEPOSIT")) {
            throw new IllegalArgumentException("Method name " + transactionType + " is not allowed (Deposit)");
        }

        if (receiverId == null) {
            throw new IllegalArgumentException("ReceiverId cannot be null (Deposit)");
        }

        if (senderId != null) {
            throw new IllegalArgumentException("SenderId must be null (Deposit)");
        }

        if (!bankAccountRepository.haveUUID(receiverId)) {
            throw new UserNotFoundException("No such receiver Id " + receiverId);
        }

        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // TODO: конвертация валют
        String receiverCurrency = bankAccountRepository.getCurrency(receiverId);
        if (!Objects.equals(currency, receiverCurrency)) {
            throw new DifferentCurrencyException("Currency " + currency + " doesn't equal " + receiverCurrency);
        }
    }

    private void validateTransfer(Transaction transfer) {
        String currency = transfer.getCurrency();
        UUID receiverId = transfer.getReceiverId();
        UUID senderId = transfer.getSenderId();
        Float amount = transfer.getAmount();
        String transactionType = transfer.getTransactionType().toString();

        if (!transactionType.equals("TRANSFER")) {
            throw new IllegalArgumentException("Method name " + transactionType + " is not allowed (Transfer)");
        }

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("Both SenderId and userId are required (Transfer)");
        }

        if (!bankAccountRepository.haveUUID(receiverId)) {
            throw new UserNotFoundException("No such receiver Id " + receiverId);
        }

        if (!bankAccountRepository.haveUUID(senderId)) {
            throw new UserNotFoundException("No such sender Id " + senderId);
        }

        if (amount > bankAccountRepository.getBalance(senderId)) {
            throw new InsufficientFundsException("Sender balance must be greater than transaction amount");
        }

        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // TODO: конвертация валют
        String receiverCurrency = bankAccountRepository.getCurrency(receiverId);
        if (!Objects.equals(currency, receiverCurrency)) {
            throw new DifferentCurrencyException("Currency " + currency + " doesn't equal " + receiverCurrency);
        }
    }

    private void validatePayment(Transaction payment) {
        UUID receiverId = payment.getReceiverId();
        UUID senderId = payment.getSenderId();
        Float amount = payment.getAmount();
        String transactionType = payment.getTransactionType().toString();

        if (!transactionType.equals("PAYMENT")) {
            throw new IllegalArgumentException("Method name " + transactionType + " is not allowed (Payment)");
        }

        if (senderId == null) {
            throw new IllegalArgumentException("SenderId cannot be null (Payment)");
        }

        if (receiverId != null) {
            throw new IllegalArgumentException("ReceiverId must be null (Payment)");
        }

        if (!bankAccountRepository.haveUUID(senderId)) {
            throw new UserNotFoundException("No such sender Id " + senderId);
        }

        if (amount > bankAccountRepository.getBalance(senderId)) {
            throw new InsufficientFundsException("Sender balance must be greater than transaction amount");
        }

        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public RecentTransactionsDTO getRecentTransactions(UUID accountId, int page, int size) {
        if (!bankAccountRepository.haveUUID(accountId)) {
            throw new BankAccountNotFoundException("Bank account with UUID " + accountId + " doesn't exist");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page "  + page + " cannot be negative");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Size " + size + " cannot be negative");
        }

        return transactionsRepository.getTransactions(accountId, page, size);
    }
}
