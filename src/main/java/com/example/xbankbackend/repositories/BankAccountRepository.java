package com.example.xbankbackend.repositories;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.models.BankAccount;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.BANK_ACCOUNTS;

@AllArgsConstructor
@Repository
public class BankAccountRepository {

    private final DSLContext dsl;

    public void create(BankAccount bankAccount) {
        dsl.insertInto(BANK_ACCOUNTS)
                .values(bankAccount.getAccountId(), bankAccount.getUserId(), bankAccount.getBalance(), bankAccount.getCurrency(), bankAccount.getAccountType(), bankAccount.getActive())
                .execute();
    }

    public void deactivate(UUID accountId) {
        dsl.update(BANK_ACCOUNTS)
                .set(BANK_ACCOUNTS.ACTIVE, false)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public void reactivate(UUID accountId) {
        dsl.update(BANK_ACCOUNTS)
                .set(BANK_ACCOUNTS.ACTIVE, true)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public boolean isActive(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .and(BANK_ACCOUNTS.ACTIVE)
                .fetch()
                .size() == 1;
    }

    public void increaseBalance(UUID accountId, BigDecimal amount) {
        dsl.update(BANK_ACCOUNTS)
                .set(BANK_ACCOUNTS.BALANCE, BANK_ACCOUNTS.BALANCE.add(amount))
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public void decreaseBalance(UUID accountId, BigDecimal amount) {
        dsl.update(BANK_ACCOUNTS)
                .set(BANK_ACCOUNTS.BALANCE, BANK_ACCOUNTS.BALANCE.subtract(amount))
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public BankAccount get(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .into(BankAccount.class);
    }

    public boolean exists(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetch()
                .size() == 1;
    }

    public UUID getUserId(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.USER_ID);
    }

    public CurrencyType getCurrency(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.CURRENCY, CurrencyType.class);
    }

    public BankAccountType getAccountType(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.ACCOUNT_TYPE, BankAccountType.class);
    }

    public BigDecimal getBalance(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.BALANCE, BigDecimal.class);
    }

    public List<BankAccount> getBankAccounts(UUID userId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.USER_ID.eq(userId))
                .fetchInto(BankAccount.class);
    }
}
