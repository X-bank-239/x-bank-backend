package com.example.xbankbackend.repositories;

import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.Loan;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.*;
import static com.example.xbankbackend.generated.Tables.BANK_ACCOUNTS;
import static com.example.xbankbackend.generated.Tables.USERS;

@AllArgsConstructor
@Repository
public class LoanRepository {
    private final DSLContext dsl;

    public void create(Loan loan) {
        dsl.insertInto(LOANS)
                .values(
                        loan.getLoanId(),
                        loan.getUserId(),
                        loan.getCreditAccountId(),
                        loan.getServiceAccountId(),
                        loan.getCurrency(),
                        loan.getPrincipalAmount(),
                        loan.getAnnualInterestRate(),
                        loan.getTermMonths(),
                        loan.getMonthlyPayment(),
                        loan.getOutstandingPrincipal(),
                        loan.getNextPaymentDate(),
                        loan.getStatus(),
                        loan.getCreatedAt(),
                        loan.getClosedAt()
                )
                .execute();
    }

    public Loan get(UUID loanId) {
        return dsl.selectFrom(LOANS)
                .where(LOANS.LOAN_ID.eq(loanId))
                .fetchOneInto(Loan.class);
    }
    public List<Loan> getLoans(UUID userId) {
        return dsl.select()
                .from(LOANS)
                .join(USERS).on(LOANS.USER_ID.eq(USERS.USER_ID))
                .where(USERS.USER_ID.eq(userId))
                .fetch()
                .into(Loan.class);
    }
    public Optional<Loan> findActiveByCreditAccountIdAndUserId(UUID creditAccountId, UUID userId) {
        return dsl.selectFrom(LOANS)
                .where(LOANS.CREDIT_ACCOUNT_ID.eq(creditAccountId))
                .and(LOANS.USER_ID.eq(userId))
                .and(LOANS.STATUS.eq(com.example.xbankbackend.generated.enums.LoanStatus.ACTIVE))
                .orderBy(LOANS.CREATED_AT.desc())
                .limit(1)
                .fetchOptionalInto(Loan.class);
    }

    public boolean exists(UUID loanId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(LOANS)
                        .where(LOANS.LOAN_ID.eq(loanId))
        );
    }

    public void updateRepaymentState(UUID loanId, BigDecimal outstandingPrincipal, LocalDate nextPaymentDate) {
        dsl.update(LOANS)
                .set(LOANS.OUTSTANDING_PRINCIPAL, outstandingPrincipal)
                .set(LOANS.NEXT_PAYMENT_DATE, nextPaymentDate)
                .where(LOANS.LOAN_ID.eq(loanId))
                .execute();
    }

    public void close(UUID loanId, OffsetDateTime closedAt) {
        dsl.update(LOANS)
                .set(LOANS.STATUS, com.example.xbankbackend.generated.enums.LoanStatus.CLOSED)
                .set(LOANS.OUTSTANDING_PRINCIPAL, BigDecimal.ZERO)
                .set(LOANS.CLOSED_AT, closedAt)
                .where(LOANS.LOAN_ID.eq(loanId))
                .execute();
    }
}
