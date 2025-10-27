package com.example.xbankbackend.repositories;

import com.example.xbankbackend.generated.tables.Transactions;
import com.example.xbankbackend.models.Transaction;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionsRepository {
    private final DSLContext dsl;

    @Autowired
    public TransactionsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void addPayment(Transaction transaction) {
        dsl.insertInto(Transactions.TRANSACTIONS)
                .values(transaction.getTransactionId(), transaction.getSenderId(), transaction.getReceiverId(), transaction.getAmount(), transaction.getCurrency(), transaction.getDate())
                .execute();
    }
}
