CREATE TABLE team_notifications (
    id BIGSERIAL PRIMARY KEY,
    team_project_id BIGINT NOT NULL REFERENCES team_projects(id) ON DELETE CASCADE,
    document_id BIGINT REFERENCES documents(id) ON DELETE SET NULL,
    document_name VARCHAR(100) NOT NULL,
    before_version INTEGER,
    after_version INTEGER NOT NULL,
    performed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE team_notification_reads (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES team_notifications(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    read_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (notification_id, user_id)
);

CREATE INDEX idx_team_notifications_team_project_id ON team_notifications(team_project_id);
CREATE INDEX idx_team_notification_reads_user_id ON team_notification_reads(user_id);
