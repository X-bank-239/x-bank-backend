package com.example.xbankbackend.repositories;

import com.example.xbankbackend.models.Loan;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.LOANS;

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
