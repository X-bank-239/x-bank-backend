package com.example.xbankbackend.repositories;

import com.example.xbankbackend.models.SavingsAccount;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.SAVINGS_ACCOUNTS;
import static com.example.xbankbackend.generated.Tables.BANK_ACCOUNTS;

@AllArgsConstructor
@Repository
public class SavingsAccountRepository {

    private final DSLContext dsl;

    public void create(SavingsAccount savingsAccount) {
        dsl.insertInto(SAVINGS_ACCOUNTS)
                .set(SAVINGS_ACCOUNTS.ACCOUNT_ID, savingsAccount.getAccountId())
                .set(SAVINGS_ACCOUNTS.INTEREST_RATE, savingsAccount.getInterestRate())
                .set(SAVINGS_ACCOUNTS.MATURITY_DATE, savingsAccount.getMaturityDate())
                .set(SAVINGS_ACCOUNTS.LAST_INTEREST_CALCULATION, savingsAccount.getLastInterestCalculation())
                .set(SAVINGS_ACCOUNTS.ALLOW_WITHDRAWAL, savingsAccount.isAllowWithdrawal())
                .set(SAVINGS_ACCOUNTS.ALLOW_TOPUP, savingsAccount.isAllowTopup())
                .set(SAVINGS_ACCOUNTS.EARLY_WITHDRAWAL_PENALTY, savingsAccount.getEarlyWithdrawalPenalty())
                .set(SAVINGS_ACCOUNTS.STATUS, "ACTIVE")
                .set(SAVINGS_ACCOUNTS.AUTO_PROLONG, false)
                .execute();
    }

    public SavingsAccount get(UUID accountId) {
        return dsl.selectFrom(SAVINGS_ACCOUNTS)
                .where(SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOneInto(SavingsAccount.class);
    }

    public boolean exists(UUID accountId) {
        return dsl.fetchExists(SAVINGS_ACCOUNTS, SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(accountId));
    }

    public void increaseAccruedInterest(UUID accountId, BigDecimal amount) {
        dsl.update(SAVINGS_ACCOUNTS)
                .set(SAVINGS_ACCOUNTS.ACCRUED_INTEREST, SAVINGS_ACCOUNTS.ACCRUED_INTEREST.add(amount))
                .where(SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public void setAccruedInterestToZero(UUID accountId) {
        dsl.update(SAVINGS_ACCOUNTS)
                .set(SAVINGS_ACCOUNTS.ACCRUED_INTEREST, BigDecimal.ZERO)
                .where(SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public void setExpired(UUID accountId) {
        dsl.update(SAVINGS_ACCOUNTS)
                .set(SAVINGS_ACCOUNTS.STATUS, "EXPIRED")
                .where(SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public List<SavingsAccount> findAllActive() {
        return dsl.select()
                .from(SAVINGS_ACCOUNTS)
                .join(BANK_ACCOUNTS).on(SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(BANK_ACCOUNTS.ACCOUNT_ID))
                .where(BANK_ACCOUNTS.ACTIVE.eq(true))
                .and(SAVINGS_ACCOUNTS.MATURITY_DATE.greaterOrEqual(LocalDate.now()))
                .fetchInto(SavingsAccount.class);
    }

    public List<SavingsAccount> findForInterestCalculation(LocalDate calculationDate) {
        return dsl.select()
                .from(SAVINGS_ACCOUNTS)
                .join(BANK_ACCOUNTS).on(SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(BANK_ACCOUNTS.ACCOUNT_ID))
                .where(BANK_ACCOUNTS.ACTIVE.eq(true))
                .and(SAVINGS_ACCOUNTS.MATURITY_DATE.greaterOrEqual(LocalDate.now()))
                .and(SAVINGS_ACCOUNTS.LAST_INTEREST_CALCULATION.ne(calculationDate))
                .fetchInto(SavingsAccount.class);
    }

    public void updateLastInterestCalculation(UUID accountId, LocalDate date) {
        dsl.update(SAVINGS_ACCOUNTS)
                .set(SAVINGS_ACCOUNTS.LAST_INTEREST_CALCULATION, date)
                .where(SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public void update(SavingsAccount account) {
        dsl.update(SAVINGS_ACCOUNTS)
                .set(SAVINGS_ACCOUNTS.ACCOUNT_ID, account.getAccountId())
                .set(SAVINGS_ACCOUNTS.INTEREST_RATE, account.getInterestRate())
                .set(SAVINGS_ACCOUNTS.MATURITY_DATE, account.getMaturityDate())
                .set(SAVINGS_ACCOUNTS.LAST_INTEREST_CALCULATION, account.getLastInterestCalculation())
                .set(SAVINGS_ACCOUNTS.ALLOW_WITHDRAWAL, account.isAllowWithdrawal())
                .set(SAVINGS_ACCOUNTS.ALLOW_TOPUP, account.isAllowTopup())
                .set(SAVINGS_ACCOUNTS.EARLY_WITHDRAWAL_PENALTY, account.getEarlyWithdrawalPenalty())
                .set(SAVINGS_ACCOUNTS.STATUS, account.getStatus())
                .set(SAVINGS_ACCOUNTS.AUTO_PROLONG, account.isAutoProlong())
                .where(SAVINGS_ACCOUNTS.ACCOUNT_ID.eq(account.getAccountId()))
                .execute();
    }
}
