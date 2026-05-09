ALTER TABLE bank_accounts DROP COLUMN account_type;
DROP type bank_account_type CASCADE;
CREATE TYPE bank_account_type AS ENUM ('CREDIT', 'DEBIT', 'SAVINGS');
ALTER table bank_accounts ADD COLUMN account_type bank_account_type;
