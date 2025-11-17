-- enums
CREATE TYPE bank_account_type AS ENUM ('CREDIT', 'DEBIT');
CREATE TYPE transaction_type AS ENUM ('PAYMENT', 'TRANSFER', 'DEPOSIT');
CREATE TYPE currency_type AS ENUM ('RUB', 'USD', 'EUR', 'CNY');

-- users
CREATE TABLE users (
    user_id     UUID PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    birthdate   DATE
);