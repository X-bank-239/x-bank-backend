package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.responses.RecentTransactionsResponse;
import com.example.xbankbackend.dtos.responses.TransactionResponse;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
import com.example.xbankbackend.exceptions.UserIsNotABankAccountOwner;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.repositories.UserRepository;
import com.example.xbankbackend.services.external.cbr.CurrencyRateService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class TransactionsService {

    private CurrencyRateService currencyRateService;
    private TransactionsRepository transactionsRepository;
    private BankAccountRepository bankAccountRepository;
    private UserRepository userRepository;

    public void deposit(Transaction deposit, UUID authenticatedUserId) {
        validateDeposit(deposit, authenticatedUserId);

        UUID receiverId = deposit.getReceiverId();
        CurrencyType receiverCurrency = bankAccountRepository.getCurrency(receiverId);

        BigDecimal convertedAmount = currencyRateService.convert(deposit.getCurrency(), receiverCurrency, deposit.getAmount());

        deposit.setTransactionId(UUID.randomUUID());
        deposit.setTransactionDate(OffsetDateTime.now());

        transactionsRepository.addTransaction(deposit);
        bankAccountRepository.increaseBalance(receiverId, convertedAmount);
    }

    public void transfer(Transaction transfer, UUID authenticatedUserId) {
        validateTransfer(transfer, authenticatedUserId);

        UUID senderId = transfer.getSenderId();
        UUID receiverId = transfer.getReceiverId();
        CurrencyType receiverCurrency = bankAccountRepository.getCurrency(receiverId);

        BigDecimal amount = transfer.getAmount();
        BigDecimal convertedAmount = currencyRateService.convert(transfer.getCurrency(), receiverCurrency, transfer.getAmount());

        transfer.setTransactionId(UUID.randomUUID());
        transfer.setTransactionDate(OffsetDateTime.now());

        transactionsRepository.addTransaction(transfer);
        bankAccountRepository.decreaseBalance(senderId, amount);
        bankAccountRepository.increaseBalance(receiverId, convertedAmount);
    }

    public void pay(Transaction payment, UUID authenticatedUserId) {
        validatePayment(payment, authenticatedUserId);
        UUID ownerOfBankAccount = bankAccountRepository.getUserId(payment.getReceiverId());
        UUID senderId = payment.getSenderId();
        BigDecimal amount = payment.getAmount();

        payment.setTransactionId(UUID.randomUUID());
        payment.setTransactionDate(OffsetDateTime.now());

        transactionsRepository.addTransaction(payment);
        bankAccountRepository.decreaseBalance(senderId, amount);
    }

    private void validateDeposit(Transaction deposit, UUID authenticatedUserId) {
        CurrencyType currency = deposit.getCurrency();
        UUID receiverId = deposit.getReceiverId();
        UUID senderId = deposit.getSenderId();
        BigDecimal amount = deposit.getAmount();
        TransactionType transactionType = deposit.getTransactionType();

        if (!bankAccountRepository.exists(receiverId)) {
            throw new BankAccountNotFoundException("Bank account with UUID " + receiverId + " doesn't exist");
        }

        UUID accountOwnerId = bankAccountRepository.getUserId(receiverId);

        if (receiverId == null) {
            throw new IllegalArgumentException("ReceiverId cannot be null (Deposit)");
        }
        if (senderId != null) {
            throw new IllegalArgumentException("SenderId must be null (Deposit)");
        }

        if (transactionType != TransactionType.DEPOSIT) {
            throw new IllegalArgumentException("Method name " + deposit.getTransactionType() + " is not allowed (Expected: DEPOSIT)");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (!authenticatedUserId.equals(accountOwnerId)) {
            throw new UserIsNotABankAccountOwner("Authenticated user is not the bank account owner");
        }
    }

    private void validateTransfer(Transaction transfer, UUID authenticatedUserId) {
        CurrencyType currency = transfer.getCurrency();
        UUID receiverId = transfer.getReceiverId();
        UUID senderId = transfer.getSenderId();
        BigDecimal amount = transfer.getAmount();
        TransactionType transactionType = transfer.getTransactionType();

        if (!bankAccountRepository.exists(receiverId)) {
            throw new BankAccountNotFoundException("No such receiver Id " + receiverId);
        }

        UUID accountOwnerId = bankAccountRepository.getUserId(receiverId);

        if (!bankAccountRepository.exists(senderId)) {
            throw new BankAccountNotFoundException("No such sender Id " + senderId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (transactionType != TransactionType.TRANSFER) {
            throw new IllegalArgumentException("Method name " + transactionType + " is not allowed (Transfer)");
        }

        if (transfer.getSenderId() == null || transfer.getReceiverId() == null) {
            throw new IllegalArgumentException("Both SenderId and userId are required (Transfer)");
        }


        if (amount.compareTo(bankAccountRepository.getBalance(senderId)) > 0) {
            throw new InsufficientFundsException("Sender balance must be greater than transaction amount");
        }

        if (!authenticatedUserId.equals(accountOwnerId) ) {
            throw new UserIsNotABankAccountOwner("Authenticated user is not the bank account owner");
        }

    }

    private void validatePayment(Transaction payment, UUID authenticatedUserId) {
        CurrencyType currency = payment.getCurrency();
        UUID receiverId = payment.getReceiverId();
        UUID senderId = payment.getSenderId();
        BigDecimal amount = payment.getAmount();
        TransactionType transactionType = payment.getTransactionType();
//
//        if (!bankAccountRepository.exists(receiverId)) {
//            throw new BankAccountNotFoundException("No such receiver Id " + receiverId);
//        }
        UUID userReceiverId = bankAccountRepository.getUserId(payment.getReceiverId());

        if (!bankAccountRepository.exists(senderId)) {
            throw new BankAccountNotFoundException("No such sender Id " + senderId); // user to account
        }

        if (transactionType != TransactionType.PAYMENT) {
            throw new IllegalArgumentException("Method name " + transactionType + " is not allowed (Payment)");
        }

        if (senderId == null) {
            throw new IllegalArgumentException("SenderId cannot be null (Payment)");
        }

        if (receiverId != null) {
            throw new IllegalArgumentException("ReceiverId must be null (Payment)");
        }

        if (amount.compareTo(bankAccountRepository.getBalance(senderId)) > 0) {
            throw new InsufficientFundsException("Sender balance must be greater than transaction amount");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (!authenticatedUserId.equals(userReceiverId) ) {
            throw new UserIsNotABankAccountOwner("Authenticated user is not the bank account owner");
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
