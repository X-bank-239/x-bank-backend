ALTER TABLE loans
      DROP CONSTRAINT fk_loan_credit_account;

ALTER TABLE loans RENAME COLUMN credit_account_id TO debit_account_id;


ALTER TABLE loans
    ADD CONSTRAINT fk_loan_debit_account_id
        FOREIGN KEY (debit_account_id)
            REFERENCES bank_accounts (account_id)
            ON DELETE SET NULL;


