package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.CreateLoanRequest;
import com.example.xbankbackend.dtos.requests.LoanRepaymentRequest;
import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.LoanStatus;
import com.example.xbankbackend.enums.TransactionStatus;
import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.exceptions.DifferentCurrencyException;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
import com.example.xbankbackend.exceptions.LoanClosedException;
import com.example.xbankbackend.exceptions.LoanNotFoundException;
import com.example.xbankbackend.exceptions.LoanRepaymentAmountMismatchException;
import com.example.xbankbackend.mappers.LoanMapper;
import com.example.xbankbackend.models.Loan;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.LoanRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class LoanService {
    public static final BigDecimal ANNUAL_RATE = new BigDecimal("0.15");
    private static final BigDecimal MONTHLY_RATE = ANNUAL_RATE.divide(new BigDecimal("12"), 16, RoundingMode.HALF_UP);

    private final LoanRepository loanRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionsRepository transactionsRepository;
    @Value("${application.serviceAccountId}")
    private UUID serviceAccountId;
    private final LoanMapper mapper;

    public LoanResponse createLoan(CreateLoanRequest request, UUID authenticatedUserId) {
        UUID creditAccountId = request.getCreditAccountId();

        validateAccountExists(creditAccountId);
        validateAccountExists(serviceAccountId);

        if (!bankAccountRepository.getUserId(creditAccountId).equals(authenticatedUserId)) {
            throw new AccessDeniedException("Authenticated user is not the credit account owner");
        }

        if (bankAccountRepository.getAccountType(creditAccountId) != BankAccountType.CREDIT) {
            throw new IllegalArgumentException("Credit account must have CREDIT type");
        }

        CurrencyType loanCurrency = bankAccountRepository.getCurrency(creditAccountId);

        BigDecimal monthlyPayment = calculateAnnuityPayment(request.getPrincipalAmount(), request.getTermMonths());

        BigDecimal principalAmount = scaleMoney(request.getPrincipalAmount());
        Loan loan = Loan.builder()
                .loanId(UUID.randomUUID())
                .userId(authenticatedUserId)
                .creditAccountId(creditAccountId)
                .serviceAccountId(serviceAccountId)
                .currency(loanCurrency)
                .principalAmount(principalAmount)
                .annualInterestRate(ANNUAL_RATE)
                .termMonths(request.getTermMonths())
                .monthlyPayment(monthlyPayment)
                .outstandingPrincipal(principalAmount)
                .nextPaymentDate(LocalDate.now().plusMonths(1))
                .status(LoanStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();
        loanRepository.create(loan);


        bankAccountRepository.increaseBalance(creditAccountId, principalAmount);

        return mapper.loanToResponse(loan);
    }

    public LoanResponse repayMonthly(UUID loanId, LoanRepaymentRequest request, UUID authenticatedUserId) {
        Loan loan = getActiveOwnedLoan(loanId, authenticatedUserId);

        validatePayerAccount(request.getPayerAccountId(), authenticatedUserId, loan);

        BigDecimal expectedPayment = scaleMoney(loan.getMonthlyPayment());
        BigDecimal providedAmount = scaleMoney(request.getAmount());
        if (providedAmount.compareTo(expectedPayment) != 0) {
            throw new LoanRepaymentAmountMismatchException("Arbitrary repayment amounts are not allowed. Monthly repayment must equal annuity amount");
        }

        transferRepaymentToServiceAccount(request.getPayerAccountId(), loan, providedAmount);

        BigDecimal currentOutstanding = scaleMoney(loan.getOutstandingPrincipal());
        BigDecimal monthlyInterest = scaleMoney(currentOutstanding.multiply(MONTHLY_RATE));
        BigDecimal principalReduction = scaleMoney(providedAmount.subtract(monthlyInterest));
        if (principalReduction.compareTo(BigDecimal.ZERO) < 0) {
            principalReduction = BigDecimal.ZERO;
        }

        BigDecimal newOutstanding = scaleMoney(currentOutstanding.subtract(principalReduction));

        if (newOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
            loanRepository.close(loanId, OffsetDateTime.now());
            loan.setOutstandingPrincipal(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            loan.setStatus(LoanStatus.CLOSED);
            return mapper.loanToResponse(loan);
        }

        LocalDate nextPaymentDate = loan.getNextPaymentDate().plusMonths(1);
        loanRepository.updateRepaymentState(loanId, newOutstanding, nextPaymentDate);

        loan.setOutstandingPrincipal(newOutstanding);
        loan.setNextPaymentDate(nextPaymentDate);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse repayEarly(UUID loanId, LoanRepaymentRequest request, UUID authenticatedUserId) {
        Loan loan = getActiveOwnedLoan(loanId, authenticatedUserId);

        validatePayerAccount(request.getPayerAccountId(), authenticatedUserId, loan);

        BigDecimal requiredAmount = scaleMoney(loan.getOutstandingPrincipal());
        BigDecimal providedAmount = scaleMoney(request.getAmount());
        if (providedAmount.compareTo(requiredAmount) != 0) {
            throw new LoanRepaymentAmountMismatchException("Arbitrary repayment amounts are not allowed. Early repayment must close the remaining principal in full");
        }

        transferRepaymentToServiceAccount(request.getPayerAccountId(), loan, providedAmount);
        loanRepository.close(loanId, OffsetDateTime.now());

        loan.setOutstandingPrincipal(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        loan.setStatus(LoanStatus.CLOSED);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse get(UUID loanId, UUID authenticatedUserId) {
        Loan loan = getOwnedLoan(loanId, authenticatedUserId);
        return mapper.loanToResponse(loan);
    }

    BigDecimal calculateAnnuityPayment(BigDecimal principalAmount, int termMonths) {
        if (principalAmount == null || principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be positive");
        }
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Term months must be positive");
        }

        double monthlyRate = MONTHLY_RATE.doubleValue();
        double factor = Math.pow(1 + monthlyRate, termMonths);
        BigDecimal numerator = principalAmount.multiply(BigDecimal.valueOf(monthlyRate)).multiply(BigDecimal.valueOf(factor));
        BigDecimal denominator = BigDecimal.valueOf(factor - 1);
        return scaleMoney(numerator.divide(denominator, 8, RoundingMode.HALF_UP));
    }

    private Loan getOwnedLoan(UUID loanId, UUID authenticatedUserId) {
        if (!loanRepository.exists(loanId)) {
            throw new LoanNotFoundException("Loan with UUID " + loanId + " does not exist");
        }
        Loan loan = loanRepository.get(loanId);
        if (!loan.getUserId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Authenticated user is not the loan owner");
        }
        return loan;
    }

    private Loan getActiveOwnedLoan(UUID loanId, UUID authenticatedUserId) {
        Loan loan = getOwnedLoan(loanId, authenticatedUserId);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new LoanClosedException("Loan is already closed");
        }
        return loan;
    }

    private void saveCompletedTransaction(Transaction tx) {
        tx.setTransactionId(UUID.randomUUID());
        tx.setTransactionDate(OffsetDateTime.now());
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionsRepository.addTransaction(tx);
    }

    private void repayFromDebitToService(UUID payerId, UUID serviceId, BigDecimal amount, CurrencyType currency) {
        if (bankAccountRepository.getBalance(payerId).compareTo(amount) < 0) {
            throw new InsufficientFundsException("Payer balance must be greater than or equal to repayment amount");
        }
        if (!bankAccountRepository.isActive(serviceId)) {
            throw new AccessDeniedException("Service account is deactivated");
        }
        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .senderId(payerId)
                .receiverId(serviceId)
                .amount(amount)
                .currency(currency)
                .comment("Погашение кредита")
                .build();
        saveCompletedTransaction(tx);
        bankAccountRepository.decreaseBalance(payerId, amount);
        bankAccountRepository.increaseBalance(serviceId, amount);
    }

    private void validateAccountExists(UUID accountId) {
        if (!bankAccountRepository.exists(accountId)) {
            throw new BankAccountNotFoundException("Bank account with UUID " + accountId + " doesn't exist");
        }
    }

    private void validatePayerAccount(UUID payerAccountId, UUID authenticatedUserId, Loan loan) {
        validateAccountExists(payerAccountId);
        if (!bankAccountRepository.getUserId(payerAccountId).equals(authenticatedUserId)) {
            throw new AccessDeniedException("Authenticated user is not the payer account owner");
        }
        if (bankAccountRepository.getAccountType(payerAccountId) != BankAccountType.DEBIT) {
            throw new IllegalArgumentException("Repayment payer account must have DEBIT type");
        }
        if (!bankAccountRepository.getCurrency(payerAccountId).equals(loan.getCurrency())) {
            throw new DifferentCurrencyException("Repayment payer account currency must match loan currency");
        }
        if (!bankAccountRepository.isActive(payerAccountId)) {
            throw new AccessDeniedException("Payer account is deactivated");
        }
    }

    private void transferRepaymentToServiceAccount(UUID payerAccountId, Loan loan, BigDecimal amount) {
        repayFromDebitToService(payerAccountId, loan.getServiceAccountId(), amount, loan.getCurrency());
    }

    private BigDecimal scaleMoney(BigDecimal amount) {
        return amount.setScale(4, RoundingMode.HALF_UP);
    }

}
