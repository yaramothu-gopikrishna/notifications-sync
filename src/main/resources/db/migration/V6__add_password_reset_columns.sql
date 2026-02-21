ALTER TABLE users ADD COLUMN password_reset_token VARCHAR(128);
ALTER TABLE users ADD COLUMN password_reset_expires_at TIMESTAMPTZ;

CREATE INDEX ix_users_reset_token ON users (password_reset_token) WHERE password_reset_token IS NOT NULL;
