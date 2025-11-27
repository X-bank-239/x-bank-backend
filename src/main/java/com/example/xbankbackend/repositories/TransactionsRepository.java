package com.example.xbankbackend.repositories;

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

    public void addTransaction(Transaction transaction) {
        dsl.insertInto(TRANSACTIONS)
                .values(transaction.getTransactionId(), transaction.getSenderId(), transaction.getReceiverId(), transaction.getAmount(), transaction.getCurrency(), transaction.getTransactionDate(), transaction.getTransactionType(), transaction.getComment())
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

    public Integer getTransactionsCount(UUID accountId) {
        return dsl.select()
                .from(TRANSACTIONS)
                .where(TRANSACTIONS.SENDER_ID.eq(accountId)).or(TRANSACTIONS.RECEIVER_ID.eq(accountId))
                .fetch()
                .size();
    }
}
