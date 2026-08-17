# Phase 4: 팀 프로젝트

> **완료일**: 2026-08-12

---

## 산출물

### 1. API

| API | 메서드 | 설명 |
|-----|--------|------|
| `/api/teams` | POST | 팀 생성 (201) — 초대 코드 자동 발급, LEADER 등록 |
| `/api/teams/join` | POST | 초대 코드로 참여 (200) — MEMBER 등록 |
| `/api/teams/{teamId}` | GET | 팀 상세 조회 — 멤버/문서/언어 목록 |
| `/api/teams/{teamId}/invite-code` | GET | 초대 코드 재조회 |

### 2. 초대 코드 생성 로직

- 영대문자 + 숫자 6자리 랜덤 (`SecureRandom`)
- DB에서 중복 확인 후 유니크한 코드 생성
- 예: `FE4JT1`, `A3K9F2`

### 3. DTO

**Request:**
- `CreateTeamRequest` — name, defaultLanguage
- `JoinTeamRequest` — inviteCode

**Response:**
- `CreateTeamResponse` — id, name, defaultLanguage, inviteCode, createdBy, createdAt
- `JoinTeamResponse` — teamProjectId, name, role, joinedAt
- `TeamDetailResponse` — 팀 정보 + myRole + members + documents(documentType 포함) + memberLanguages + activityLogs
- `InviteCodeResponse` — inviteCode

### 4. 권한 검증

- 팀 상세 조회: 팀 멤버만 접근 가능 (비멤버 → 403)
- 초대 코드 조회: 팀 멤버만 접근 가능

---

## 검증 결과

- [x] 팀 생성 → 201 + 초대 코드 반환
- [x] 팀 참여 → 200 + MEMBER 등록
- [x] 잘못된 초대 코드 → 404
- [x] 이미 참여한 팀 재참여 → 409
- [x] 팀 상세 조회 → 멤버 2명 확인
- [x] 비멤버 팀 조회 → 403
- [x] 대시보드 재검증 → 생성한 팀 프로젝트 표시 확인

---

## 추가 구현 (2026-08-17)

### 활동 요약 (Activity Log)

- `activity_logs` 테이블 추가 (Flyway V3)
- `ActivityLog` 엔티티 — teamProject, document, actionType(UPLOADED/UPDATED), documentName, documentType, version, performedBy
- 문서 생성/저장/새 버전 생성 시 자동 기록
- `GET /api/teams/{teamId}` 응답에 `activityLogs` (최근 10개) 포함
- `documents[]`에 `documentType` 필드 추가
