package com.example.xbankbackend.repositories;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.models.BankAccount;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.BANK_ACCOUNTS;
import static com.example.xbankbackend.generated.Tables.USERS;

@AllArgsConstructor
@Repository
public class BankAccountRepository {

    private final DSLContext dsl;

    public void create(BankAccount bankAccount) {
        dsl.insertInto(BANK_ACCOUNTS)
                .values(bankAccount.getAccountId(), bankAccount.getUserId(), bankAccount.getBalance(), bankAccount.getCurrency(), bankAccount.getAccountType())
                .execute();
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

    public BigDecimal getBalance(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.BALANCE, BigDecimal.class);
    }

    public List<BankAccount> getBankAccounts(UUID accountId) {
        return dsl.select()
                .from(BANK_ACCOUNTS)
                .join(USERS).on(BANK_ACCOUNTS.USER_ID.eq(USERS.USER_ID))
                .where(USERS.USER_ID.eq(accountId))
                .fetch()
                .into(BankAccount.class);
    }
}
