package com.example.xbankbackend.services;

import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service("ownershipService")
public class OwnershipService {

    private BankAccountRepository bankAccountRepository;
    private TransactionsRepository transactionsRepository;

    public boolean isAccountOwner(UUID accountId, Authentication auth) {
        if (!bankAccountRepository.exists(accountId)) {
            return false;
        }
        UUID userId = UUID.fromString(auth.getName());
        UUID accountUserId = bankAccountRepository.getUserId(accountId);
        return userId.equals(accountUserId);
    }

    public boolean isTransactionOwner(UUID transactionId, Authentication auth) {
        if (!transactionsRepository.exists(transactionId)) {
            return false;
        }
        UUID userId = UUID.fromString(auth.getName());

        Transaction transaction = transactionsRepository.get(transactionId);
        UUID senderId = transaction.getSenderId(), receiverId = transaction.getReceiverId();
        UUID senderUserId = null, receiverUserId = null;

        if (senderId != null) {
            senderUserId = bankAccountRepository.getUserId(senderId);
        }
        if (receiverId != null) {
            receiverUserId = bankAccountRepository.getUserId(receiverId);
        }
        return userId.equals(senderUserId) || userId.equals(receiverUserId);
    }
}
