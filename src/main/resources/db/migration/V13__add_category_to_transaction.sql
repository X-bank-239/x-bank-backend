ALTER TABLE transactions ADD COLUMN category VARCHAR(50) DEFAULT 'OTHER';

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_category
        FOREIGN KEY (category)
        REFERENCES transaction_categories(code)
        ON DELETE SET NULL;

CREATE INDEX idx_transactions_category ON transactions(category);
