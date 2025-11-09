package com.example.xbankbackend.repositories;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.models.BankAccount;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.BANK_ACCOUNTS;
import static com.example.xbankbackend.generated.Tables.USERS;

@Repository
public class BankAccountRepository {
    private final DSLContext dsl;

    @Autowired
    public BankAccountRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void createBankAccount(BankAccount bankAccount) {
        dsl.insertInto(BANK_ACCOUNTS)
                .values(bankAccount.getAccountId(), bankAccount.getUserId(), bankAccount.getBalance(), bankAccount.getCurrency(), bankAccount.getAccountType())
                .execute();
    }

    public void increaseBalanceByAccountId(UUID accountId, float amount) {
        dsl.update(BANK_ACCOUNTS)
                .set(BANK_ACCOUNTS.BALANCE, BANK_ACCOUNTS.BALANCE.add(amount))
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public void decreaseBalanceByAccountId(UUID accountId, float amount) {
        dsl.update(BANK_ACCOUNTS)
                .set(BANK_ACCOUNTS.BALANCE, BANK_ACCOUNTS.BALANCE.subtract(amount))
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .execute();
    }

    public boolean haveAccountId(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetch()
                .size() == 1;
    }

    public UUID getUserIdByAccountId(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.USER_ID);
    }

    public CurrencyType getCurrencyByAccountId(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.CURRENCY, CurrencyType.class);
    }

    public Float getBalanceByAccountId(UUID accountId) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(accountId))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.BALANCE, Float.class);
    }

    public List<BankAccount> getBankAccountsByAccountId(UUID accountId) {
        return dsl.select()
                .from(BANK_ACCOUNTS)
                .join(USERS).on(BANK_ACCOUNTS.USER_ID.eq(USERS.USER_ID))
                .where(USERS.USER_ID.eq(accountId))
                .fetch()
                .into(BankAccount.class);
    }
}
