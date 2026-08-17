-- 1. users 테이블에 프로필 필드 추가
ALTER TABLE users ADD COLUMN bio VARCHAR(500);
ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(500);

-- 2. 계정 삭제를 위한 FK 제약조건 변경

-- team_projects.created_by → SET NULL
ALTER TABLE team_projects ALTER COLUMN created_by DROP NOT NULL;
ALTER TABLE team_projects DROP CONSTRAINT team_projects_created_by_fkey;
ALTER TABLE team_projects ADD CONSTRAINT team_projects_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

-- documents.created_by → SET NULL
ALTER TABLE documents ALTER COLUMN created_by DROP NOT NULL;
ALTER TABLE documents DROP CONSTRAINT documents_created_by_fkey;
ALTER TABLE documents ADD CONSTRAINT documents_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

-- document_versions.created_by → SET NULL
ALTER TABLE document_versions ALTER COLUMN created_by DROP NOT NULL;
ALTER TABLE document_versions DROP CONSTRAINT document_versions_created_by_fkey;
ALTER TABLE document_versions ADD CONSTRAINT document_versions_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

-- activity_logs.performed_by → SET NULL
ALTER TABLE activity_logs ALTER COLUMN performed_by DROP NOT NULL;
ALTER TABLE activity_logs DROP CONSTRAINT activity_logs_performed_by_fkey;
ALTER TABLE activity_logs ADD CONSTRAINT activity_logs_performed_by_fkey
    FOREIGN KEY (performed_by) REFERENCES users (id) ON DELETE SET NULL;

-- document_changes.modified_by → SET NULL
ALTER TABLE document_changes ALTER COLUMN modified_by DROP NOT NULL;
ALTER TABLE document_changes DROP CONSTRAINT document_changes_modified_by_fkey;
ALTER TABLE document_changes ADD CONSTRAINT document_changes_modified_by_fkey
    FOREIGN KEY (modified_by) REFERENCES users (id) ON DELETE SET NULL;

-- change_confirmations.confirmed_by → CASCADE
ALTER TABLE change_confirmations DROP CONSTRAINT change_confirmations_confirmed_by_fkey;
ALTER TABLE change_confirmations ADD CONSTRAINT change_confirmations_confirmed_by_fkey
    FOREIGN KEY (confirmed_by) REFERENCES users (id) ON DELETE CASCADE;

-- translation_languages.target_user_id → SET NULL
ALTER TABLE translation_languages DROP CONSTRAINT translation_languages_target_user_id_fkey;
ALTER TABLE translation_languages ADD CONSTRAINT translation_languages_target_user_id_fkey
    FOREIGN KEY (target_user_id) REFERENCES users (id) ON DELETE SET NULL;
