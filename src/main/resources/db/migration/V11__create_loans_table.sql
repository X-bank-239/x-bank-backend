CREATE TYPE loan_status AS ENUM ('ACTIVE', 'CLOSED');

CREATE TABLE loans (
    loan_id                UUID PRIMARY KEY,
    user_id                UUID NOT NULL,
    credit_account_id      UUID NOT NULL,
    service_account_id     UUID NOT NULL,
    currency               currency_type NOT NULL,
    principal_amount       NUMERIC(19, 4) NOT NULL,
    annual_interest_rate   NUMERIC(7, 4) NOT NULL,
    term_months            INT NOT NULL,
    monthly_payment        NUMERIC(19, 4) NOT NULL,
    outstanding_principal  NUMERIC(19, 4) NOT NULL,
    next_payment_date      DATE NOT NULL,
    status                 loan_status NOT NULL DEFAULT 'ACTIVE',
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at              TIMESTAMPTZ
);

ALTER TABLE loans
    ADD CONSTRAINT fk_loan_user
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_loan_credit_account
        FOREIGN KEY(credit_account_id)
        REFERENCES bank_accounts(account_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_loan_service_account
        FOREIGN KEY(service_account_id)
        REFERENCES bank_accounts(account_id)
        ON DELETE CASCADE;

ALTER TABLE loans
    ADD CONSTRAINT chk_loan_principal_positive CHECK (principal_amount > 0),
    ADD CONSTRAINT chk_loan_rate_positive CHECK (annual_interest_rate > 0),
    ADD CONSTRAINT chk_loan_term_positive CHECK (term_months > 0),
    ADD CONSTRAINT chk_loan_monthly_payment_positive CHECK (monthly_payment > 0),
    ADD CONSTRAINT chk_loan_outstanding_non_negative CHECK (outstanding_principal >= 0);

