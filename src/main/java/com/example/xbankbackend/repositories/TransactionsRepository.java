package com.example.xbankbackend.repositories;

import com.example.xbankbackend.config.JOOQConfig;
import com.example.xbankbackend.generated.tables.Payments;
import com.example.xbankbackend.models.Payment;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class TransactionsRepository {
    private final DSLContext dsl = JOOQConfig.createDSLContext();

    public TransactionsRepository() throws SQLException {
    }

    public void addPayment(Payment payment) {
        dsl.insertInto(Payments.PAYMENTS)
                .values(payment.getPaymentId(), payment.getSenderId(), payment.getReceiverId(), payment.getAmount(), payment.getCurrency(), payment.getDate())
                .execute();
    }
}
