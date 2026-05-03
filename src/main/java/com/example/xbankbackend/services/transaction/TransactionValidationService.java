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
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
    }

    // OwnershipService
    public void validateUserIsOwner(UUID accountId, UUID authenticatedUserId) {
        UUID ownerId = bankAccountRepository.getUserId(accountId);
        if (!authenticatedUserId.equals(ownerId)) {
            throw new UserIsNotABankAccountOwner("Пользователь не является владельцем аккаунта");
        }
    }

    public void validateTransactionExists(UUID transactionId) {
        if (!transactionsRepository.exists(transactionId)) {
            throw new TransactionNotFoundException("Транзакция с UUID " + transactionId + " не найдена");
        }
    }

    public void validateDepositStructure(Transaction tx) {
        if (tx.getReceiverId() == null) {
            throw new IllegalArgumentException("ReceiverId не может быть null (Deposit)");
        }
        if (tx.getSenderId() != null) {
            throw new IllegalArgumentException("SenderId должен быть null (Deposit)");
        }
        if (tx.getTransactionType() != TransactionType.DEPOSIT) {
            throw new IllegalArgumentException("Ожидается тип транзакции DEPOSIT");
        }
    }

    public void validateTransferStructure(Transaction tx) {
        if (tx.getSenderId() == null || tx.getReceiverId() == null) {
            throw new IllegalArgumentException("Оба SenderId и ReceiverId не могут быть null (Transfer)");
        }
        if (tx.getTransactionType() != TransactionType.TRANSFER) {
            throw new IllegalArgumentException("Ожидается тип транзакции TRANSFER");
        }
    }

    public void validatePaymentStructure(Transaction tx) {
        if (tx.getSenderId() == null) {
            throw new IllegalArgumentException("SenderId не может быть null (Payment)");
        }
        if (tx.getReceiverId() != null) {
            throw new IllegalArgumentException("ReceiverId должен быть null (Payment)");
        }
        if (tx.getTransactionType() != TransactionType.PAYMENT) {
            throw new IllegalArgumentException("Ожидается тип транзакции PAYMENT");
        }
    }
}
