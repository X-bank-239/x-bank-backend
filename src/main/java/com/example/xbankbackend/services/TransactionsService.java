package com.example.xbankbackend.services;

import com.example.xbankbackend.models.Payment;
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

    public void topUpAccount(Payment payment) {
        UUID receiverId = payment.getReceiverId();
        UUID senderId = payment.getSenderId();
        float amount = payment.getAmount();
        String currency = payment.getCurrency();

        if (!bankAccountRepository.haveUUID(receiverId)) {
            throw new IllegalArgumentException("No such receiver Id " + receiverId);
        }
        if (senderId != null) {
            throw new IllegalArgumentException("Receiver Id " + receiverId + " should be null. Sender Id: " + senderId);
        }
        // TODO: конвертация валют
        String receiverCurrency = bankAccountRepository.getCurrency(receiverId);
        if (!Objects.equals(currency, receiverCurrency)) {
            throw new IllegalArgumentException("Currency " + currency + " doesn't equal " + receiverCurrency);
        }

        payment.setPaymentId(UUID.randomUUID());
        payment.setSenderId(receiverId);
        payment.setDate(new Timestamp(new Date().getTime()));

        transactionsRepository.addPayment(payment);
        bankAccountRepository.changeBalance(receiverId, amount);
    }
}
