CREATE TABLE notification_channels (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel_type            VARCHAR(20)  NOT NULL
                            CHECK (channel_type IN ('slack', 'whatsapp')),
    status                  VARCHAR(20)  NOT NULL DEFAULT 'active'
                            CHECK (status IN ('active', 'error', 'disconnected')),
    bot_token_encrypted     TEXT,
    slack_channel_id        VARCHAR(255),
    whatsapp_phone_number   VARCHAR(20),
    twilio_sid              VARCHAR(255),
    key_version             SMALLINT     NOT NULL DEFAULT 1,
    consent_given           BOOLEAN      NOT NULL DEFAULT FALSE,
    consent_given_at        TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (user_id, channel_type)
);

CREATE INDEX ix_notification_channels_user_id ON notification_channels(user_id);
CREATE INDEX ix_notification_channels_status ON notification_channels(user_id, status);
