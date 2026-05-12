CREATE TABLE app_settings (
    setting_key         VARCHAR(100) PRIMARY KEY,
    setting_value       VARCHAR(500) NOT NULL,
    description         TEXT,
    updated_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_by          UUID REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_config_key ON app_settings(setting_key);