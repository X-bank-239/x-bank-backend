CREATE TABLE bank_accounts (
    account_id    UUID PRIMARY KEY,
    user_id       UUID NOT NULL,
    balance       NUMERIC(19, 4) NOT NULL DEFAULT 0.00,
    currency      currency_type NOT NULL,
    account_type  bank_account_type NOT NULL
);

CREATE INDEX idx_bank_accounts_user_id ON bank_accounts(user_id);