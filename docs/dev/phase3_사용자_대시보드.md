# Phase 3: 사용자 + 대시보드

> **완료일**: 2026-08-12

---

## 산출물

### 1. 사용자 정보 조회 API

`GET /api/users/me`

- `UserResponse` — id, email, firstName, lastName, country, language, initial, createdAt
- `initial`: lastName 첫 글자 대문자 (예: "Kim" → "K")
- SecurityContext에서 userId 추출

### 2. 대시보드 API

| API | 설명 |
|-----|------|
| `GET /api/dashboard` | 홈 탭 — user + projects + recentDocuments 통합 |
| `GET /api/dashboard/projects` | 프로젝트 탭 — 내 프로젝트 목록 |
| `GET /api/dashboard/documents` | 문서 탭 — 내 문서 목록 |

### 3. 추가 생성 엔티티 (조회용)

Phase 4, 5에서 본격 사용하지만, 대시보드 조회를 위해 미리 생성:

- `TeamProject` 엔티티 + Repository
- `TeamMember` 엔티티 + Repository
- `Document` 엔티티 + Repository
- `DocumentVersion` 엔티티 + Repository

### 4. DTO

**User:**
- `UserResponse` — 전체 프로필 (me API용)
- `UserSummary` — id, firstName, lastName, initial (대시보드 배너용)
- `MemberSummary` — firstName, lastName, initial (멤버 목록용)

**Dashboard:**
- `DashboardResponse` — user + projects + recentDocuments
- `DashboardProjectResponse` — 프로젝트 정보 + memberLanguages + members + counts
- `DashboardDocumentResponse` — 문서 정보 + teamProjectName + latestVersion
- `ProjectListResponse`, `DocumentListResponse` — 탭별 래퍼

---

## 검증 결과

- [x] `/api/users/me` → 200 정상 (initial "K")
- [x] `/api/dashboard` → 200 빈 데이터 정상
- [x] `/api/dashboard/projects` → 200 빈 배열 정상
- [x] `/api/dashboard/documents` → 200 빈 배열 정상
- [x] 토큰 없이 접근 → 401 정상
- [ ] Phase 4, 5 완료 후 실제 데이터로 재검증 예정
