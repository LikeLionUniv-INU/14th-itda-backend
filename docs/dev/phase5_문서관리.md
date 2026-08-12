# Phase 5: 문서 관리

> **완료일**: 2026-08-12

---

## 산출물

### 1. Flyway 마이그레이션

**V2__add_original_id_to_requirements.sql**
- `requirements` 테이블에 `original_id` 컬럼 추가
- 버전 복사 시 원본 요구사항 ID를 추적하여 변경 전/후 비교 가능
- `ON DELETE SET NULL` — 원본 삭제 시 참조만 해제

### 2. 엔티티

| 엔티티 | 패키지 | 설명 |
|--------|--------|------|
| `Page` | `domain.page.entity` | 페이지 (documentVersion, pageNumber, screenName, screenId) |
| `Pin` | `domain.pin.entity` | 핀 (page, pinNumber, xCoordinate, yCoordinate) |
| `Requirement` | `domain.requirement.entity` | 요구사항 (pin, originalId, tabType, itemName, content) |

### 3. API

| API | 메서드 | 설명 |
|-----|--------|------|
| `/api/teams/{teamId}/documents` | POST | 문서 생성 (201) — 팀장만 가능 |
| `/api/documents/{documentId}/versions/{version}` | GET | 문서 상세 조회 — 페이지/핀/요구사항 포함 |
| `/api/documents/{documentId}/versions` | GET | 버전 목록 조회 |
| `/api/documents/{documentId}/versions/{version}` | PUT | 문서 전체 저장 — status=COMPLETED |
| `/api/documents/{documentId}/versions/{version}/auto-save` | POST | 임시저장 — status=DRAFT, isAutoSaved=true |
| `/api/documents/{documentId}/versions` | POST | 새 버전 생성 (201) — 기준 버전 데이터 복사 |
| `/api/documents/{documentId}/versions/{version}` | DELETE | 버전 삭제 — 마지막 버전 삭제 불가 (400) |

### 4. DTO

**Request:**
- `CreateDocumentRequest` — name(@Size(max=10)), language, version(@Min(1)), documentType
- `SaveDocumentRequest` — status, changeSummary, pages[](pageNumber, screenName, screenId, pins[](pinNumber, xCoordinate, yCoordinate, requirements[](tabType, itemName, content)))
- `CreateVersionRequest` — baseVersion(@Min(1))

**Response:**
- `CreateDocumentResponse` — documentId, name, language, documentType, version, status, createdAt
- `DocumentDetailResponse` — 문서 메타 + pages[] > pins[] > requirements[] 계층 구조
- `DocumentVersionResponse` — id, version, status, isAutoSaved, changeSummary, createdBy, createdAt, updatedAt

### 5. 권한 검증

- 문서 생성/저장/임시저장/버전 생성/버전 삭제: **팀장(LEADER)만** 가능 (비팀장 → 403)
- 문서 조회/버전 목록 조회: **팀 멤버** 접근 가능 (비멤버 → 403)

### 6. 버전 복사 로직 (createNewVersion)

- 기준 버전의 pages → pins → requirements를 **깊은 복사**
- 복사된 requirements의 `original_id`에 원본 requirement의 `id`를 설정
- 이를 통해 버전 간 변경 전/후 비교 가능:
  ```
  ver.1: 요구사항 "이메일 형식 검증" (id=1)
  ver.2 복사: 요구사항 "이메일 형식 검증" (id=4, original_id=1)
  ver.2 수정: 요구사항 "이메일 형식 + 중복 검증" (id=4, original_id=1) ← 변경됨
  ```
  → original_id=1인 ver.1 내용과 ver.2 내용을 비교하여 변경 전/후 표시

---

## 검증 결과

- [x] 문서 생성 (팀장) → 201 + DRAFT 상태
- [x] 문서 생성 (팀원) → 403 거부
- [x] 문서 상세 조회 → 빈 페이지 목록 확인
- [x] 문서 전체 저장 → 200 + status=COMPLETED, isAutoSaved=false
- [x] 저장 후 조회 → 페이지/핀/요구사항 데이터 일치 확인
- [x] 임시저장 → 200 + status=DRAFT, isAutoSaved=true
- [x] 새 버전 생성 → 201 + 이전 데이터 복사 확인
- [x] 버전 목록 조회 → 2개 버전 확인 (내림차순)
- [x] 버전 삭제 → 200
- [x] 마지막 버전 삭제 → 400 ("마지막 남은 버전은 삭제할 수 없습니다.")
- [x] 대시보드 재검증 → 생성한 문서 표시 확인
- [x] 팀 상세 재검증 → 문서 목록에 표시 확인
