CREATE TABLE notifications (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email_account_id        UUID         REFERENCES email_accounts(id) ON DELETE SET NULL,
    notification_channel_id UUID         REFERENCES notification_channels(id) ON DELETE SET NULL,
    sender_name             VARCHAR(255),
    sender_address          VARCHAR(320),
    subject                 VARCHAR(998),
    preview                 VARCHAR(200),
    delivery_status         VARCHAR(20)  NOT NULL DEFAULT 'pending'
                            CHECK (delivery_status IN ('pending', 'sent', 'delivered', 'failed', 'batched')),
    external_message_id     VARCHAR(255),
    retry_count             SMALLINT     NOT NULL DEFAULT 0,
    email_received_at       TIMESTAMPTZ,
    delivered_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_notifications_user_id ON notifications(user_id, created_at DESC);
CREATE INDEX ix_notifications_delivery_status ON notifications(delivery_status);
CREATE INDEX ix_notifications_retry ON notifications(delivery_status, retry_count)
    WHERE delivery_status = 'failed' AND retry_count < 3;
CREATE INDEX ix_notifications_retention ON notifications(created_at);
