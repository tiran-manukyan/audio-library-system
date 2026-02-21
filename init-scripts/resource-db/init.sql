CREATE TABLE IF NOT EXISTS resources
(
    id          BIGSERIAL PRIMARY KEY,
    data        OID       NOT NULL,
    upload_time TIMESTAMP NOT NULL DEFAULT Now()
);
