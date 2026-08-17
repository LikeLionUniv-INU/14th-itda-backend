# 잇다(ITDA) ERD 설계

> **버전**: v2.0  
> **작성일**: 2026-08-17  
> **DB**: PostgreSQL 17+  
> **마이그레이션**: Flyway V1 ~ V7

---

## 1. 테이블 관계 다이어그램

```
users (사용자)
  │
  ├──< team_members (팀 멤버) >──┐
  │                              │
  │                        team_projects (팀 프로젝트)
  │                              │
  │                              ├──< documents (문서)
  │                              │        │
  │                              │        ├──< document_versions (문서 버전)
  │                              │        │        │
  │                              │        │        ├──< pages (페이지)
  │                              │        │        │        │
  │                              │        │        │        ├──< pins (핀)
  │                              │        │        │        │       │
  │                              │        │        │        │       └──< requirements (요구사항)
  │                              │        │        │        │
  │                              │        │        │        └──< wireframe_images (와이어프레임 이미지)
  │                              │        │        │
  │                              │        │        ├──< document_changes (수정사항)
  │                              │        │        │        │
  │                              │        │        │        └──< change_confirmations (수정사항 확인)
  │                              │        │        │
  │                              │        │        └──< translation_jobs (번역 작업)
  │                              │        │                 │
  │                              │        │                 └──< translation_languages (번역 언어별 상태)
  │                              │        │
  │                              │        └──< translated_requirements (번역된 요구사항)
  │                              │
  │                              ├──< activity_logs (활동 로그)
  │                              │
  │                              └──< team_notifications (팀 알림)
  │                                       │
  │                                       └──< team_notification_reads (알림 읽음)
  │
  └──< refresh_tokens (리프레시 토큰)

범례: ──< = 1:N 관계
```

---

## 2. 테이블 상세 정의

---

### 2.1 users (사용자)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 사용자 고유 ID |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | 이메일 (로그인 ID) |
| `password` | VARCHAR(255) | NOT NULL | 비밀번호 (BCrypt 해시) |
| `first_name` | VARCHAR(50) | NOT NULL | 이름 (영어만) |
| `last_name` | VARCHAR(50) | NOT NULL | 성 (영어만) |
| `country` | VARCHAR(100) | NOT NULL | 국적 |
| `language` | VARCHAR(20) | NOT NULL | 사용 언어 (ko, en, ja, zh, vi 등) |
| `bio` | VARCHAR(500) | | 자기소개 (최대 500자) |
| `profile_image_url` | VARCHAR(500) | | 프로필 이미지 URL (S3) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 가입일시 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스**:
- `idx_users_email` — UNIQUE (email)

---

### 2.2 refresh_tokens (리프레시 토큰)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 토큰 고유 ID |
| `user_id` | BIGINT | FK → users.id, NOT NULL | 사용자 |
| `token` | VARCHAR(500) | UNIQUE, NOT NULL | 리프레시 토큰 값 |
| `expires_at` | TIMESTAMP | NOT NULL | 만료 일시 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |

**인덱스**:
- `idx_refresh_tokens_token` — UNIQUE (token)
- `idx_refresh_tokens_user_id` — (user_id)

**삭제 정책**: 사용자 삭제 시 CASCADE 삭제

---

### 2.3 team_projects (팀 프로젝트)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 팀 프로젝트 고유 ID |
| `name` | VARCHAR(100) | NOT NULL | 프로젝트 이름 |
| `default_language` | VARCHAR(20) | NOT NULL | 기본 언어 |
| `invite_code` | VARCHAR(6) | UNIQUE, NOT NULL | 초대 코드 (영대문자+숫자 6자리) |
| `created_by` | BIGINT | FK → users.id, ON DELETE SET NULL | 생성자 (탈퇴 시 null) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스**:
- `idx_team_projects_invite_code` — UNIQUE (invite_code)
- `idx_team_projects_created_by` — (created_by)

---

