CREATE TABLE currency_rates (
    currency          currency_type NOT NULL,
    rate              NUMERIC(19, 4) NOT NULL,
    date              DATE NOT NULL,
    created_at        TIMESTAMP DEFAULT NOW(),

    UNIQUE(currency, date)
);

CREATE INDEX idx_currency_rates_date ON currency_rates(date);