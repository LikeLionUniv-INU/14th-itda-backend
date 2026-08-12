# Phase 6: 페이지 / 핀 / 요구사항

> **완료일**: 2026-08-12

---

## 산출물

### 1. API

#### 페이지 API

| API | 메서드 | 설명 |
|-----|--------|------|
| `/api/documents/{documentId}/versions/{version}/pages` | POST | 페이지 추가 (201) — 자동 번호 할당 |
| `/api/documents/{documentId}/versions/{version}/pages/{pageId}` | PUT | 페이지 수정 (screenName, screenId) |
| `/api/documents/{documentId}/versions/{version}/pages/{pageId}` | DELETE | 페이지 삭제 (CASCADE: 핀/요구사항) + 번호 재정렬 |
| `/api/documents/{documentId}/versions/{version}/pages/reorder` | PATCH | 페이지 순서 변경 |

#### 핀 API

| API | 메서드 | 설명 |
|-----|--------|------|
| `/api/pages/{pageId}/pins` | POST | 핀 추가 (201) — 자동 번호 할당 |
| `/api/pages/{pageId}/pins/{pinId}` | PUT | 핀 위치 수정 (x, y 좌표) |
| `/api/pages/{pageId}/pins/{pinId}` | DELETE | 핀 삭제 (CASCADE: 요구사항) |
| `/api/pages/{pageId}/pins` | GET | 핀 전체 조회 (요구사항 포함, tabType 필터) |

#### 요구사항 API

| API | 메서드 | 설명 |
|-----|--------|------|
| `/api/pins/{pinId}/requirements` | POST | 요구사항 추가 (201) |
| `/api/pins/{pinId}/requirements/{requirementId}` | PUT | 요구사항 수정 — 빈 content 시 400 |
| `/api/pins/{pinId}/requirements/{requirementId}` | DELETE | 요구사항 삭제 |

### 2. DTO

**Page:**
- `CreatePageRequest` — screenName(@Size(max=10)), screenId(@Size(max=10))
- `UpdatePageRequest` — screenName, screenId
- `ReorderPagesRequest` — pageIds(@NotEmpty)
- `PageResponse` — id, pageNumber, screenName, screenId, createdAt, updatedAt

**Pin:**
- `CreatePinRequest` — xCoordinate(@NotNull), yCoordinate(@NotNull)
- `UpdatePinRequest` — xCoordinate, yCoordinate
- `PinResponse` — id, pinNumber, xCoordinate, yCoordinate, createdAt, updatedAt
- `PinDetailResponse` — 핀 정보 + requirements[]

**Requirement:**
- `CreateRequirementRequest` — tabType(@NotBlank), itemName(@Size(max=10)), content(@Size(max=200))
- `UpdateRequirementRequest` — itemName, content (빈 content → 400)
- `RequirementResponse` — id, tabType, itemName, content, createdAt, updatedAt

### 3. 권한 검증

- 모든 CUD 작업: **팀장(LEADER)만** 가능 (비팀장 → 403)
- 핀 조회(GET): **팀 멤버** 접근 가능

### 4. 핵심 동작

- **자동 번호 할당**: 페이지/핀 추가 시 기존 개수 + 1로 자동 번호 부여
- **페이지 삭제**: 하위 핀/요구사항 CASCADE 삭제 + 남은 페이지 번호 재정렬
- **핀 삭제**: 하위 요구사항 CASCADE 삭제
- **tabType 필터**: `GET /api/pages/{pageId}/pins?tabType=FRONTEND` 으로 특정 탭 요구사항만 조회

---

## 검증 결과

- [x] 페이지 추가 → 201 + 자동 번호 (1, 2)
- [x] 페이지 수정 → screenName/screenId 변경 확인
- [x] 핀 추가 → 201 + 자동 번호 (1, 2)
- [x] 핀 위치 수정 → 좌표 변경 확인
- [x] 요구사항 추가 → 5개 탭(COMMON/PLANNING/FRONTEND/BACKEND/DESIGN) 각각 추가
- [x] 핀 전체 조회 → 핀 2개 + 요구사항 5개 확인
- [x] tabType 필터 → FRONTEND만 1개 반환
- [x] 요구사항 수정 → 내용 변경 확인
- [x] 빈 content 수정 → 400 ("수정내용을 입력해주세요.")
- [x] 요구사항 삭제 → 200 (5→4개)
- [x] 핀 삭제 → 200 (CASCADE 확인)
- [x] 페이지 순서 변경 → 200
- [x] 페이지 삭제 → 200 (CASCADE + 번호 재정렬)
- [x] 팀원 CUD 시도 → 403 ("팀장만 수행할 수 있는 작업입니다.")
- [x] 팀원 조회 → 200 (읽기 허용)
- [x] 문서 조회 재검증 → 남은 페이지/핀/요구사항 정확히 반영
