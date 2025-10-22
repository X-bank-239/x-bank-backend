package com.example.xbankbackend.repositories;

import com.example.xbankbackend.generated.tables.BankAccounts;
import com.example.xbankbackend.models.BankAccount;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import com.example.xbankbackend.config.JOOQConfig;

import java.sql.SQLException;
import java.util.UUID;

@Repository
public class BankAccountRepository {
    private final DSLContext dsl = JOOQConfig.createDSLContext();

    public BankAccountRepository() throws SQLException {
    }

    public void createBankAccount(BankAccount bankAccount) {
        dsl.insertInto(BankAccounts.BANK_ACCOUNTS)
                .values(bankAccount.getAccountId(), bankAccount.getUserId(), bankAccount.getAmount(), bankAccount.getCurrency(), bankAccount.getAccountType())
                .execute();
    }

    public void changeBalance(UUID uuid, float amount) {
        dsl.update(BankAccounts.BANK_ACCOUNTS)
                .set(BankAccounts.BANK_ACCOUNTS.AMOUNT, BankAccounts.BANK_ACCOUNTS.AMOUNT.add(amount))
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
}
