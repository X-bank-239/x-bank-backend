CREATE TABLE transaction_keywords (
    word                    VARCHAR(100) NOT NULL,
    category_code           VARCHAR(50) NOT NULL REFERENCES transaction_categories(code),
    created_at TIMESTAMPTZ  DEFAULT NOW(),

    PRIMARY KEY (word, category_code)
);

CREATE INDEX idx_keywords_word ON transaction_keywords(word);

-- Дефолтные кейворды
INSERT INTO transaction_keywords (word, category_code) VALUES
('магнит', 'FOOD'), ('перекресток', 'FOOD'), ('пятерочка', 'FOOD'),
('такси', 'TRANSPORT'), ('метро', 'TRANSPORT'),
('мтс', 'UTILITIES'), ('билайн', 'UTILITIES'), ('ростелеком', 'UTILITIES');