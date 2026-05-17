package com.example.xbankbackend.services.loan;

import com.example.xbankbackend.dtos.requests.CreateLoanRequest;
import com.example.xbankbackend.dtos.requests.LoanRepaymentRequest;
import com.example.xbankbackend.dtos.responses.LoanPaymentAmountResponse;
import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.dtos.result.LoanRepaymentResult;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.LoanStatus;
import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.exceptions.*;
import com.example.xbankbackend.mappers.LoanMapper;
import com.example.xbankbackend.models.Loan;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.LoanRepository;
import com.example.xbankbackend.repositories.UserRepository;
import com.example.xbankbackend.services.AppSettingsService;
import com.example.xbankbackend.services.external.notification.EmailSender;
import com.example.xbankbackend.services.transaction.TransactionsService;
import com.example.xbankbackend.services.user.UserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionsService transactionsService;
    private final LoanValidationService loanValidationService;
    private final LoanRepaymentCalculationService repaymentCalculationService;
    private final UserValidationService userValidationService;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final AppSettingsService appSettingsService;
    @Value("${application.serviceAccountId}")
    private UUID serviceAccountId;
    private final LoanMapper mapper;

    public LoanResponse createLoan(CreateLoanRequest request, UUID authenticatedUserId) {
        UUID debitAccountId = request.getDebitAccountId();

        loanValidationService.validateBankAccountExists(serviceAccountId);

        CurrencyType loanCurrency = loanValidationService.validateCashLoanDisbursementTarget(debitAccountId, authenticatedUserId);
        BigDecimal annualRate = appSettingsService.getLoanAnnualRate();
        BigDecimal monthlyPayment = calculateAnnuityPayment(request.getPrincipalAmount(), request.getTermMonths(), annualRate);
        BigDecimal principalAmount = repaymentCalculationService.scaleMoney(request.getPrincipalAmount());

        Loan loan = Loan.builder()
                .loanId(UUID.randomUUID())
                .userId(authenticatedUserId)
                .debitAccountId(debitAccountId)
                .serviceAccountId(serviceAccountId)
                .autopayEnabled(false)
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

        bankAccountRepository.increaseBalance(debitAccountId, principalAmount);

        return mapper.loanToResponse(loan);
    }

    public LoanResponse repayMonthly(UUID loanId, LoanRepaymentRequest request, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        UUID loanEntityId = loan.getLoanId();
        loanValidationService.validateCashLoanContextForRepayment(loan, authenticatedUserId);
        UUID senderAccountId = loan.getDebitAccountId();

        BigDecimal expectedPayment = repaymentCalculationService.scaleMoney(loan.getMonthlyPayment());
        BigDecimal providedAmount = repaymentCalculationService.scaleMoney(request.getAmount());

        loanValidationService.validateProvidedAmountIsEqualsToExpectedPayment(providedAmount, expectedPayment);

        executeRepaymentTransfer(senderAccountId, loan.getServiceAccountId(), providedAmount, loan.getCurrency(), authenticatedUserId);

        BigDecimal repaymentRate = loan.getAnnualInterestRate() != null ? loan.getAnnualInterestRate() : appSettingsService.getLoanAnnualRate();
        LoanRepaymentResult repaymentResult = repaymentCalculationService.calculateMonthlyRepaymentResult(
                loan.getOutstandingPrincipal(),
                repaymentRate,
                providedAmount,
                loan.getNextPaymentDate()
        );

        if (repaymentResult.isShouldCloseLoan()) {
            loanRepository.close(loanEntityId, OffsetDateTime.now());
            loan.setOutstandingPrincipal(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            loan.setStatus(LoanStatus.CLOSED);
            sendLoanRepaymentEmail(loan, providedAmount, true);
            return mapper.loanToResponse(loan);
        }

        BigDecimal newOutstanding = repaymentResult.getNewOutstanding();
        LocalDate nextPaymentDate = repaymentResult.getNextPaymentDate();
        loanRepository.updateRepaymentState(loanEntityId, newOutstanding, nextPaymentDate);

        loan.setOutstandingPrincipal(newOutstanding);
        loan.setNextPaymentDate(nextPaymentDate);
        sendLoanRepaymentEmail(loan, providedAmount, false);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse repayMonthlyByAccount(UUID accountId, LoanRepaymentRequest request, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedByAccount(accountId, authenticatedUserId);
        return repayMonthly(loan.getLoanId(), request, authenticatedUserId);
    }

    public LoanResponse repayEarly(UUID loanId, LoanRepaymentRequest request, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        UUID loanEntityId = loan.getLoanId();
        loanValidationService.validateCashLoanContextForRepayment(loan, authenticatedUserId);
        UUID senderAccountId = loan.getDebitAccountId();

        BigDecimal expectedAmount = calculateEarlyRepaymentAmount(loan);
        BigDecimal providedAmount = repaymentCalculationService.scaleMoney(request.getAmount());
        loanValidationService.validateProvidedAmountIsEqualsToExpectedPayment(providedAmount, expectedAmount);

        executeRepaymentTransfer(senderAccountId, loan.getServiceAccountId(), providedAmount, loan.getCurrency(), authenticatedUserId);
        loanRepository.close(loanEntityId, OffsetDateTime.now());

        loan.setOutstandingPrincipal(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        loan.setStatus(LoanStatus.CLOSED);
        sendLoanRepaymentEmail(loan, providedAmount, true);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse repayEarlyByAccount(UUID accountId, LoanRepaymentRequest request, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedByAccount(accountId, authenticatedUserId);
        return repayEarly(loan.getLoanId(), request, authenticatedUserId);
    }

    public LoanResponse getByLoanId(UUID loanId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse getByAccountId(UUID accountId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedByAccount(accountId, authenticatedUserId);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse enableAutopay(UUID loanId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        loanValidationService.validateCashLoanContextForRepayment(loan, authenticatedUserId);

        loanRepository.setAutopayEnabled(loanId, true);
        loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse disableAutopay(UUID loanId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        loanValidationService.validateCashLoanContextForRepayment(loan, authenticatedUserId);
        loanRepository.setAutopayEnabled(loanId, false);
        loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        return mapper.loanToResponse(loan);
    }

    public LoanResponse getAutopayStatus(UUID loanId, UUID authenticatedUserId) {
        return getByLoanId(loanId, authenticatedUserId);
    }

    public LoanResponse enableAutopayByAccount(UUID accountId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedByAccount(accountId, authenticatedUserId);
        return enableAutopay(loan.getLoanId(), authenticatedUserId);
    }

    public LoanResponse disableAutopayByAccount(UUID accountId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedByAccount(accountId, authenticatedUserId);
        return disableAutopay(loan.getLoanId(), authenticatedUserId);
    }

    public LoanResponse getAutopayStatusByAccount(UUID accountId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedByAccount(accountId, authenticatedUserId);
        return getAutopayStatus(loan.getLoanId(), authenticatedUserId);
    }

    public LoanPaymentAmountResponse fullPaymentCost(UUID loanId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        return LoanPaymentAmountResponse.builder()
                .amount(calculateEarlyRepaymentAmount(loan))
                .build();
    }

    public LoanPaymentAmountResponse fullPaymentCostByAccount(UUID accountId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedByAccount(accountId, authenticatedUserId);
        return fullPaymentCost(loan.getLoanId(), authenticatedUserId);
    }

    public LoanPaymentAmountResponse monthlyPaymentCost(UUID loanId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedById(loanId, authenticatedUserId);
        BigDecimal providedAmount = repaymentCalculationService.scaleMoney(loan.getMonthlyPayment());
        return LoanPaymentAmountResponse.builder()
                .amount(providedAmount)
                .build();
    }

    public LoanPaymentAmountResponse monthlyPaymentCostByAccount(UUID accountId, UUID authenticatedUserId) {
        Loan loan = getActiveLoanOwnedByAccount(accountId, authenticatedUserId);
        return monthlyPaymentCost(loan.getLoanId(), authenticatedUserId);
    }

    public BigDecimal calculateAnnuityPayment(BigDecimal principalAmount, int termMonths, BigDecimal annualRate) {
        loanValidationService.validatePrincipalAmountIsPositive(principalAmount);
        loanValidationService.validateTermMonthsIsPositive(termMonths);

        double monthlyRate = repaymentCalculationService.monthlyRate(annualRate).doubleValue();
        double factor = Math.pow(1 + monthlyRate, termMonths);
        BigDecimal numerator = principalAmount.multiply(BigDecimal.valueOf(monthlyRate)).multiply(BigDecimal.valueOf(factor));
        BigDecimal denominator = BigDecimal.valueOf(factor - 1);
        return repaymentCalculationService.scaleMoney(numerator.divide(denominator, 8, RoundingMode.HALF_UP));
    }

    public List<LoanResponse> getLoansByUser(UUID userId) {
        userValidationService.validateUserExists(userId);
        List<Loan> loans = loanRepository.getLoans(userId);
        return mapper.loansToResponses(loans);
    }

    private Loan getActiveLoanOwnedById(UUID loanId, UUID authenticatedUserId) {
        return loanRepository.findActiveByLoanIdAndUserId(loanId, authenticatedUserId)
                .orElseThrow(() -> new LoanNotFoundException(
                        "Нет активного кредита с id " + loanId));
    }

    private Loan getActiveLoanOwnedByAccount(UUID accountId, UUID authenticatedUserId) {
        loanValidationService.validateBankAccountExists(accountId);
        return loanRepository.findActiveByDebitAccountIdAndUserId(accountId, authenticatedUserId)
                .orElseThrow(() -> new LoanNotFoundException(
                        "Нет активного кредита по счёту " + accountId));
    }

    private void executeRepaymentTransfer(UUID senderAccountId, UUID serviceId, BigDecimal amount, CurrencyType currency,
                                          UUID authenticatedUserId) {
        BigDecimal balance = bankAccountRepository.getBalance(senderAccountId);

        loanValidationService.validateIsEnoughMoneyInTheBankAccount(balance, amount);
        loanValidationService.validateServiceAccountIsActive(serviceId);

        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .senderId(senderAccountId)
                .receiverId(serviceId)
                .amount(amount)
                .currency(currency)
                .comment("Погашение кредита")
                .build();
        transactionsService.transfer(tx, authenticatedUserId);
    }

    private void sendLoanRepaymentEmail(Loan loan, BigDecimal amount, boolean closed) {
        try {
            String email = userRepository.getUser(loan.getUserId()).getEmail();
            emailSender.sendLoanRepaymentReceipt(
                    email,
                    loan.getLoanId(),
                    repaymentCalculationService.scaleMoney(amount),
                    repaymentCalculationService.scaleMoney(loan.getOutstandingPrincipal()),
                    closed ? null : loan.getNextPaymentDate(),
                    closed
            );
        } catch (Exception ex) {
            log.warn("Failed to send loan repayment email for loan {}: {}", loan.getLoanId(), ex.getMessage());
        }
    }

    private BigDecimal calculateEarlyRepaymentAmount(Loan loan) {
        long paidMonths = ChronoUnit.MONTHS.between(
                loan.getCreatedAt().toLocalDate().plusMonths(1),
                loan.getNextPaymentDate()
        );

        long monthsLeft = loan.getTermMonths() - paidMonths;
        long remainingMonths = Math.max(1, monthsLeft);

        return repaymentCalculationService.scaleMoney(
                loan.getMonthlyPayment().multiply(BigDecimal.valueOf(remainingMonths))
        );
    }

}
