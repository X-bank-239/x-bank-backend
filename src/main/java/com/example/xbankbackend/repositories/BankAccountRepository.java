package com.example.xbankbackend.repositories;

import com.example.xbankbackend.generated.tables.BankAccounts;
import com.example.xbankbackend.models.BankAccount;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class BankAccountRepository {
    private final DSLContext dsl;

    @Autowired
    public BankAccountRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void createBankAccount(BankAccount bankAccount) {
        dsl.insertInto(BankAccounts.BANK_ACCOUNTS)
                .values(bankAccount.getAccountId(), bankAccount.getUserId(), bankAccount.getAmount(), bankAccount.getCurrency(), bankAccount.getAccountType())
                .execute();
    }

    public void increaseBalance(UUID uuid, float amount) {
        dsl.update(BankAccounts.BANK_ACCOUNTS)
                .set(BankAccounts.BANK_ACCOUNTS.BALANCE, BankAccounts.BANK_ACCOUNTS.BALANCE.add(amount))
                .where(BankAccounts.BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .execute();
    }

    public void decreaseBalance(UUID uuid, float amount) {
        dsl.update(BankAccounts.BANK_ACCOUNTS)
                .set(BankAccounts.BANK_ACCOUNTS.BALANCE, BankAccounts.BANK_ACCOUNTS.BALANCE.subtract(amount))
                .where(BankAccounts.BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .execute();
    }

    public boolean haveUUID(UUID uuid) {
        return dsl.selectFrom(BankAccounts.BANK_ACCOUNTS)
                .where(BankAccounts.BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .fetch()
                .size() == 1;
    }

    public String getCurrency(UUID uuid) {
        return dsl.selectFrom(BankAccounts.BANK_ACCOUNTS)
                .where(BankAccounts.BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .fetchOne()
                .getValue(BankAccounts.BANK_ACCOUNTS.CURRENCY, String.class);
    }

    public Float getBalance(UUID uuid) {
        return dsl.selectFrom(BankAccounts.BANK_ACCOUNTS)
                .where(BankAccounts.BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .fetchOne()
                .getValue(BankAccounts.BANK_ACCOUNTS.BALANCE, Float.class);
    }
}
