CREATE TABLE filter_rules (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rule_type   VARCHAR(20)  NOT NULL
                CHECK (rule_type IN ('sender', 'subject_keyword')),
    pattern     VARCHAR(500) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    priority    INTEGER      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_filter_rules_user_active ON filter_rules(user_id, is_active)
    WHERE is_active = TRUE;
CREATE INDEX ix_filter_rules_user_type ON filter_rules(user_id, rule_type);
