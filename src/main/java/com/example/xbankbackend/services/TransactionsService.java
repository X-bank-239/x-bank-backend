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
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class TransactionsService {

    private TransactionsRepository transactionsRepository;
    private BankAccountRepository bankAccountRepository;
    private UserRepository userRepository;

    public void deposit(Transaction deposit) {
        validateDeposit(deposit);

        UUID receiverId = deposit.getReceiverId();
        float amount = deposit.getAmount();

        deposit.setTransactionId(UUID.randomUUID());
        deposit.setTransactionDate(OffsetDateTime.now());

        transactionsRepository.addTransaction(deposit);
        bankAccountRepository.increaseBalance(receiverId, amount);
    }

    public void transfer(Transaction transfer) {
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
        CurrencyType currency = deposit.getCurrency();
        UUID receiverId = deposit.getReceiverId();
        UUID senderId = deposit.getSenderId();
        Float amount = deposit.getAmount();
        TransactionType transactionType = deposit.getTransactionType();

        if (transactionType != TransactionType.DEPOSIT) {
            throw new IllegalArgumentException("Method name " + transactionType.toString() + " is not allowed (Deposit)");
        }

        if (receiverId == null) {
            throw new IllegalArgumentException("ReceiverId cannot be null (Deposit)");
        }

        if (senderId != null) {
            throw new IllegalArgumentException("SenderId must be null (Deposit)");
        }

        if (!bankAccountRepository.exists(receiverId)) {
            throw new UserNotFoundException("No such receiver Id " + receiverId);
        }

        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // TODO: конвертация валют
        CurrencyType receiverCurrency = bankAccountRepository.getCurrency(receiverId);
        if (currency != receiverCurrency) {
            throw new DifferentCurrencyException("Currency " + currency.toString() + " doesn't equal " + receiverCurrency);
        }
    }

    private void validateTransfer(Transaction transfer) {
        CurrencyType currency = transfer.getCurrency();
        UUID receiverId = transfer.getReceiverId();
        UUID senderId = transfer.getSenderId();
        Float amount = transfer.getAmount();
        TransactionType transactionType = transfer.getTransactionType();

        if (transactionType != TransactionType.TRANSFER) {
            throw new IllegalArgumentException("Method name " + transactionType + " is not allowed (Transfer)");
        }

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("Both SenderId and userId are required (Transfer)");
        }

        if (!bankAccountRepository.exists(receiverId)) {
            throw new UserNotFoundException("No such receiver Id " + receiverId);
        }

        if (!bankAccountRepository.exists(senderId)) {
            throw new UserNotFoundException("No such sender Id " + senderId);
        }

        if (amount > bankAccountRepository.getBalance(senderId)) {
            throw new InsufficientFundsException("Sender balance must be greater than transaction amount");
        }

        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // TODO: конвертация валют
        CurrencyType receiverCurrency = bankAccountRepository.getCurrency(receiverId);
        if (currency != receiverCurrency) {
            throw new DifferentCurrencyException("Currency " + currency.toString() + " doesn't equal " + receiverCurrency.toString());
        }
    }

    private void validatePayment(Transaction payment) {
        UUID receiverId = payment.getReceiverId();
        UUID senderId = payment.getSenderId();
        Float amount = payment.getAmount();
        TransactionType transactionType = payment.getTransactionType();

        if (transactionType != TransactionType.PAYMENT) {
            throw new IllegalArgumentException("Method name " + transactionType + " is not allowed (Payment)");
        }

        if (senderId == null) {
            throw new IllegalArgumentException("SenderId cannot be null (Payment)");
        }

        if (receiverId != null) {
            throw new IllegalArgumentException("ReceiverId must be null (Payment)");
        }

        if (!bankAccountRepository.exists(senderId)) {
            throw new UserNotFoundException("No such sender Id " + senderId);
        }

        if (amount > bankAccountRepository.getBalance(senderId)) {
            throw new InsufficientFundsException("Sender balance must be greater than transaction amount");
        }

        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public RecentTransactionsResponse getRecent(UUID accountId, int page, int size) {
        if (!bankAccountRepository.exists(accountId)) {
            throw new BankAccountNotFoundException("Bank account with UUID " + accountId + " doesn't exist");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page "  + page + " cannot be negative");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Size " + size + " cannot be negative");
        }

        List<Transaction> transactions = transactionsRepository.getTransactions(accountId, page, size);

        List<TransactionResponse> transactionResponses = new java.util.ArrayList<>(List.of());

        for (Transaction transaction : transactions) {
            // TODO: оптимизировать
            UUID senderId = transaction.getSenderId();
            UUID receiverId = transaction.getReceiverId();
            String senderName = null, receiverName = null;
            if (senderId != null) {
                UUID senderIdUser = bankAccountRepository.getUserId(senderId);
                senderName = userRepository.getUser(senderIdUser).getFirstName();
            }
            if (receiverId != null) {
                UUID receiverIdUser = bankAccountRepository.getUserId(receiverId);
                receiverName = userRepository.getUser(receiverIdUser).getFirstName();
            }

            TransactionResponse currentDTO = new TransactionResponse();
            currentDTO.setTransactionType(transaction.getTransactionType());
            currentDTO.setSenderName(senderName);
            currentDTO.setReceiverName(receiverName);
            currentDTO.setAmount(transaction.getAmount());
            currentDTO.setCurrency(transaction.getCurrency());
            currentDTO.setTransactionDate(transaction.getTransactionDate());
            currentDTO.setComment(transaction.getComment());

            transactionResponses.add(currentDTO);
        }

        int total = transactionsRepository.getTransactionsCount(accountId);

        return new RecentTransactionsResponse(total, page, size, transactionResponses);
    }
}
