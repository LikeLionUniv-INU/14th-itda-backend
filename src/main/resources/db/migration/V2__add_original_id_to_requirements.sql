-- ============================================
-- V2: Add original_id to requirements for version change tracking
-- ============================================

ALTER TABLE requirements
    ADD COLUMN original_id BIGINT REFERENCES requirements (id) ON DELETE SET NULL;

CREATE INDEX idx_requirements_original_id ON requirements (original_id);
