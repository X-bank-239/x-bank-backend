-- enums
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED');

ALTER TABLE users ADD role user_role DEFAULT 'USER';
ALTER TABLE users ADD active BOOLEAN DEFAULT TRUE;
ALTER TABLE transactions ADD status transaction_status DEFAULT 'PENDING';
ALTER TABLE bank_accounts ADD active BOOLEAN DEFAULT TRUE;
