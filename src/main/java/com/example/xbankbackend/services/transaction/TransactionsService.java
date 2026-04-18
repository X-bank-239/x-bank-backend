package com.example.xbankbackend.services.transaction;

import com.example.xbankbackend.dtos.responses.RecentTransactionsResponse;
import com.example.xbankbackend.dtos.responses.TransactionResponse;
import com.example.xbankbackend.enums.TransactionStatus;
import com.example.xbankbackend.mappers.TransactionMapper;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.services.FeeService;
import com.example.xbankbackend.services.bankAccount.BankAccountValidationService;
import com.example.xbankbackend.services.transactionCategories.TransactionCategoriesService;
import com.example.xbankbackend.services.transactionCategories.TransactionCategoriesValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class TransactionsService {

    private TransactionsRepository transactionsRepository;
    private TransactionMapper transactionMapper;
    private TransactionCategoriesService categoriesService;

    private final TransactionValidationService transactionValidationService;
    private final BankAccountValidationService bankAccountValidationService;
    private final TransactionCategoriesValidationService categoriesValidationService;
    private final BalanceOperationService balanceOperationService;
    private final FeeService feeService;

    public TransactionResponse deposit(Transaction tx, UUID authenticatedUserId) {
        UUID receiverId = tx.getReceiverId();

        bankAccountValidationService.validateBankAccountExists(receiverId);
        bankAccountValidationService.validateBankAccountActive(receiverId);

        transactionValidationService.validateDepositStructure(tx);
        transactionValidationService.validateAmountPositive(tx.getAmount());

        prepareAndSaveTransaction(tx);
        balanceOperationService.increaseBalance(receiverId, tx.getAmount(), tx.getCurrency());

        TransactionResponse transactionResponse = transactionMapper.transactionToResponse(tx);
        transactionResponse.setCommission(BigDecimal.ZERO);

        return transactionResponse;
    }

    public TransactionResponse transfer(Transaction tx, UUID authenticatedUserId) {
        UUID receiverId = tx.getReceiverId();
        UUID senderId = tx.getSenderId();

        bankAccountValidationService.validateBankAccountExists(receiverId);
        bankAccountValidationService.validateBankAccountExists(senderId);

        bankAccountValidationService.validateBankAccountActive(receiverId);
        bankAccountValidationService.validateBankAccountActive(senderId);

        transactionValidationService.validateTransferStructure(tx);
        transactionValidationService.validateUserIsOwner(senderId, authenticatedUserId);
        transactionValidationService.validateAmountPositive(tx.getAmount());

        bankAccountValidationService.validateSufficientFundsWithFee(senderId, tx.getAmount());

        prepareAndSaveTransaction(tx);

        balanceOperationService.decreaseBalanceWithFee(senderId, tx.getAmount(), tx.getCurrency());
        balanceOperationService.increaseBalance(receiverId, tx.getAmount(), tx.getCurrency());

        TransactionResponse transactionResponse = transactionMapper.transactionToResponse(tx);
        transactionResponse.setCommission(feeService.getBaseFeeAmount(tx.getAmount()));

        return transactionResponse;
    }

    public TransactionResponse pay(Transaction tx, UUID authenticatedUserId) {
        UUID senderId = tx.getSenderId();

        bankAccountValidationService.validateBankAccountExists(senderId);
        bankAccountValidationService.validateBankAccountActive(senderId);

        transactionValidationService.validatePaymentStructure(tx);
        transactionValidationService.validateUserIsOwner(senderId, authenticatedUserId);
        transactionValidationService.validateAmountPositive(tx.getAmount());

        bankAccountValidationService.validateSufficientFundsWithFee(senderId, tx.getAmount());

        prepareAndSaveTransaction(tx);

        balanceOperationService.decreaseBalanceWithFee(senderId, tx.getAmount(), tx.getCurrency());

        TransactionResponse transactionResponse = transactionMapper.transactionToResponse(tx);
        transactionResponse.setCommission(feeService.getBaseFeeAmount(tx.getAmount()));

        return transactionResponse;
    }

    private void prepareAndSaveTransaction(Transaction tx) {
        tx.setTransactionId(UUID.randomUUID());
        tx.setTransactionDate(OffsetDateTime.now());
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setCategory(categoriesService.findCategory(tx.getComment()));

        transactionsRepository.addTransaction(tx);
    }

    public TransactionResponse getTransaction(UUID transactionId) {
        transactionValidationService.validateTransactionExists(transactionId);

        Transaction transaction = transactionsRepository.get(transactionId);
        return transactionMapper.transactionToResponse(transaction);
    }

    public void cancelTransaction(UUID transactionId) {
        transactionValidationService.validateTransactionExists(transactionId);

        Transaction transaction = transactionsRepository.get(transactionId);
        UUID senderId = transaction.getSenderId(), receiverId = transaction.getReceiverId();

        if (senderId != null) {
            balanceOperationService.increaseBalance(senderId, transaction.getAmount(), transaction.getCurrency());
        }
        if (receiverId != null) {
            balanceOperationService.decreaseBalance(receiverId, transaction.getAmount(), transaction.getCurrency());
        }

        transactionsRepository.cancel(transactionId);
    }

    public List<Transaction> getTransactionsByCategory(UUID accountId, String categoryCode) {
        bankAccountValidationService.validateBankAccountExists(accountId);
        categoriesValidationService.validateCategoryExists(categoryCode);

        return transactionsRepository.getTransactionsByCategory(accountId, categoryCode);
    }

    public RecentTransactionsResponse getRecent(UUID accountId, int page, int size) {
        bankAccountValidationService.validateBankAccountExists(accountId);

        if (page < 0) {
            throw new IllegalArgumentException("Page "  + page + " cannot be negative");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Size " + size + " cannot be negative");
        }

        List<Transaction> transactions = transactionsRepository.getTransactions(accountId, page, size);

        List<TransactionResponse> transactionResponses = new java.util.ArrayList<>(List.of());

        for (Transaction transaction : transactions) {
            TransactionResponse currentDTO = transactionMapper.transactionToResponse(transaction);

            transactionResponses.add(currentDTO);
        }

        int total = transactionsRepository.getTransactionsCount(accountId);

        return new RecentTransactionsResponse(total, page, size, transactionResponses);
    }
}
