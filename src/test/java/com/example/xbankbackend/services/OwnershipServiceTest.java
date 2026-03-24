package com.example.xbankbackend.services;

import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OwnershipServiceTest {
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OwnershipService ownershipService;

    private UUID userId;
    private UUID otherUserId;
    private UUID accountId;
    private UUID transactionId;
    private UUID senderAccountId;
    private UUID receiverAccountId;

    @BeforeEach
    void setUp() {
        ownershipService = new OwnershipService(bankAccountRepository, transactionsRepository);
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        senderAccountId = UUID.randomUUID();
        receiverAccountId = UUID.randomUUID();
    }

    @Test
    void isAccountOwner_AccountDoesNotExist_ReturnsFalse() {
        when(bankAccountRepository.exists(accountId)).thenReturn(false);

        boolean result = ownershipService.isAccountOwner(accountId, authentication);

        assertFalse(result);
        verify(bankAccountRepository).exists(accountId);
        verify(authentication, never()).getName();
    }

    @Test
    void isAccountOwner_UserIsOwner_ReturnsTrue() {
        when(bankAccountRepository.exists(accountId)).thenReturn(true);
        when(authentication.getName()).thenReturn(userId.toString());
        when(bankAccountRepository.getUserId(accountId)).thenReturn(userId);

        boolean result = ownershipService.isAccountOwner(accountId, authentication);

        assertTrue(result);
        verify(bankAccountRepository).exists(accountId);
        verify(bankAccountRepository).getUserId(accountId);
    }

    @Test
    void isAccountOwner_UserIsNotOwner_ReturnsFalse() {
        when(bankAccountRepository.exists(accountId)).thenReturn(true);
        when(authentication.getName()).thenReturn(userId.toString());
        when(bankAccountRepository.getUserId(accountId)).thenReturn(otherUserId);

        boolean result = ownershipService.isAccountOwner(accountId, authentication);

        assertFalse(result);
        verify(bankAccountRepository).exists(accountId);
        verify(bankAccountRepository).getUserId(accountId);
    }

    @Test
    void isTransactionOwner_TransactionDoesNotExist_ReturnsFalse() {
        when(transactionsRepository.exists(transactionId)).thenReturn(false);

        boolean result = ownershipService.isTransactionOwner(transactionId, authentication);

        assertFalse(result);
        verify(transactionsRepository).exists(transactionId);
        verify(authentication, never()).getName();
    }

    @Test
    void isTransactionOwner_UserIsSender_ReturnsTrue() {
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderAccountId);
        transaction.setReceiverId(receiverAccountId);

        when(transactionsRepository.exists(transactionId)).thenReturn(true);
        when(transactionsRepository.get(transactionId)).thenReturn(transaction);
        when(authentication.getName()).thenReturn(userId.toString());
        when(bankAccountRepository.getUserId(senderAccountId)).thenReturn(userId);

        boolean result = ownershipService.isTransactionOwner(transactionId, authentication);

        assertTrue(result);
        verify(transactionsRepository).exists(transactionId);
        verify(transactionsRepository).get(transactionId);
        verify(bankAccountRepository).getUserId(senderAccountId);
    }

    @Test
    void isTransactionOwner_UserIsReceiver_ReturnsTrue() {
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderAccountId);
        transaction.setReceiverId(receiverAccountId);

        when(transactionsRepository.exists(transactionId)).thenReturn(true);
        when(transactionsRepository.get(transactionId)).thenReturn(transaction);
        when(authentication.getName()).thenReturn(userId.toString());
        when(bankAccountRepository.getUserId(senderAccountId)).thenReturn(otherUserId);
        when(bankAccountRepository.getUserId(receiverAccountId)).thenReturn(userId);

        boolean result = ownershipService.isTransactionOwner(transactionId, authentication);

        assertTrue(result);
        verify(transactionsRepository).exists(transactionId);
        verify(transactionsRepository).get(transactionId);
        verify(bankAccountRepository).getUserId(senderAccountId);
        verify(bankAccountRepository).getUserId(receiverAccountId);
    }

    @Test
    void isTransactionOwner_UserIsNeitherSenderNorReceiver_ReturnsFalse() {
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderAccountId);
        transaction.setReceiverId(receiverAccountId);

        when(transactionsRepository.exists(transactionId)).thenReturn(true);
        when(transactionsRepository.get(transactionId)).thenReturn(transaction);
        when(authentication.getName()).thenReturn(userId.toString());
        when(bankAccountRepository.getUserId(senderAccountId)).thenReturn(otherUserId);
        when(bankAccountRepository.getUserId(receiverAccountId)).thenReturn(otherUserId);

        boolean result = ownershipService.isTransactionOwner(transactionId, authentication);

        assertFalse(result);
        verify(transactionsRepository).exists(transactionId);
        verify(transactionsRepository).get(transactionId);
        verify(bankAccountRepository).getUserId(senderAccountId);
        verify(bankAccountRepository).getUserId(receiverAccountId);
    }

    @Test
    void isTransactionOwner_SenderIsNull_UserIsReceiver_ReturnsTrue() {
        Transaction transaction = new Transaction();
        transaction.setSenderId(null);
        transaction.setReceiverId(receiverAccountId);

        when(transactionsRepository.exists(transactionId)).thenReturn(true);
        when(transactionsRepository.get(transactionId)).thenReturn(transaction);
        when(authentication.getName()).thenReturn(userId.toString());
        when(bankAccountRepository.getUserId(receiverAccountId)).thenReturn(userId);

        boolean result = ownershipService.isTransactionOwner(transactionId, authentication);

        assertTrue(result);
        verify(transactionsRepository).exists(transactionId);
        verify(transactionsRepository).get(transactionId);
        verify(bankAccountRepository).getUserId(receiverAccountId);
    }

    @Test
    void isTransactionOwner_ReceiverIsNull_UserIsSender_ReturnsTrue() {
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderAccountId);
        transaction.setReceiverId(null);

        when(transactionsRepository.exists(transactionId)).thenReturn(true);
        when(transactionsRepository.get(transactionId)).thenReturn(transaction);
        when(authentication.getName()).thenReturn(userId.toString());
        when(bankAccountRepository.getUserId(senderAccountId)).thenReturn(userId);

        boolean result = ownershipService.isTransactionOwner(transactionId, authentication);

        assertTrue(result);
        verify(transactionsRepository).exists(transactionId);
        verify(transactionsRepository).get(transactionId);
        verify(bankAccountRepository).getUserId(senderAccountId);
    }
}
