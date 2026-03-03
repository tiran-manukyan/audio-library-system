CREATE TABLE IF NOT EXISTS resources
(
    id          BIGSERIAL PRIMARY KEY,
    data        OID       NOT NULL,
    upload_time TIMESTAMP NOT NULL DEFAULT Now()
);

CREATE TABLE outbox_events
(
    id         BIGSERIAL PRIMARY KEY,
    type       VARCHAR(40) NOT NULL,
    entity_id  BIGINT      NOT NULL,
    payload    TEXT        NOT NULL,
    attempts   INTEGER     NOT NULL DEFAULT 0,
    last_error VARCHAR(2000),
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Unique constraint
ALTER TABLE outbox_events
    ADD CONSTRAINT uk_outbox_type_entity_id
        UNIQUE (type, entity_id);

-- Index for processing
CREATE INDEX idx_outbox_processing
    ON outbox_events (type, attempts, created_at);