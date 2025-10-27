package com.example.xbankbackend.services;

import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransactionsService {
    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    public void depositAccount(Transaction deposit) {
        validateTransaction(deposit);

        UUID receiverId = deposit.getReceiverId();
        float amount = deposit.getAmount();

        deposit.setTransactionId(UUID.randomUUID());
        deposit.setDate(new Timestamp(new Date().getTime()));

        transactionsRepository.addPayment(deposit);
        bankAccountRepository.changeBalance(receiverId, amount);
    }

    public void transferMoney(Transaction transfer) {
        validateTransaction(transfer);

        UUID senderId = transfer.getSenderId();
        UUID receiverId = transfer.getReceiverId();
        float amount = transfer.getAmount();

        transfer.setTransactionId(UUID.randomUUID());
        transfer.setDate(new Timestamp(new Date().getTime()));

        transactionsRepository.addPayment(transfer);
        bankAccountRepository.changeBalance(senderId, -amount);
        bankAccountRepository.changeBalance(receiverId, amount);
    }

    public void pay(Transaction payment) {
        validateTransaction(payment);

        UUID senderId = payment.getReceiverId();
        float amount = payment.getAmount();

        payment.setTransactionId(UUID.randomUUID());
        payment.setDate(new Timestamp(new Date().getTime()));

        transactionsRepository.addPayment(payment);
        bankAccountRepository.changeBalance(senderId, -amount);
    }

    private void validateTransaction(Transaction transaction) {
        TransactionType transactionType = transaction.getTransactionType();
        switch (transactionType) {
            case Deposit:
                if (transaction.getReceiverId() == null) {
                    throw new IllegalArgumentException("ReceiverId cannot be null (Deposit)");
                }
                break;
            case Payment:
                if (transaction.getSenderId() == null) {
                    throw new IllegalArgumentException("SenderId cannot be null (Payment)");
                }
                break;
            case Transfer:
                if (transaction.getSenderId() == null || transaction.getReceiverId() == null) {
                    throw new IllegalArgumentException("Both SenderId and userId are required (Transfer)");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown transaction type " + transactionType);
        }

        String currency = transaction.getCurrency();
        UUID receiverId = transaction.getReceiverId();
        if (!bankAccountRepository.haveUUID(receiverId)) {
            throw new IllegalArgumentException("No such receiver Id " + receiverId);
        }
        // TODO: конвертация валют
        String receiverCurrency = bankAccountRepository.getCurrency(receiverId);
        if (!Objects.equals(currency, receiverCurrency)) {
            throw new IllegalArgumentException("Currency " + currency + " doesn't equal " + receiverCurrency);
        }
    }
}
