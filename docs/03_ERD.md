# 잇다(ITDA) ERD 설계

> **버전**: v1.0  
> **작성일**: 2026-08-12  
> **DB**: PostgreSQL 17+  
> **기반 문서**: 01_유저플로우.md, 02_구현기술계획.md

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
  │                              │        │        └──< translation_jobs (번역 작업)
  │                              │        │                 │
  │                              │        │                 └──< translation_languages (번역 언어별 상태)
  │                              │        │
  │                              │        └──< translated_requirements (번역된 요구사항)
  │                              │
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

---

### 2.3 team_projects (팀 프로젝트)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 팀 프로젝트 고유 ID |
| `name` | VARCHAR(100) | NOT NULL | 프로젝트 이름 |
| `default_language` | VARCHAR(20) | NOT NULL | 기본 언어 (현재 'ko'만) |
| `invite_code` | VARCHAR(6) | UNIQUE, NOT NULL | 초대 코드 (영대문자+숫자 6자리) |
| `created_by` | BIGINT | FK → users.id, NOT NULL | 생성자 (팀장) |
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

---

### 2.5 documents (문서)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 문서 고유 ID |
| `team_project_id` | BIGINT | FK → team_projects.id, NOT NULL | 소속 팀 프로젝트 |
| `name` | VARCHAR(10) | NOT NULL | 문서 이름 (최대 10자) |
| `language` | VARCHAR(20) | NOT NULL | 작성 언어 |
| `document_type` | VARCHAR(20) | NOT NULL, DEFAULT 'STORYBOARD' | 문서 유형 |
| `created_by` | BIGINT | FK → users.id, NOT NULL | 생성자 (팀장) |
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
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | 상태: `DRAFT` / `SAVED` / `COMPLETED` / `TRANSLATING` / `TRANSLATED` / `EDITING` |
| `is_auto_saved` | BOOLEAN | NOT NULL, DEFAULT FALSE | 임시저장 여부 |
| `change_summary` | VARCHAR(500) | | 변경 내용 요약 |
| `created_by` | BIGINT | FK → users.id, NOT NULL | 작성자/수정자 |
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
| `image_type` | VARCHAR(10) | NOT NULL | 타입: `DESKTOP` / `MOBILE` |
| `image_url` | VARCHAR(500) | NOT NULL | S3 이미지 URL |
| `original_width` | INT | | 원본 가로 크기 (px) |
| `original_height` | INT | | 원본 세로 크기 (px) |
| `display_width` | INT | NOT NULL | 표시 가로 크기 (데스크톱: 660, 모바일: 214) |
| `display_height` | INT | NOT NULL | 표시 세로 크기 (비율 계산) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |

**인덱스**:
- `idx_wireframe_images_page_id` — (page_id)

---

### 2.9 pins (핀)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 핀 고유 ID |
| `page_id` | BIGINT | FK → pages.id, NOT NULL | 소속 페이지 |
| `pin_number` | INT | NOT NULL | 핀 번호 (1, 2, 3, ...) 자동 할당 |
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
| `tab_type` | VARCHAR(20) | NOT NULL | 탭: `COMMON` / `PLANNING` / `FRONTEND` / `BACKEND` / `DESIGN` |
| `item_name` | VARCHAR(10) | | 항목명 (최대 10자) |
| `content` | VARCHAR(200) | | 요구사항 내용 (최대 200자) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스**:
- `idx_requirements_pin_id` — (pin_id)
- `idx_requirements_original_id` — (original_id)

---

### 2.11 translation_jobs (번역 작업)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 번역 작업 고유 ID |
| `document_version_id` | BIGINT | FK → document_versions.id, NOT NULL | 대상 문서 버전 |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 상태: `PENDING` / `IN_PROGRESS` / `COMPLETED` / `FAILED` |
| `total_languages` | INT | NOT NULL | 번역 대상 언어 수 |
| `completed_languages` | INT | NOT NULL, DEFAULT 0 | 완료된 언어 수 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `completed_at` | TIMESTAMP | | 완료일시 |

**인덱스**:
- `idx_translation_jobs_document_version_id` — (document_version_id)

---

### 2.12 translation_languages (번역 언어별 상태)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| `id` | BIGSERIAL | PK | 고유 ID |
| `translation_job_id` | BIGINT | FK → translation_jobs.id, NOT NULL | 번역 작업 |
| `target_language` | VARCHAR(20) | NOT NULL | 대상 언어 (en, ja, zh, vi 등) |
| `target_user_id` | BIGINT | FK → users.id | 번역 대상 팀원 |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 상태: `PENDING` / `TRANSLATING` / `COMPLETED` / `FAILED` |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성일시 |
| `completed_at` | TIMESTAMP | | 완료일시 |

**인덱스**:
- `idx_translation_languages_job_id` — (translation_job_id)

---

### 2.13 translated_requirements (번역된 요구사항)

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
| documents 1:N document_versions | 문서는 여러 버전 보유 |
| document_versions 1:N pages | 문서 버전은 여러 페이지 보유 |
| pages 1:N wireframe_images | 페이지는 여러 와이어프레임 이미지 보유 (데스크톱/모바일) |
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
- 삭제 후 이전 버전이 최신 버전으로 자동 승격

### 4.2 핀-요구사항 매핑
- 핀 번호(`pin_number`)는 페이지 내에서 자동 증가
- 하나의 핀에 **5개 탭**(공통/기획/프론트/백엔드/디자인)의 요구사항이 각각 존재
- 핀 번호는 탭 간 공유 — 탭을 바꿔도 같은 핀 번호의 요구사항을 표시

### 4.3 번역 구조
- 번역은 **요구사항 텍스트만** 대상 (와이어프레임 이미지, 핀 좌표는 공유)
- 팀원이 번역 문서를 볼 때: 같은 pages/pins 구조 + `translated_requirements`에서 해당 언어 데이터 조회
- 번역 캐싱: `requirement_id` + `target_language` 조합으로 중복 번역 방지

### 4.4 임시저장
- `document_versions.is_auto_saved = true`로 임시저장 상태 구분
- 최종 저장 시 `is_auto_saved = false`, `status = COMPLETED`로 변경

### 4.5 와이어프레임 이미지
- 별도 `wireframe_images` 테이블로 분리 — 페이지당 데스크톱/모바일 각각 저장 가능
- 이미지 파일은 **S3**에 저장, DB에는 URL만 저장
- 리사이즈 정보(원본/표시 크기)도 함께 저장하여 프론트에서 활용

---

## 5. ENUM 값 정리

| ENUM | 값 | 설명 |
|------|-----|------|
| **team_members.role** | `LEADER`, `MEMBER` | 팀장, 팀원 |
| **documents.document_type** | `STORYBOARD` | 스토리보드 (현재 유일, 확장 가능) |
| **document_versions.status** | `DRAFT`, `SAVED`, `COMPLETED`, `TRANSLATING`, `TRANSLATED`, `EDITING` | 문서 버전 상태 |
| **wireframe_images.image_type** | `DESKTOP`, `MOBILE` | 와이어프레임 유형 |
| **requirements.tab_type** | `COMMON`, `PLANNING`, `FRONTEND`, `BACKEND`, `DESIGN` | 요구사항 탭 |
| **translation_jobs.status** | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED` | 번역 작업 상태 |
| **translation_languages.status** | `PENDING`, `TRANSLATING`, `COMPLETED`, `FAILED` | 언어별 번역 상태 |
