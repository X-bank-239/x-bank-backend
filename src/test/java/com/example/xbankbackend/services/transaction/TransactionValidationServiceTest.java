package com.example.xbankbackend.services.transaction;

import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.TransactionNotFoundException;
import com.example.xbankbackend.exceptions.UserIsNotABankAccountOwner;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionValidationService")
class TransactionValidationServiceTest {

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private TransactionValidationService service;

    @Nested
    @DisplayName("validateAmountPositive")
    class ValidateAmountPositiveTests {

        @Test
        void shouldNotThrow_WhenAmountIsPositive() {
            assertThatNoException().isThrownBy(() -> service.validateAmountPositive(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenAmountIsZero() {
            assertThatThrownBy(() -> service.validateAmountPositive(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenAmountIsNull() {
            assertThatThrownBy(() -> service.validateAmountPositive(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenAmountIsNegative() {
            assertThatThrownBy(() -> service.validateAmountPositive(BigDecimal.valueOf(-50)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("validateUserIsOwner")
    class ValidateUserIsOwnerTests {

        @Test
        void shouldNotThrow_WhenUserIsOwner() {
            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(bankAccountRepository.getUserId(accountId)).thenReturn(userId);

            assertThatNoException().isThrownBy(() -> service.validateUserIsOwner(accountId, userId));
        }

        @Test
        void shouldThrowUserIsNotABankAccountOwner_WhenUserIsNotOwner() {
            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            when(bankAccountRepository.getUserId(accountId)).thenReturn(otherUserId);

            assertThatThrownBy(() -> service.validateUserIsOwner(accountId, userId))
                    .isInstanceOf(UserIsNotABankAccountOwner.class);
        }
    }

    @Nested
    @DisplayName("validateTransactionExists")
    class ValidateTransactionExistsTests {

        @Test
        void shouldNotThrow_WhenTransactionExists() {
            UUID transactionId = UUID.randomUUID();

            when(transactionsRepository.exists(transactionId)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateTransactionExists(transactionId));
        }

        @Test
        void shouldThrowTransactionNotFoundException_WhenTransactionNotExists() {
            UUID transactionId = UUID.randomUUID();

            when(transactionsRepository.exists(transactionId)).thenReturn(false);

            assertThatThrownBy(() -> service.validateTransactionExists(transactionId))
                    .isInstanceOf(TransactionNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateDepositStructure")
    class ValidateDepositStructureTests {

        @Test
        void shouldNotThrow_WhenStructureIsValid() {
            Transaction tx = buildTransaction(null, UUID.randomUUID(), TransactionType.DEPOSIT);

            assertThatNoException().isThrownBy(() -> service.validateDepositStructure(tx));
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenReceiverIdIsNull() {
            Transaction tx = buildTransaction(null, null, TransactionType.DEPOSIT);

            assertThatThrownBy(() -> service.validateDepositStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenSenderIdIsNotNull() {
            Transaction tx = buildTransaction(UUID.randomUUID(), UUID.randomUUID(), TransactionType.DEPOSIT);

            assertThatThrownBy(() -> service.validateDepositStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenTransactionTypeIsWrong() {
            Transaction tx = buildTransaction(null, UUID.randomUUID(), TransactionType.TRANSFER);

            assertThatThrownBy(() -> service.validateDepositStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("validateTransferStructure")
    class ValidateTransferStructureTests {

        @Test
        void shouldNotThrow_WhenStructureIsValid() {
            Transaction tx = buildTransaction(UUID.randomUUID(), UUID.randomUUID(), TransactionType.TRANSFER);

            assertThatNoException().isThrownBy(() -> service.validateTransferStructure(tx));
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenSenderIdIsNull() {
            Transaction tx = buildTransaction(null, UUID.randomUUID(), TransactionType.TRANSFER);

            assertThatThrownBy(() -> service.validateTransferStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenReceiverIdIsNull() {
            Transaction tx = buildTransaction(UUID.randomUUID(), null, TransactionType.TRANSFER);

            assertThatThrownBy(() -> service.validateTransferStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenTransactionTypeIsWrong() {
            Transaction tx = buildTransaction(UUID.randomUUID(), UUID.randomUUID(), TransactionType.PAYMENT);

            assertThatThrownBy(() -> service.validateTransferStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("validatePaymentStructure")
    class ValidatePaymentStructureTests {

        @Test
        void shouldNotThrow_WhenStructureIsValid() {
            Transaction tx = buildTransaction(UUID.randomUUID(), null, TransactionType.PAYMENT);

            assertThatNoException().isThrownBy(() -> service.validatePaymentStructure(tx));
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenSenderIdIsNull() {
            Transaction tx = buildTransaction(null, null, TransactionType.PAYMENT);

            assertThatThrownBy(() -> service.validatePaymentStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenReceiverIdIsNotNull() {
            Transaction tx = buildTransaction(UUID.randomUUID(), UUID.randomUUID(), TransactionType.PAYMENT);

            assertThatThrownBy(() -> service.validatePaymentStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenTransactionTypeIsWrong() {
            Transaction tx = buildTransaction(UUID.randomUUID(), null, TransactionType.DEPOSIT);

            assertThatThrownBy(() -> service.validatePaymentStructure(tx))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private Transaction buildTransaction(UUID sender, UUID receiver, TransactionType type) {
        Transaction tx = new Transaction();
        tx.setSenderId(sender);
        tx.setReceiverId(receiver);
        tx.setTransactionType(type);
        return tx;
    }
}
