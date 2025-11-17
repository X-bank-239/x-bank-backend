CREATE TABLE transactions (
    transaction_id    UUID PRIMARY KEY,
    sender_id         UUID,
    receiver_id       UUID,
    amount            NUMERIC(19, 4) NOT NULL,
    currency          currency_type NOT NULL,
    transaction_date  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    transaction_type  transaction_type NOT NULL,
    comment           VARCHAR(255),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);