### 2.4 team_members (팀 멤버)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 멤버 고유 ID |
| `team_project_id` | BIGINT | FK → team_projects.id, NOT NULL | 팀 프로젝트 |
| `user_id` | BIGINT | FK → users.id, NOT NULL | 사용자 |
| `role` | VARCHAR(20) | NOT NULL | 역할: `LEADER` / `MEMBER` |
| `joined_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 참여일시 |

**제약조건**:
- `uq_team_members` — UNIQUE (team_project_id, user_id) — 중복 참여 방지

**인덱스**:
- `idx_team_members_team_project_id` — (team_project_id)
- `idx_team_members_user_id` — (user_id)

**삭제 정책**: 사용자 삭제 시 CASCADE 삭제

---

### 2.5 documents (문서)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 문서 고유 ID |
| `team_project_id` | BIGINT | FK → team_projects.id, NOT NULL | 소속 팀 프로젝트 |
| `name` | VARCHAR(10) | NOT NULL | 문서 이름 (최대 10자) |
| `language` | VARCHAR(20) | NOT NULL | 작성 언어 |
| `document_type` | VARCHAR(20) | NOT NULL, DEFAULT 'STORYBOARD' | 문서 유형 |
| `created_by` | BIGINT | FK → users.id, ON DELETE SET NULL | 생성자 (탈퇴 시 null) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스**:
- `idx_documents_team_project_id` — (team_project_id)
- `idx_documents_created_by` — (created_by)

---

### 2.6 document_versions (문서 버전)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 버전 고유 ID |
| `document_id` | BIGINT | FK → documents.id, NOT NULL | 문서 |
| `version` | INT | NOT NULL | 버전 번호 (1, 2, 3, ...) |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | 상태 |
| `is_auto_saved` | BOOLEAN | NOT NULL, DEFAULT FALSE | 임시저장 여부 |
| `change_summary` | VARCHAR(500) | | 변경 내용 요약 |
| `created_by` | BIGINT | FK → users.id, ON DELETE SET NULL | 작성자 (탈퇴 시 null) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 |

**제약조건**:
- `uq_document_versions` — UNIQUE (document_id, version)

**인덱스**:
- `idx_document_versions_document_id` — (document_id)

---

### 2.7 pages (페이지)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 페이지 고유 ID |
| `document_version_id` | BIGINT | FK → document_versions.id, NOT NULL | 문서 버전 |
| `page_number` | INT | NOT NULL | 페이지 순서 번호 |
| `screen_name` | VARCHAR(10) | | 화면 이름 (최대 10자) |
| `screen_id` | VARCHAR(10) | | 화면 ID (최대 10자) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스**:
- `idx_pages_document_version_id` — (document_version_id)

---

### 2.8 wireframe_images (와이어프레임 이미지)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 이미지 고유 ID |
| `page_id` | BIGINT | FK → pages.id, NOT NULL | 소속 페이지 |
| `image_type` | VARCHAR(10) | NOT NULL | 이미지 유형 (자유 문자열) |
| `image_url` | VARCHAR(500) | NOT NULL | S3 이미지 URL |
| `original_width` | INT | | 원본 가로 크기 (px) |
| `original_height` | INT | | 원본 세로 크기 (px) |
| `display_width` | INT | NOT NULL | 표시 가로 크기 (px) |
| `display_height` | INT | NOT NULL | 표시 세로 크기 (px) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |

**인덱스**:
- `idx_wireframe_images_page_id` — (page_id)

---

### 2.9 pins (핀)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 핀 고유 ID |
| `page_id` | BIGINT | FK → pages.id, NOT NULL | 소속 페이지 |
| `pin_number` | INT | NOT NULL | 핀 번호 (1, 2, 3, ...) 자동 할당, 삭제 시 재정렬 |
| `x_coordinate` | DOUBLE PRECISION | NOT NULL | X 좌표 (이미지 내 상대 좌표) |
| `y_coordinate` | DOUBLE PRECISION | NOT NULL | Y 좌표 (이미지 내 상대 좌표) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스**:
- `idx_pins_page_id` — (page_id)

---

### 2.10 requirements (요구사항)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 요구사항 고유 ID |
| `pin_id` | BIGINT | FK → pins.id, NOT NULL | 소속 핀 |
| `original_id` | BIGINT | FK → requirements.id, ON DELETE SET NULL | 원본 요구사항 ID (버전 복사 시 원본 추적용) |
| `tab_type` | VARCHAR(20) | NOT NULL | 탭 유형 (자유 문자열, 기본값: `공통`) |
| `item_name` | VARCHAR(10) | | 항목명 (최대 10자) |
| `content` | VARCHAR(200) | | 요구사항 내용 (최대 200자) |
| `is_required` | BOOLEAN | NOT NULL, DEFAULT FALSE | 필수 여부 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스**:
- `idx_requirements_pin_id` — (pin_id)
- `idx_requirements_original_id` — (original_id)

---

### 2.11 document_changes (수정사항)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 수정사항 고유 ID |
| `document_version_id` | BIGINT | FK → document_versions.id, NOT NULL | 문서 버전 |
| `change_type` | VARCHAR(30) | NOT NULL | 변경 유형 |
| `page_number` | INT | | 대상 페이지 번호 |
| `screen_name` | VARCHAR(10) | | 대상 화면 이름 |
| `pin_number` | INT | | 대상 핀 번호 (이미지 변경 시 null) |
| `item_description` | VARCHAR(100) | | 변경된 항목 설명 |
| `before_value` | TEXT | | 변경 전 값 (JSON 문자열) |
| `after_value` | TEXT | | 변경 후 값 (JSON 문자열) |
| `modified_by` | BIGINT | FK → users.id, ON DELETE SET NULL | 수정자 (탈퇴 시 null) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |

**인덱스**:
- `idx_document_changes_document_version_id` — (document_version_id)

---

### 2.12 change_confirmations (수정사항 확인)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 확인 고유 ID |
| `document_change_id` | BIGINT | FK → document_changes.id, NOT NULL | 수정사항 |
| `confirmed_by` | BIGINT | FK → users.id, ON DELETE CASCADE | 확인한 사용자 |
| `confirmed_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 확인 일시 |

