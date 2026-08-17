CREATE TABLE document_changes (
    id                  BIGSERIAL    PRIMARY KEY,
    document_version_id BIGINT       NOT NULL REFERENCES document_versions (id) ON DELETE CASCADE,
    change_type         VARCHAR(30)  NOT NULL,
    page_number         INT          NOT NULL,
    screen_name         VARCHAR(10),
    pin_number          INT,
    item_description    VARCHAR(100),
    before_value        TEXT,
    after_value         TEXT,
    modified_by         BIGINT       NOT NULL REFERENCES users (id),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_document_changes_version_id ON document_changes (document_version_id);

CREATE TABLE change_confirmations (
    id                  BIGSERIAL  PRIMARY KEY,
    document_change_id  BIGINT     NOT NULL REFERENCES document_changes (id) ON DELETE CASCADE,
    confirmed_by        BIGINT     NOT NULL REFERENCES users (id),
    confirmed_at        TIMESTAMP  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_change_confirmation UNIQUE (document_change_id, confirmed_by)
);

CREATE INDEX idx_change_confirmations_change_id ON change_confirmations (document_change_id);
