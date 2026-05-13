package com.example.xbankbackend.repositories;

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

import com.example.xbankbackend.generated.enums.CurrencyType;
import com.example.xbankbackend.generated.enums.LoanStatus;

import static com.example.xbankbackend.generated.Tables.LOANS;
import static com.example.xbankbackend.generated.Tables.USERS;

@AllArgsConstructor
@Repository
public class LoanRepository {
    private final DSLContext dsl;

    public void create(Loan loan) {
        dsl.insertInto(LOANS)
                .set(LOANS.LOAN_ID, loan.getLoanId())
                .set(LOANS.CREATED_AT, loan.getCreatedAt())
                .set(LOANS.DEBIT_ACCOUNT_ID,loan.getDebitAccountId())
                .set(LOANS.USER_ID, loan.getUserId())
                .set(LOANS.CURRENCY, CurrencyType.valueOf(loan.getCurrency().name()))
                .set(LOANS.SERVICE_ACCOUNT_ID, loan.getServiceAccountId())
                .set(LOANS.PRINCIPAL_AMOUNT, loan.getPrincipalAmount())
                .set(LOANS.ANNUAL_INTEREST_RATE, loan.getAnnualInterestRate())
                .set(LOANS.TERM_MONTHS, loan.getTermMonths())
                .set(LOANS.MONTHLY_PAYMENT, loan.getMonthlyPayment())
                .set(LOANS.OUTSTANDING_PRINCIPAL, loan.getOutstandingPrincipal())
                .set(LOANS.NEXT_PAYMENT_DATE, loan.getNextPaymentDate())
                .set(LOANS.STATUS, LoanStatus.valueOf(loan.getStatus().name()))
                .set(LOANS.CLOSED_AT,  loan.getClosedAt())
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

    public Optional<Loan> findActiveByLoanIdAndUserId(UUID loanId, UUID userId) {
        return dsl.selectFrom(LOANS)
                .where(LOANS.LOAN_ID.eq(loanId))
                .and(LOANS.USER_ID.eq(userId))
                .and(LOANS.STATUS.eq(LoanStatus.ACTIVE))
                .fetchOptionalInto(Loan.class);
    }

    public Optional<Loan> findActiveByDebitAccountIdAndUserId(UUID accountId, UUID userId) {
        return dsl.selectFrom(LOANS)
                .where(LOANS.DEBIT_ACCOUNT_ID.eq(accountId))
                .and(LOANS.USER_ID.eq(userId))
                .and(LOANS.STATUS.eq(LoanStatus.ACTIVE))
                .orderBy(LOANS.CREATED_AT.desc())
                .limit(1)
                .fetchOptionalInto(Loan.class);
    }

    public List<Loan> findDueActiveLoans(LocalDate date) {
        return dsl.selectFrom(LOANS)
                .where(LOANS.STATUS.eq(LoanStatus.ACTIVE))
                .and(LOANS.AUTOPAY_ENABLED.eq(true))
                .and(LOANS.NEXT_PAYMENT_DATE.le(date))
                .fetchInto(Loan.class);
    }

    public void setAutopayEnabled(UUID loanId, boolean enabled) {
        dsl.update(LOANS)
                .set(LOANS.AUTOPAY_ENABLED, enabled)
                .where(LOANS.LOAN_ID.eq(loanId))
                .execute();
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
                .set(LOANS.STATUS, LoanStatus.CLOSED)
                .set(LOANS.OUTSTANDING_PRINCIPAL, BigDecimal.ZERO)
                .set(LOANS.CLOSED_AT, closedAt)
                .where(LOANS.LOAN_ID.eq(loanId))
                .execute();
    }
}
