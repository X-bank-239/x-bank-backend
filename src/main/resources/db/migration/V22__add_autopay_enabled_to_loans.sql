ALTER TABLE loans
    ADD COLUMN IF NOT EXISTS autopay_enabled boolean NOT NULL DEFAULT false;

