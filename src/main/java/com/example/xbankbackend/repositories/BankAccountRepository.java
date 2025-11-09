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

    public void increaseBalance(UUID uuid, float amount) {
        dsl.update(BANK_ACCOUNTS)
                .set(BANK_ACCOUNTS.BALANCE, BANK_ACCOUNTS.BALANCE.add(amount))
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .execute();
    }

    public void decreaseBalance(UUID uuid, float amount) {
        dsl.update(BANK_ACCOUNTS)
                .set(BANK_ACCOUNTS.BALANCE, BANK_ACCOUNTS.BALANCE.subtract(amount))
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .execute();
    }

    public boolean haveUUID(UUID uuid) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .fetch()
                .size() == 1;
    }

    public UUID getUser(UUID uuid) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.USER_ID);
    }

    public CurrencyType getCurrency(UUID uuid) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.CURRENCY, CurrencyType.class);
    }

    public Float getBalance(UUID uuid) {
        return dsl.selectFrom(BANK_ACCOUNTS)
                .where(BANK_ACCOUNTS.ACCOUNT_ID.eq(uuid))
                .fetchOne()
                .getValue(BANK_ACCOUNTS.BALANCE, Float.class);
    }

    public List<BankAccount> getBankAccounts(UUID uuid) {
        return dsl.select()
                .from(BANK_ACCOUNTS)
                .join(USERS).on(BANK_ACCOUNTS.USER_ID.eq(USERS.USER_ID))
                .where(USERS.USER_ID.eq(uuid))
                .fetch()
                .into(BankAccount.class);
    }
}
