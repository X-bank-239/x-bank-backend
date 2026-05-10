CREATE TABLE savings_accounts (
    account_id                  UUID PRIMARY KEY,
    accrued_interest            NUMERIC(19, 2) DEFAULT 0,
    interest_rate               NUMERIC(5,2) NOT NULL,
    maturity_date               DATE NOT NULL,
    last_interest_calculation   DATE NOT NULL,
    allow_withdrawal            BOOLEAN DEFAULT FALSE,
    allow_topup                 BOOLEAN DEFAULT FALSE,
    early_withdrawal_penalty    NUMERIC(5,2) DEFAULT 100,
    status                      VARCHAR(20) DEFAULT 'ACTIVE',
    auto_prolong                BOOLEAN DEFAULT FALSE
);

ALTER TABLE savings_accounts
    ADD CONSTRAINT fk_savings_account
        FOREIGN KEY(account_id)
        REFERENCES bank_accounts(account_id)
        ON DELETE CASCADE;