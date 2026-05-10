package com.example.xbankbackend.services.loan;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.DifferentCurrencyException;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
import com.example.xbankbackend.exceptions.LoanRepaymentAmountMismatchException;
import com.example.xbankbackend.models.Loan;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.services.bankAccount.BankAccountValidationService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Service
public class LoanValidationService {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountValidationService bankAccountValidationService;

    public void validateBankAccountExists(UUID accountId) {
        bankAccountValidationService.validateBankAccountExists(accountId);
    }

    public CurrencyType validateCashLoanDisbursementTarget(UUID debitAccountId, UUID authenticatedUserId) {
        if (debitAccountId == null) {
            throw new IllegalArgumentException("Для кредита наличными обязателен debitAccountId");
        }
        validateBankAccountExists(debitAccountId);
        if (!bankAccountRepository.getUserId(debitAccountId).equals(authenticatedUserId)) {
            throw new AccessDeniedException("Пользователь не является владельцем дебетового счёта для зачисления");
        }
        if (bankAccountRepository.getAccountType(debitAccountId) != BankAccountType.DEBIT) {
            throw new IllegalArgumentException("Кредит наличными можно зачислить только на счёт типа DEBIT");
        }
        if (!bankAccountRepository.isActive(debitAccountId)) {
            throw new AccessDeniedException("Дебетовый счёт для зачисления деактивирован");
        }
        return bankAccountRepository.getCurrency(debitAccountId);
    }

    public void validateCashLoanContextForRepayment(Loan loan, UUID authenticatedUserId) {
        UUID debitAccountId = loan.getDebitAccountId();
        if (debitAccountId == null) {
            throw new IllegalStateException("Для cash-loan должен быть указан счёт списания");
        }
        validateBankAccountExists(debitAccountId);
        if (!bankAccountRepository.getUserId(debitAccountId).equals(authenticatedUserId)) {
            throw new AccessDeniedException("Пользователь не является владельцем счёта списания при погашении");
        }
        if (bankAccountRepository.getAccountType(debitAccountId) != BankAccountType.DEBIT) {
            throw new IllegalArgumentException("Счёт списания при погашении должен быть типа DEBIT");
        }
        if (!bankAccountRepository.getCurrency(debitAccountId).equals(loan.getCurrency())) {
            throw new DifferentCurrencyException("Валюта счёта списания должна совпадать с валютой кредита");
        }
        if (!bankAccountRepository.isActive(debitAccountId)) {
            throw new AccessDeniedException("Счёт списания при погашении деактивирован");
        }
    }

    public void validateProvidedAmountIsEqualsToExpectedPayment(BigDecimal providedAmount, BigDecimal expectedPayment) {
        if (providedAmount.compareTo(expectedPayment) != 0) {
            throw new LoanRepaymentAmountMismatchException("Произвольные суммы погашения не допускаются. Ежемесячные выплаты должны быть равны сумме аннуитета");
        }
    }

    public void validateTermMonthsIsPositive(int termMonths) {
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Количество месяцев должно быть положительно");
        }
    }

    public void validatePrincipalAmountIsPositive(BigDecimal principalAmount) {
        if (principalAmount == null || principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Основная сумма должна быть положительной");
        }
    }

    public void validateIsEnoughMoneyInTheBankAccount(BigDecimal balance, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Остаток на счёте списания должен быть не меньше суммы погашения");
        }
    }

    public void validateServiceAccountIsActive(UUID serviceAccountId) {
        if (!bankAccountRepository.isActive(serviceAccountId)) {
            throw new AccessDeniedException("Сервисный счёт неактивен");
        }
    }
    public void validateAccountIsActive(UUID accountId) {
        if (!bankAccountRepository.isActive(accountId)) {
            throw new AccessDeniedException("Счёт " + accountId + " неактивен");
        }
    }

    public void validateRepaymentSenderAccountPresent(UUID debitAccountId) {
        if (debitAccountId == null) {
            throw new IllegalStateException("Для автоплатежа по кредиту должен быть указан дебетовый счёт списания");
        }
    }

    public void validateAutopayHasEnoughFunds(UUID senderAccountId, BigDecimal paymentAmount) {
        BigDecimal balance = bankAccountRepository.getBalance(senderAccountId);
        validateIsEnoughMoneyInTheBankAccount(balance, paymentAmount);
    }
}
