package com.example.xbankbackend.repositories;

import com.example.xbankbackend.generated.tables.BankAccounts;
import com.example.xbankbackend.models.BankAccount;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import com.example.xbankbackend.config.JOOQConfig;

import java.sql.SQLException;

@Repository
public class BankAccountRepository {
    private final DSLContext dsl = JOOQConfig.createDSLContext();

    public BankAccountRepository() throws SQLException {
    }

    public void createBankAccount(BankAccount bankAccount) {
        dsl.insertInto(BankAccounts.BANK_ACCOUNTS)
                .values(bankAccount.getAccountId(), bankAccount.getUserId(), bankAccount.getAmount(), bankAccount.getCurrency(), bankAccount.getAccountType());
    }
}
