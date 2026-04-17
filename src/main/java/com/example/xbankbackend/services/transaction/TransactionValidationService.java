package com.example.xbankbackend.services.transaction;

import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.TransactionNotFoundException;
import com.example.xbankbackend.exceptions.UserIsNotABankAccountOwner;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Service
public class TransactionValidationService {

    private TransactionsRepository transactionsRepository;
    private BankAccountRepository bankAccountRepository;

    public void validateAmountPositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    // OwnershipService
    public void validateUserIsOwner(UUID accountId, UUID authenticatedUserId) {
        UUID ownerId = bankAccountRepository.getUserId(accountId);
        if (!authenticatedUserId.equals(ownerId)) {
            throw new UserIsNotABankAccountOwner("Authenticated user is not the bank account owner");
        }
    }

    public void validateTransactionExists(UUID transactionId) {
        if (!transactionsRepository.exists(transactionId)) {
            throw new TransactionNotFoundException("Transaction with id " + transactionId + " not found");
        }
    }

    public void validateDepositStructure(Transaction tx) {
        if (tx.getReceiverId() == null) {
            throw new IllegalArgumentException("ReceiverId cannot be null (Deposit)");
        }
        if (tx.getSenderId() != null) {
            throw new IllegalArgumentException("SenderId must be null (Deposit)");
        }
        if (tx.getTransactionType() != TransactionType.DEPOSIT) {
            throw new IllegalArgumentException("Expected transaction type DEPOSIT");
        }
    }

    public void validateTransferStructure(Transaction tx) {
        if (tx.getSenderId() == null || tx.getReceiverId() == null) {
            throw new IllegalArgumentException("Both SenderId and ReceiverId are required (Transfer)");
        }
        if (tx.getTransactionType() != TransactionType.TRANSFER) {
            throw new IllegalArgumentException("Expected transaction type TRANSFER");
        }
    }

    public void validatePaymentStructure(Transaction tx) {
        if (tx.getSenderId() == null) {
            throw new IllegalArgumentException("SenderId cannot be null (Payment)");
        }
        if (tx.getReceiverId() != null) {
            throw new IllegalArgumentException("ReceiverId must be null (Payment)");
        }
        if (tx.getTransactionType() != TransactionType.PAYMENT) {
            throw new IllegalArgumentException("Expected transaction type PAYMENT");
        }
    }
}
