package com.example.xbankbackend.services.transaction;

import com.example.xbankbackend.dtos.responses.RecentTransactionsResponse;
import com.example.xbankbackend.dtos.responses.TransactionResponse;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.TransactionStatus;
import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.TransactionNotFoundException;
import com.example.xbankbackend.mappers.TransactionMapper;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.services.bankAccount.BankAccountValidationService;
import com.example.xbankbackend.services.transactionCategories.TransactionCategoriesService;
import com.example.xbankbackend.services.transactionCategories.TransactionCategoriesValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionsService")
class TransactionsServiceTest {

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionCategoriesService categoriesService;

    @Mock
    private TransactionValidationService transactionValidationService;

    @Mock
    private BankAccountValidationService bankAccountValidationService;

    @Mock
    private TransactionCategoriesValidationService categoriesValidationService;

    @Mock
    private BalanceOperationService balanceOperationService;

    @InjectMocks
    private TransactionsService service;

    @Nested
    @DisplayName("deposit")
    class DepositTests {

        @Test
        void shouldExecuteDeposit_WhenValidationsPass() {
            Transaction tx = buildValidDeposit();
            UUID userId = UUID.randomUUID();
            UUID receiverId = tx.getReceiverId();

            doNothing().when(bankAccountValidationService).validateBankAccountExists(receiverId);
            doNothing().when(bankAccountValidationService).validateBankAccountActive(receiverId);
            doNothing().when(transactionValidationService).validateDepositStructure(tx);
            doNothing().when(transactionValidationService).validateAmountPositive(tx.getAmount());

            service.deposit(tx, userId);

            verify(transactionsRepository).addTransaction(tx);
            verify(balanceOperationService).increaseBalance(receiverId, tx.getAmount(), tx.getCurrency());
            assertThat(tx.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(tx.getTransactionDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("transfer")
    class TransferTests {

        @Test
        void shouldExecuteTransfer_WhenValidationsPass() {
            Transaction tx = buildValidTransfer();
            UUID userId = UUID.randomUUID();
            UUID senderId = tx.getSenderId();
            UUID receiverId = tx.getReceiverId();

            doNothing().when(bankAccountValidationService).validateBankAccountExists(senderId);
            doNothing().when(bankAccountValidationService).validateBankAccountExists(receiverId);
            doNothing().when(bankAccountValidationService).validateBankAccountActive(senderId);
            doNothing().when(bankAccountValidationService).validateBankAccountActive(receiverId);
            doNothing().when(transactionValidationService).validateTransferStructure(tx);
            doNothing().when(transactionValidationService).validateUserIsOwner(senderId, userId);
            doNothing().when(transactionValidationService).validateAmountPositive(tx.getAmount());
            doNothing().when(bankAccountValidationService).validateSufficientFundsWithFee(senderId, tx.getAmount());

            service.transfer(tx, userId);

            verify(transactionsRepository).addTransaction(tx);
            verify(balanceOperationService).decreaseBalanceWithFee(senderId, tx.getAmount(), tx.getCurrency());
            verify(balanceOperationService).increaseBalance(receiverId, tx.getAmount(), tx.getCurrency());
        }
    }

    @Nested
    @DisplayName("pay")
    class PayTests {

        @Test
        void shouldExecutePay_WhenValidationsPass() {
            Transaction tx = buildValidPayment();
            UUID userId = UUID.randomUUID();
            UUID senderId = tx.getSenderId();

            doNothing().when(bankAccountValidationService).validateBankAccountExists(senderId);
            doNothing().when(bankAccountValidationService).validateBankAccountActive(senderId);
            doNothing().when(transactionValidationService).validatePaymentStructure(tx);
            doNothing().when(transactionValidationService).validateUserIsOwner(senderId, userId);
            doNothing().when(transactionValidationService).validateAmountPositive(tx.getAmount());
            doNothing().when(bankAccountValidationService).validateSufficientFundsWithFee(senderId, tx.getAmount());

            service.pay(tx, userId);

            verify(transactionsRepository).addTransaction(tx);
            verify(balanceOperationService).decreaseBalanceWithFee(senderId, tx.getAmount(), tx.getCurrency());
        }
    }

    @Nested
    @DisplayName("getTransaction")
    class GetTransactionTests {

        @Test
        void shouldThrowTransactionNotFoundException_WhenTransactionNotExists() {
            UUID transactionId = UUID.randomUUID();

            doThrow(new TransactionNotFoundException("Not found"))
                    .when(transactionValidationService).validateTransactionExists(transactionId);

            assertThatThrownBy(() -> service.getTransaction(transactionId))
                    .isInstanceOf(TransactionNotFoundException.class);
        }

        @Test
        void shouldReturnTransactionResponse_WhenTransactionExists() {
            UUID transactionId = UUID.randomUUID();
            Transaction entity = new Transaction();
            TransactionResponse expected = new TransactionResponse();

            doNothing().when(transactionValidationService).validateTransactionExists(transactionId);
            when(transactionsRepository.get(transactionId)).thenReturn(entity);
            when(transactionMapper.transactionToResponse(entity)).thenReturn(expected);

            TransactionResponse result = service.getTransaction(transactionId);

            assertThat(result).isEqualTo(expected);
            verify(transactionsRepository).get(transactionId);
        }
    }

    @Nested
    @DisplayName("cancelTransaction")
    class CancelTransactionTests {

        @Test
        void shouldRefundSenderAndReverseReceiver_WhenCancellingTransfer() {
            UUID transactionId = UUID.randomUUID();
            Transaction tx = buildValidTransfer();
            tx.setTransactionId(transactionId);

            doNothing().when(transactionValidationService).validateTransactionExists(transactionId);
            when(transactionsRepository.get(transactionId)).thenReturn(tx);

            service.cancelTransaction(transactionId);

            verify(balanceOperationService).increaseBalance(tx.getSenderId(), tx.getAmount(), tx.getCurrency());
            verify(balanceOperationService).decreaseBalance(tx.getReceiverId(), tx.getAmount(), tx.getCurrency());
            verify(transactionsRepository).cancel(transactionId);
        }

        @Test
        void shouldRefundSenderOnly_WhenCancellingPayment() {
            UUID transactionId = UUID.randomUUID();
            Transaction tx = buildValidPayment();
            tx.setTransactionId(transactionId);

            doNothing().when(transactionValidationService).validateTransactionExists(transactionId);
            when(transactionsRepository.get(transactionId)).thenReturn(tx);

            service.cancelTransaction(transactionId);

            verify(balanceOperationService).increaseBalance(tx.getSenderId(), tx.getAmount(), tx.getCurrency());
            verify(balanceOperationService, never()).decreaseBalance(any(), any(), any());
            verify(transactionsRepository).cancel(transactionId);
        }
    }

    @Nested
    @DisplayName("getTransactionsByCategory")
    class GetTransactionsByCategoryTests {

        @Test
        void shouldReturnTransactions_WhenValid() {
            UUID accountId = UUID.randomUUID();
            String categoryCode = "FOOD";
            List<Transaction> expected = List.of(new Transaction(), new Transaction());

            doNothing().when(bankAccountValidationService).validateBankAccountExists(accountId);
            doNothing().when(categoriesValidationService).validateCategoryExists(categoryCode);
            when(transactionsRepository.getTransactionsByCategory(accountId, categoryCode)).thenReturn(expected);

            List<Transaction> result = service.getTransactionsByCategory(accountId, categoryCode);

            assertThat(result).hasSize(2).containsExactlyElementsOf(expected);
        }
    }

    @Nested
    @DisplayName("getRecent")
    class GetRecentTests {

        @Test
        void shouldThrowIllegalArgumentException_WhenPageIsNegative() {
            UUID accountId = UUID.randomUUID();

            doNothing().when(bankAccountValidationService).validateBankAccountExists(accountId);

            assertThatThrownBy(() -> service.getRecent(accountId, -1, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenSizeIsNegative() {
            UUID accountId = UUID.randomUUID();

            doNothing().when(bankAccountValidationService).validateBankAccountExists(accountId);

            assertThatThrownBy(() -> service.getRecent(accountId, 0, -5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldReturnRecentTransactionsResponse_WhenValid() {
            UUID accountId = UUID.randomUUID();
            List<Transaction> transactions = List.of(buildValidDeposit(), buildValidPayment());
            int total = 42;

            doNothing().when(bankAccountValidationService).validateBankAccountExists(accountId);
            when(transactionsRepository.getTransactions(accountId, 0, 10)).thenReturn(transactions);
            when(transactionsRepository.getTransactionsCount(accountId)).thenReturn(total);
            when(transactionMapper.transactionToResponse(any(Transaction.class)))
                    .thenReturn(new TransactionResponse());

            RecentTransactionsResponse result = service.getRecent(accountId, 0, 10);

            assertThat(result.getTotal()).isEqualTo(total);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTransactions()).hasSize(2);
        }
    }

    private Transaction buildValidDeposit() {
        Transaction tx = new Transaction();
        tx.setReceiverId(UUID.randomUUID());
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setCurrency(CurrencyType.RUB);
        tx.setTransactionType(TransactionType.DEPOSIT);
        tx.setComment("Test deposit");
        return tx;
    }

    private Transaction buildValidTransfer() {
        Transaction tx = new Transaction();
        tx.setSenderId(UUID.randomUUID());
        tx.setReceiverId(UUID.randomUUID());
        tx.setAmount(BigDecimal.valueOf(200));
        tx.setCurrency(CurrencyType.USD);
        tx.setTransactionType(TransactionType.TRANSFER);
        tx.setComment("Test transfer");
        return tx;
    }

    private Transaction buildValidPayment() {
        Transaction tx = new Transaction();
        tx.setSenderId(UUID.randomUUID());
        tx.setAmount(BigDecimal.valueOf(50));
        tx.setCurrency(CurrencyType.EUR);
        tx.setTransactionType(TransactionType.PAYMENT);
        tx.setComment("Test payment");
        return tx;
    }
}
