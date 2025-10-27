package com.example.xbankbackend.repositories;

import com.example.xbankbackend.config.JOOQConfig;
import com.example.xbankbackend.generated.tables.Transactions;
import com.example.xbankbackend.models.Transaction;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class TransactionsRepository {
    private final DSLContext dsl = JOOQConfig.createDSLContext();

    public TransactionsRepository() throws SQLException {
    }

    public void addPayment(Transaction transaction) {
        dsl.insertInto(Transactions.TRANSACTIONS)
                .values(transaction.getTransactionId(), transaction.getSenderId(), transaction.getReceiverId(), transaction.getAmount(), transaction.getCurrency(), transaction.getDate())
                .execute();
    }
}
