package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.CreateLoanRequest;
import com.example.xbankbackend.dtos.requests.LoanRepaymentRequest;
import com.example.xbankbackend.dtos.responses.LoanPaymentAmountResponse;
import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.LoanStatus;
import com.example.xbankbackend.enums.TransactionStatus;
import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.*;
import com.example.xbankbackend.mappers.LoanMapper;
import com.example.xbankbackend.models.Loan;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.LoanRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class LoanService {
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");

    private final LoanRepository loanRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionsRepository transactionsRepository;
    private final UserRepository userRepository;
    @Value("${application.serviceAccountId}")
    private UUID serviceAccountId;
    @Value("${application.loanAnnualRate}")
    private BigDecimal annualRate;
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
                .annualInterestRate(annualRate)
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

    public LoanResponse repayMonthly(UUID creditAccountId, LoanRepaymentRequest request, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedForCreditAccount(creditAccountId, authenticatedUserId);
        UUID loanId = loan.getLoanId();

        validateLoanCreditAccountForRepayment(loan, authenticatedUserId);

        BigDecimal expectedPayment = scaleMoney(loan.getMonthlyPayment());
        BigDecimal providedAmount = scaleMoney(request.getAmount());
        if (providedAmount.compareTo(expectedPayment) != 0) {
            throw new LoanRepaymentAmountMismatchException("Arbitrary repayment amounts are not allowed. Monthly repayment must equal annuity amount");
        }

        transferRepaymentFromCreditAccount(creditAccountId, loan, providedAmount);

        BigDecimal currentOutstanding = scaleMoney(loan.getOutstandingPrincipal());
        BigDecimal monthlyInterest = scaleMoney(currentOutstanding.multiply(getMonthlyRate()));
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

    public LoanResponse repayEarly(UUID creditAccountId, LoanRepaymentRequest request, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedForCreditAccount(creditAccountId, authenticatedUserId);
        UUID loanId = loan.getLoanId();
        BigDecimal months = new BigDecimal(loan.getTermMonths());
        validateLoanCreditAccountForRepayment(loan, authenticatedUserId);

        BigDecimal requiredAmount = scaleMoney(loan.getOutstandingPrincipal().multiply(months));
        BigDecimal providedAmount = scaleMoney(request.getAmount());
        if (providedAmount.compareTo(requiredAmount) != 0) {
            throw new LoanRepaymentAmountMismatchException("Arbitrary repayment amounts are not allowed. Early repayment must close the remaining principal in full");
        }

        transferRepaymentFromCreditAccount(creditAccountId, loan, providedAmount);
        loanRepository.close(loanId, OffsetDateTime.now());

        loan.setOutstandingPrincipal(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        loan.setStatus(LoanStatus.CLOSED);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse getByCreditAccount(UUID loanId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedForCreditAccount(loanId, authenticatedUserId);
        return mapper.loanToResponse(loan);
    }
 
    public LoanPaymentAmountResponse fullPaymentCost(UUID creditAccountId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedForCreditAccount(creditAccountId, authenticatedUserId);
        BigDecimal providedAmount = scaleMoney(loan.getOutstandingPrincipal());
        BigDecimal termMonths = new BigDecimal(loan.getTermMonths());
        LoanPaymentAmountResponse response = LoanPaymentAmountResponse.builder()
                .amount(providedAmount.multiply(termMonths))
                .build();
        return response;

    }
    public LoanPaymentAmountResponse monthlyPaymentCost(UUID creditAccountId, UUID authenticatedUserId){
        Loan loan = getActiveLoanOwnedForCreditAccount(creditAccountId, authenticatedUserId);
        BigDecimal providedAmount = scaleMoney(loan.getMonthlyPayment());
        LoanPaymentAmountResponse response = LoanPaymentAmountResponse.builder()
                .amount(providedAmount)
        .build();
        return response;
    }

    BigDecimal calculateAnnuityPayment(BigDecimal principalAmount, int termMonths) {
        if (principalAmount == null || principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be positive");
        }
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Term months must be positive");
        }

        double monthlyRate = getMonthlyRate().doubleValue();
        double factor = Math.pow(1 + monthlyRate, termMonths);
        BigDecimal numerator = principalAmount.multiply(BigDecimal.valueOf(monthlyRate)).multiply(BigDecimal.valueOf(factor));
        BigDecimal denominator = BigDecimal.valueOf(factor - 1);
        return scaleMoney(numerator.divide(denominator, 8, RoundingMode.HALF_UP));
    }

    public List<LoanResponse> getLoansByUser(UUID userId) {
        if (!userRepository.exists(userId)) {
            throw new UserNotFoundException("User with UUID " + userId + " does not exist");
        }
        List<Loan> loans = loanRepository.getLoans(userId);
        return mapper.loansToResponses(loans);
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

    private Loan getActiveLoanOwnedForCreditAccount(UUID creditAccountId, UUID authenticatedUserId) {
        validateAccountExists(creditAccountId);
        return loanRepository.findActiveByCreditAccountIdAndUserId(creditAccountId, authenticatedUserId)
                .orElseThrow(() -> new LoanNotFoundException(
                        "No active loan for credit account " + creditAccountId));
    }

    private void saveCompletedTransaction(Transaction tx) {
        tx.setTransactionId(UUID.randomUUID());
        tx.setTransactionDate(OffsetDateTime.now());
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionsRepository.addTransaction(tx);
    }

    private void transferFromCreditAccountToService(UUID creditAccountId, UUID serviceId, BigDecimal amount, CurrencyType currency) {
        if (bankAccountRepository.getBalance(creditAccountId).compareTo(amount) < 0) {
            throw new InsufficientFundsException("Credit account balance must be greater than or equal to repayment amount");
        }
        if (!bankAccountRepository.isActive(serviceId)) {
            throw new AccessDeniedException("Service account is deactivated");
        }
        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .senderId(creditAccountId)
                .receiverId(serviceId)
                .amount(amount)
                .currency(currency)
                .comment("Погашение кредита")
                .build();
        saveCompletedTransaction(tx);
        bankAccountRepository.decreaseBalance(creditAccountId, amount);
        bankAccountRepository.increaseBalance(serviceId, amount);
    }

    private void validateAccountExists(UUID accountId) {
        if (!bankAccountRepository.exists(accountId)) {
            throw new BankAccountNotFoundException("Bank account with UUID " + accountId + " doesn't exist");
        }
    }

    private void validateLoanCreditAccountForRepayment(Loan loan, UUID authenticatedUserId) {
        UUID creditAccountId = loan.getCreditAccountId();
        validateAccountExists(creditAccountId);
        if (!bankAccountRepository.getUserId(creditAccountId).equals(authenticatedUserId)) {
            throw new AccessDeniedException("Authenticated user is not the credit account owner");
        }
        if (bankAccountRepository.getAccountType(creditAccountId) != BankAccountType.CREDIT) {
            throw new IllegalArgumentException("Loan-linked account must have CREDIT type");
        }
        if (!bankAccountRepository.getCurrency(creditAccountId).equals(loan.getCurrency())) {
            throw new DifferentCurrencyException("Credit account currency must match loan currency");
        }
        if (!bankAccountRepository.isActive(creditAccountId)) {
            throw new AccessDeniedException("Credit account is deactivated");
        }
    }

    private void transferRepaymentFromCreditAccount(UUID creditAccountId, Loan loan, BigDecimal amount) {
        if (!loan.getCreditAccountId().equals(creditAccountId)) {
            throw new AccessDeniedException("Repayment must be transferred from the loan credit account");
        }
        transferFromCreditAccountToService(creditAccountId, loan.getServiceAccountId(), amount, loan.getCurrency());
    }

    private BigDecimal scaleMoney(BigDecimal amount) {
        return amount.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal getMonthlyRate() {
        return annualRate.divide(MONTHS_IN_YEAR, 16, RoundingMode.HALF_UP);
    }

}
