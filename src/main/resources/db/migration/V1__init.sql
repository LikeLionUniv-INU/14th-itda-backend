-- ============================================
-- ITDA Backend - Initial Schema
-- ============================================

-- 1. users
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(50)  NOT NULL,
    last_name   VARCHAR(50)  NOT NULL,
    country     VARCHAR(100) NOT NULL,
    language    VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_users_email ON users (email);

-- 2. refresh_tokens
CREATE TABLE refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

-- 3. team_projects
CREATE TABLE team_projects (
    id               BIGSERIAL    PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    default_language VARCHAR(20)  NOT NULL,
    invite_code      VARCHAR(6)   NOT NULL UNIQUE,
    created_by       BIGINT       NOT NULL REFERENCES users (id),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_team_projects_invite_code ON team_projects (invite_code);
CREATE INDEX idx_team_projects_created_by ON team_projects (created_by);

-- 4. team_members
CREATE TABLE team_members (
    id              BIGSERIAL   PRIMARY KEY,
    team_project_id BIGINT      NOT NULL REFERENCES team_projects (id) ON DELETE CASCADE,
    user_id         BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    joined_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_team_members UNIQUE (team_project_id, user_id)
);

CREATE INDEX idx_team_members_team_project_id ON team_members (team_project_id);
CREATE INDEX idx_team_members_user_id ON team_members (user_id);

-- 5. documents
CREATE TABLE documents (
    id              BIGSERIAL   PRIMARY KEY,
    team_project_id BIGINT      NOT NULL REFERENCES team_projects (id) ON DELETE CASCADE,
    name            VARCHAR(10) NOT NULL,
    language        VARCHAR(20) NOT NULL,
    document_type   VARCHAR(20) NOT NULL DEFAULT 'STORYBOARD',
    created_by      BIGINT      NOT NULL REFERENCES users (id),
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_team_project_id ON documents (team_project_id);
CREATE INDEX idx_documents_created_by ON documents (created_by);

-- 6. document_versions
CREATE TABLE document_versions (
    id             BIGSERIAL    PRIMARY KEY,
    document_id    BIGINT       NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    version        INT          NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    is_auto_saved  BOOLEAN      NOT NULL DEFAULT FALSE,
    change_summary VARCHAR(500),
    created_by     BIGINT       NOT NULL REFERENCES users (id),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_document_versions UNIQUE (document_id, version)
);

CREATE INDEX idx_document_versions_document_id ON document_versions (document_id);

-- 7. pages
CREATE TABLE pages (
    id                  BIGSERIAL   PRIMARY KEY,
    document_version_id BIGINT      NOT NULL REFERENCES document_versions (id) ON DELETE CASCADE,
    page_number         INT         NOT NULL,
    screen_name         VARCHAR(10),
    screen_id           VARCHAR(10),
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pages_document_version_id ON pages (document_version_id);

-- 8. wireframe_images
CREATE TABLE wireframe_images (
    id              BIGSERIAL    PRIMARY KEY,
    page_id         BIGINT       NOT NULL REFERENCES pages (id) ON DELETE CASCADE,
    image_type      VARCHAR(10)  NOT NULL,
    image_url       VARCHAR(500) NOT NULL,
    original_width  INT,
    original_height INT,
    display_width   INT          NOT NULL,
    display_height  INT          NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wireframe_images_page_id ON wireframe_images (page_id);

-- 9. pins
CREATE TABLE pins (
    id           BIGSERIAL        PRIMARY KEY,
    page_id      BIGINT           NOT NULL REFERENCES pages (id) ON DELETE CASCADE,
    pin_number   INT              NOT NULL,
    x_coordinate DOUBLE PRECISION NOT NULL,
    y_coordinate DOUBLE PRECISION NOT NULL,
    created_at   TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pins_page_id ON pins (page_id);

-- 10. requirements
CREATE TABLE requirements (
    id         BIGSERIAL    PRIMARY KEY,
    pin_id     BIGINT       NOT NULL REFERENCES pins (id) ON DELETE CASCADE,
    tab_type   VARCHAR(20)  NOT NULL,
    item_name  VARCHAR(10),
    content    VARCHAR(200),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_requirements_pin_id ON requirements (pin_id);

-- 11. translation_jobs
CREATE TABLE translation_jobs (
    id                  BIGSERIAL   PRIMARY KEY,
    document_version_id BIGINT      NOT NULL REFERENCES document_versions (id) ON DELETE CASCADE,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_languages     INT         NOT NULL,
    completed_languages INT         NOT NULL DEFAULT 0,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP
);

CREATE INDEX idx_translation_jobs_document_version_id ON translation_jobs (document_version_id);

-- 12. translation_languages
CREATE TABLE translation_languages (
    id                 BIGSERIAL   PRIMARY KEY,
    translation_job_id BIGINT      NOT NULL REFERENCES translation_jobs (id) ON DELETE CASCADE,
    target_language    VARCHAR(20) NOT NULL,
    target_user_id     BIGINT      REFERENCES users (id),
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMP   NOT NULL DEFAULT NOW(),
    completed_at       TIMESTAMP
);

CREATE INDEX idx_translation_languages_job_id ON translation_languages (translation_job_id);

-- 13. translated_requirements
CREATE TABLE translated_requirements (
    id                      BIGSERIAL    PRIMARY KEY,
    translation_language_id BIGINT       NOT NULL REFERENCES translation_languages (id) ON DELETE CASCADE,
    requirement_id          BIGINT       NOT NULL REFERENCES requirements (id) ON DELETE CASCADE,
    translated_item_name    VARCHAR(50),
    translated_content      VARCHAR(500),
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_translated_requirements_translation_language_id ON translated_requirements (translation_language_id);
CREATE INDEX idx_translated_requirements_requirement_id ON translated_requirements (requirement_id);
