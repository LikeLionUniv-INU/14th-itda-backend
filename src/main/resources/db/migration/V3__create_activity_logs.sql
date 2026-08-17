CREATE TABLE activity_logs (
    id              BIGSERIAL    PRIMARY KEY,
    team_project_id BIGINT       NOT NULL REFERENCES team_projects (id) ON DELETE CASCADE,
    document_id     BIGINT       REFERENCES documents (id) ON DELETE SET NULL,
    action_type     VARCHAR(20)  NOT NULL,
    document_name   VARCHAR(10)  NOT NULL,
    document_type   VARCHAR(20),
    version         INT,
    performed_by    BIGINT       NOT NULL REFERENCES users (id),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_logs_team_project_id ON activity_logs (team_project_id);
CREATE INDEX idx_activity_logs_created_at ON activity_logs (created_at);
