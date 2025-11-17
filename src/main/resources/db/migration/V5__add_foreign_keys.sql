ALTER TABLE bank_accounts
    ADD CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE;

ALTER TABLE transactions
    ADD CONSTRAINT fk_sender
        FOREIGN KEY(sender_id)
        REFERENCES bank_accounts(account_id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_receiver
        FOREIGN KEY(receiver_id)
        REFERENCES bank_accounts(account_id)
        ON DELETE SET NULL;