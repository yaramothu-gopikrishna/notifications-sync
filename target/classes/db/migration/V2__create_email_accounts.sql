CREATE TABLE email_accounts (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider                VARCHAR(50)  NOT NULL DEFAULT 'gmail',
    email_address           VARCHAR(320) NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'active'
                            CHECK (status IN ('active', 'paused', 'error')),
    access_token_encrypted  TEXT,
    refresh_token_encrypted TEXT,
    key_version             SMALLINT     NOT NULL DEFAULT 1,
    history_id              VARCHAR(255),
    token_expires_at        TIMESTAMPTZ,
    last_scanned_at         TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (user_id, email_address)
);

CREATE INDEX ix_email_accounts_user_id ON email_accounts(user_id);
CREATE INDEX ix_email_accounts_status ON email_accounts(status);
CREATE INDEX ix_email_accounts_token_expires ON email_accounts(token_expires_at)
    WHERE token_expires_at IS NOT NULL;