**제약조건**:
- UNIQUE (document_change_id, confirmed_by) — 중복 확인 방지

---

### 2.13 activity_logs (활동 로그)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 활동 로그 고유 ID |
| `team_project_id` | BIGINT | FK → team_projects.id, NOT NULL | 팀 프로젝트 |
| `document_id` | BIGINT | FK → documents.id | 대상 문서 |
| `action_type` | VARCHAR(20) | NOT NULL | 활동 유형 |
| `document_name` | VARCHAR(100) | | 문서 이름 (스냅샷) |
| `document_type` | VARCHAR(20) | | 문서 유형 (스냅샷) |
| `version` | INT | | 대상 버전 번호 |
| `performed_by` | BIGINT | FK → users.id, ON DELETE SET NULL | 수행자 (탈퇴 시 null) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 활동 일시 |

**인덱스**:
- `idx_activity_logs_team_project_id` — (team_project_id)

---

### 2.14 team_notifications (팀 알림)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 알림 고유 ID |
| `team_project_id` | BIGINT | FK → team_projects.id, NOT NULL | 팀 프로젝트 |
| `document_id` | BIGINT | FK → documents.id | 대상 문서 |
| `document_name` | VARCHAR(100) | NOT NULL | 문서 이름 (스냅샷) |
| `before_version` | INT | NOT NULL | 변경 전 버전 번호 |
| `after_version` | INT | NOT NULL | 변경 후 버전 번호 |
| `performed_by` | BIGINT | FK → users.id, ON DELETE SET NULL | 수행자 (탈퇴 시 null) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 알림 생성일시 |

**인덱스**:
- `idx_team_notifications_team_project_id` — (team_project_id)

---

