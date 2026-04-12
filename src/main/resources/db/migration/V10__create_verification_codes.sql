CREATE TABLE verification_codes (
    id UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID    NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    code_hash       VARCHAR(255) NOT NULL,
    purpose         VARCHAR(32) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    used BOOLEAN    DEFAULT FALSE
);

CREATE INDEX idx_verification_lookup ON verification_codes(user_id, purpose, used, expires_at);