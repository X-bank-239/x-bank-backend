INSERT INTO users (user_id, first_name, last_name, email)
VALUES ('00000000-0000-4000-8000-000000000001'::uuid,
        'System',
        'LoanService',
        'loan-service@xbank.internal');

INSERT INTO bank_accounts (account_id, user_id, balance, currency, account_type)
VALUES ('00000000-0000-4000-8000-000000000002'::uuid,
        '00000000-0000-4000-8000-000000000001'::uuid,
        0.00,
        'RUB',
        'DEBIT');
INSERT INTO bank_accounts (account_id, user_id, balance, currency, account_type)
VALUES ('00000000-0000-4000-8000-000000000003'::uuid,
        '00000000-0000-4000-8000-000000000001'::uuid,
        0.00,
        'USD',
        'DEBIT');
INSERT INTO bank_accounts (account_id, user_id, balance, currency, account_type)
VALUES ('00000000-0000-4000-8000-000000000004'::uuid,
        '00000000-0000-4000-8000-000000000001'::uuid,
        0.00,
        'EUR',
        'DEBIT');
INSERT INTO bank_accounts (account_id, user_id, balance, currency, account_type)
VALUES ('00000000-0000-4000-8000-000000000005'::uuid,
        '00000000-0000-4000-8000-000000000001'::uuid,
        0.00,
        'CNY',
        'DEBIT');
