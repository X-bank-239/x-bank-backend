package com.example.xbankbackend.services.loan;

import com.example.xbankbackend.enums.LoanStatus;
import com.example.xbankbackend.enums.TransactionStatus;
import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.models.Loan;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.LoanRepository;
import com.example.xbankbackend.repositories.TransactionsRepository;
import com.example.xbankbackend.repositories.UserRepository;
import com.example.xbankbackend.services.external.notification.EmailSender;
import com.example.xbankbackend.services.loan.LoanRepaymentCalculationService.LoanRepaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class LoanAutopayService {

    private final LoanRepository loanRepository;
    private final LoanValidationService loanValidationService;
    private final LoanRepaymentCalculationService repaymentCalculationService;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionsRepository transactionsRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Scheduled(cron = "${application.loanAutopayCron:0 0 3 * * *}")
    public void runScheduledAutopay() {
        processDueLoans(LocalDate.now());
    }

    @Transactional
    public void processDueLoans(LocalDate paymentDate) {
        List<Loan> dueLoans = loanRepository.findDueActiveLoans(paymentDate);
        for (Loan loan : dueLoans) {
            try {
                processSingleLoan(loan);
            } catch (Exception e) {
                log.warn("Autopay failed for loan {}: {}", loan.getLoanId(), e.getMessage());
            }
        }
        log.info("Autopay cycle finished for date {}, processed {}", paymentDate, dueLoans.size());
    }

    private void processSingleLoan(Loan loan) {
        UUID senderId = resolveRepaymentSender(loan);
        BigDecimal paymentAmount = repaymentCalculationService.scaleMoney(loan.getMonthlyPayment());

        loanValidationService.validateAutopayHasEnoughFunds(senderId, paymentAmount);
        loanValidationService.validateAccountIsActive(senderId);
        loanValidationService.validateServiceAccountIsActive(loan.getServiceAccountId());

        saveCompletedTransaction(Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .senderId(senderId)
                .receiverId(loan.getServiceAccountId())
                .amount(paymentAmount)
                .currency(loan.getCurrency())
                .comment("Автосписание по кредиту")
                .build());
        bankAccountRepository.decreaseBalance(senderId, paymentAmount);
        bankAccountRepository.increaseBalance(loan.getServiceAccountId(), paymentAmount);

        LoanRepaymentResult repaymentResult = repaymentCalculationService.calculateMonthlyRepaymentResult(
                loan.getOutstandingPrincipal(),
                loan.getAnnualInterestRate(),
                paymentAmount,
                loan.getNextPaymentDate()
        );
        if (repaymentResult.shouldCloseLoan()) {
            loanRepository.close(loan.getLoanId(), OffsetDateTime.now());
            loan.setOutstandingPrincipal(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            loan.setStatus(LoanStatus.CLOSED);
            sendRepaymentEmail(loan, paymentAmount, true);
            return;
        }
        BigDecimal newOutstanding = repaymentResult.newOutstanding();
        LocalDate nextPaymentDate = repaymentResult.nextPaymentDate();
        loanRepository.updateRepaymentState(loan.getLoanId(), newOutstanding, nextPaymentDate);
        loan.setOutstandingPrincipal(newOutstanding);
        loan.setNextPaymentDate(nextPaymentDate);
        sendRepaymentEmail(loan, paymentAmount, false);
    }

    private void saveCompletedTransaction(Transaction tx) {
        tx.setTransactionId(UUID.randomUUID());
        tx.setTransactionDate(OffsetDateTime.now());
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionsRepository.addTransaction(tx);
    }

    private UUID resolveRepaymentSender(Loan loan) {
        UUID debitId = loan.getDebitAccountId();
        loanValidationService.validateRepaymentSenderAccountPresent(debitId);
        return debitId;
    }

    private void sendRepaymentEmail(Loan loan, BigDecimal paymentAmount, boolean closed) {
        try {
            String email = userRepository.getUser(loan.getUserId()).getEmail();
            emailSender.sendLoanRepaymentReceipt(
                    email,
                    loan.getLoanId(),
                    repaymentCalculationService.scaleMoney(paymentAmount),
                    repaymentCalculationService.scaleMoney(loan.getOutstandingPrincipal()),
                    closed ? null : loan.getNextPaymentDate(),
                    closed
            );
        } catch (Exception e) {
            log.warn("Failed to send autopay email for loan {}: {}", loan.getLoanId(), e.getMessage());
        }
    }

}
