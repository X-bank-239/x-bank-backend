package com.example.xbankbackend.repositories;

import com.example.xbankbackend.generated.enums.TransactionStatus;
import com.example.xbankbackend.models.Transaction;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.TRANSACTIONS;

@AllArgsConstructor
@Repository
public class TransactionsRepository {

    private final DSLContext dsl;

    // TODO: переделать с помощью .set()
    public void addTransaction(Transaction transaction) {
        dsl.insertInto(TRANSACTIONS)
                .values(transaction.getTransactionId(), transaction.getSenderId(), transaction.getReceiverId(), transaction.getAmount(), transaction.getCurrency(), transaction.getTransactionDate(), transaction.getTransactionType(), transaction.getComment(), transaction.getStatus(), transaction.getCategory())
                .execute();
    }

    public Transaction get(UUID transactionId) {
        return dsl.selectFrom(TRANSACTIONS)
                .where(TRANSACTIONS.TRANSACTION_ID.eq(transactionId))
                .fetchOne()
                .into(Transaction.class);
    }

    public boolean exists(UUID transactionId) {
        return dsl.selectFrom(TRANSACTIONS)
                .where(TRANSACTIONS.TRANSACTION_ID.eq(transactionId))
                .fetch()
                .size() == 1;
    }

    public void cancel(UUID transactionId) {
        dsl.update(TRANSACTIONS)
                .set(TRANSACTIONS.STATUS, TransactionStatus.CANCELLED)
                .where(TRANSACTIONS.TRANSACTION_ID.eq(transactionId))
                .execute();
    }

    public List<Transaction> getTransactions(UUID accountId, int page, int size) {
        int offset = page * size;

        return dsl.select()
                .from(TRANSACTIONS)
                .where(TRANSACTIONS.SENDER_ID.eq(accountId)).or(TRANSACTIONS.RECEIVER_ID.eq(accountId))
                .orderBy(TRANSACTIONS.TRANSACTION_DATE.desc())
                .limit(size)
                .offset(offset)
                .fetchInto(Transaction.class);
    }

    public List<Transaction> getTransactionsByCategory(UUID accountId, String categoryCode) {
        return dsl.selectFrom(TRANSACTIONS)
                .where(TRANSACTIONS.SENDER_ID.eq(accountId).or(TRANSACTIONS.RECEIVER_ID.eq(accountId)))
                .and(TRANSACTIONS.CATEGORY.eq(categoryCode))
                .orderBy(TRANSACTIONS.TRANSACTION_DATE.desc())
                .fetchInto(Transaction.class);
    }

    public Integer getTransactionsCount(UUID accountId) {
        return dsl.select()
                .from(TRANSACTIONS)
                .where(TRANSACTIONS.SENDER_ID.eq(accountId)).or(TRANSACTIONS.RECEIVER_ID.eq(accountId))
                .fetch()
                .size();
    }
}
