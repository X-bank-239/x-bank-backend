package com.example.xbankbackend.services.loan;

import com.example.xbankbackend.dtos.result.LoanRepaymentResult;
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
import com.example.xbankbackend.services.transaction.TransactionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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
    private final LoanAutopayProcessorService loanAutopayProcessorService;

    @Scheduled(cron = "${application.loanAutopayCron:0 0 3 * * *}")
    public void runScheduledAutopay() {
        processDueLoans(LocalDate.now());
    }

    public void processDueLoans(LocalDate paymentDate) {
        List<Loan> dueLoans = loanRepository.findDueActiveLoans(paymentDate);
        for (Loan loan : dueLoans) {
            try {
                loanAutopayProcessorService.processSingleLoan(loan);
            } catch (Exception e) {
                log.warn("Autopay failed for loan {}: {}", loan.getLoanId(), e.getMessage());
            }
        }
        log.info("Autopay cycle finished for date {}, processed {}", paymentDate, dueLoans.size());
    }
}