### 2.15 team_notification_reads (알림 읽음)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 읽음 고유 ID |
| `notification_id` | BIGINT | FK → team_notifications.id, NOT NULL | 알림 |
| `user_id` | BIGINT | FK → users.id, NOT NULL | 읽은 사용자 |
| `read_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 읽은 일시 |

**제약조건**:
- UNIQUE (notification_id, user_id) — 중복 읽음 방지

---

### 2.16 translation_jobs (번역 작업)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 번역 작업 고유 ID |
| `document_version_id` | BIGINT | FK → document_versions.id, NOT NULL | 대상 문서 버전 |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 상태 |
| `total_languages` | INT | NOT NULL | 번역 대상 언어 수 |
| `completed_languages` | INT | NOT NULL, DEFAULT 0 | 완료된 언어 수 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `completed_at` | TIMESTAMP | | 완료일시 |

**인덱스**:
- `idx_translation_jobs_document_version_id` — (document_version_id)

---

### 2.17 translation_languages (번역 언어별 상태)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 고유 ID |
| `translation_job_id` | BIGINT | FK → translation_jobs.id, NOT NULL | 번역 작업 |
| `target_language` | VARCHAR(20) | NOT NULL | 대상 언어 (en, ja, zh, vi 등) |
| `target_user_id` | BIGINT | FK → users.id, ON DELETE SET NULL | 번역 대상 팀원 (탈퇴 시 null) |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 상태 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `completed_at` | TIMESTAMP | | 완료일시 |

**인덱스**:
- `idx_translation_languages_job_id` — (translation_job_id)

---

### 2.18 translated_requirements (번역된 요구사항)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 고유 ID |
| `translation_language_id` | BIGINT | FK → translation_languages.id, NOT NULL | 번역 언어 정보 |
| `requirement_id` | BIGINT | FK → requirements.id, NOT NULL | 원본 요구사항 |
| `translated_item_name` | VARCHAR(50) | | 번역된 항목명 |
| `translated_content` | VARCHAR(500) | | 번역된 요구사항 내용 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |

**인덱스**:
- `idx_translated_requirements_translation_language_id` — (translation_language_id)
- `idx_translated_requirements_requirement_id` — (requirement_id)

---

## 3. 테이블 관계 요약

| 관계 | 설명 |
|------|------|
| users 1:N refresh_tokens | 사용자는 여러 리프레시 토큰 보유 가능 |
| users 1:N team_projects | 사용자(팀장)는 여러 팀 프로젝트 생성 가능 |
| users N:M team_projects (via team_members) | 사용자는 여러 팀에 참여, 팀은 여러 사용자 보유 |
| team_projects 1:N documents | 팀 프로젝트는 여러 문서 보유 |
| team_projects 1:N activity_logs | 팀 프로젝트는 여러 활동 로그 보유 |
| team_projects 1:N team_notifications | 팀 프로젝트는 여러 알림 보유 |
| team_notifications 1:N team_notification_reads | 알림은 여러 사용자가 읽음 처리 |
| documents 1:N document_versions | 문서는 여러 버전 보유 |
| document_versions 1:N pages | 문서 버전은 여러 페이지 보유 |
| document_versions 1:N document_changes | 문서 버전은 여러 수정사항 보유 |
| document_changes 1:N change_confirmations | 수정사항은 여러 사용자가 확인 |
| pages 1:N wireframe_images | 페이지는 여러 와이어프레임 이미지 보유 |
| pages 1:N pins | 페이지는 여러 핀 보유 |
| pins 1:N requirements | 핀은 여러 요구사항 보유 (탭별) |
| document_versions 1:N translation_jobs | 문서 버전은 여러 번역 작업 보유 |
| translation_jobs 1:N translation_languages | 번역 작업은 여러 언어별 상태 보유 |
| translation_languages 1:N translated_requirements | 번역 언어당 여러 번역된 요구사항 |
| requirements 1:N translated_requirements | 원본 요구사항은 여러 언어로 번역 |

---

## 4. 핵심 설계 포인트

### 4.1 버전 관리 전략
- `documents` 테이블은 문서의 메타데이터만 보유 (이름, 언어, 유형)
- 실제 내용은 `document_versions` → `pages` → `pins` → `requirements` 계층으로 **버전별로 독립** 저장
- 버전 삭제 시 해당 `document_version`과 하위 데이터(pages, pins, requirements) 모두 삭제 (CASCADE)

### 4.2 핀-요구사항 매핑
- 핀 번호(`pin_number`)는 페이지 내에서 자동 증가
- 핀 삭제 시 후속 핀 번호 자동 재정렬 (예: 1,2,3 → 2번 삭제 → 1,2)
- 핀 생성 시 현재 활성 탭에 빈 요구사항 1개 자동 생성
- 하나의 핀에 여러 탭의 요구사항이 각각 존재
- 핀 번호는 탭 간 공유 — 탭을 바꿔도 같은 핀 번호의 요구사항을 표시

### 4.3 번역 구조
- 번역은 **요구사항 텍스트만** 대상 (와이어프레임 이미지, 핀 좌표는 공유)
- 팀원이 번역 문서를 볼 때: 같은 pages/pins 구조 + `translated_requirements`에서 해당 언어 데이터 조회
- OpenAI GPT API 사용, json_schema + strict 모드로 구조적 JSON 보장
- 30건 단위 청크 분할, 3회 재시도 + 지수 백오프

### 4.4 수정사항 추적
- 문서 저장 시 이전 내용과 비교하여 `document_changes`에 변경 이력 자동 기록
- 와이어프레임 이미지 등록/변경/삭제 시에도 변경사항 자동 기록
- `change_confirmations`로 팀원별 확인 여부 추적

### 4.5 알림 시스템
- 문서 저장/버전 생성 시 `team_notifications`에 알림 생성
- `team_notification_reads`로 사용자별 읽음 처리 (폴링 방식)
- 본인이 수행한 작업의 알림은 조회 시 제외

### 4.6 사용자 탈퇴 처리
- `created_by`, `performed_by`, `modified_by` 등 사용자 참조 FK는 `ON DELETE SET NULL`
- 팀 멤버십, 리프레시 토큰, 수정사항 확인 이력은 `ON DELETE CASCADE`
- 생성한 문서/프로젝트/활동 로그는 유지, 작성자 정보만 null 처리

---

## 5. 문자열 상태값 정리

> 모든 상태값은 Java Enum이 아닌 **String 자유 문자열**로 저장됩니다.

| 필드 | 사용 값 | 설명 |
|------|---------|------|
| **team_members.role** | `LEADER`, `MEMBER` | 팀장, 팀원 |
| **documents.document_type** | `STORYBOARD` | 스토리보드 (현재 유일) |
| **document_versions.status** | `DRAFT`, `EDITING`, `IN_PROGRESS`, `TRANSLATED` | 문서 버전 상태 |
| **wireframe_images.image_type** | 자유 문자열 | 프론트에서 전달한 값 그대로 저장 |
| **requirements.tab_type** | 자유 문자열 (기본값: `공통`) | 프론트에서 전달한 탭 이름 그대로 저장 |
| **activity_logs.action_type** | `UPLOADED`, `UPDATED` | 생성/업로드, 수정 |
| **document_changes.change_type** | `REQUIREMENT_ADDED`, `REQUIREMENT_MODIFIED`, `REQUIREMENT_DELETED`, `SCREEN_MODIFIED`, `IMAGE_ADDED`, `IMAGE_MODIFIED`, `IMAGE_DELETED` | 수정사항 유형 |
| **translation_jobs.status** | `PENDING`, `TRANSLATING`, `COMPLETED`, `FAILED` | 번역 작업 상태 |
| **translation_languages.status** | `PENDING`, `TRANSLATING`, `COMPLETED`, `FAILED` | 언어별 번역 상태 |

---

## 6. 마이그레이션 이력

| 버전 | 설명 |
|------|------|
| V1 | 초기 스키마 (users, refresh_tokens, team_projects, team_members, documents, document_versions, pages, wireframe_images, pins, requirements) |
| V2 | 번역 테이블 (translation_jobs, translation_languages, translated_requirements) + requirements.original_id |
| V3 | activity_logs 테이블 |
| V4 | document_changes, change_confirmations 테이블 |
| V5 | users에 bio/profile_image_url 추가, 사용자 탈퇴 지원 (FK ON DELETE SET NULL/CASCADE) |
| V6 | requirements에 is_required 추가 |
| V7 | team_notifications, team_notification_reads 테이블 |
