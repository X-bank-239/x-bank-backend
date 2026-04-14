CREATE TABLE transaction_categories (
    code                VARCHAR(50) PRIMARY KEY,
    display_name        VARCHAR(100) NOT NULL,
    color_code          VARCHAR(7) DEFAULT '#9E9E9E',
    is_active           BOOLEAN DEFAULT TRUE
);

-- Дефолтные категории
INSERT INTO transaction_categories (code, display_name, color_code) VALUES
('FOOD', 'Еда и продукты', '#4CAF50'),
('TRANSPORT', 'Транспорт', '#2196F3'),
('ENTERTAINMENT', 'Развлечения', '#9C27B0'),
('UTILITIES', 'ЖКХ и связь', '#FF9800'),
('SALARY', 'Зарплата', '#8BC34A'),
('OTHER', 'Прочее', '#9E9E9E');