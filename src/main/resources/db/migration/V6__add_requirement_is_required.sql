-- 요구사항 필수 여부 필드 추가
ALTER TABLE requirements ADD COLUMN is_required BOOLEAN NOT NULL DEFAULT false;
