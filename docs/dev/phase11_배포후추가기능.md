# Phase 11: 배포 후 추가 기능 개발

> **완료일**: 2026-08-17

---

## 1. 프론트엔드 API 연동 가이드 작성

프론트 팀이 즉시 연동 개발을 시작할 수 있도록 화면 단위 API 연동 가이드를 작성했다.

### 산출물

- `docs/프론트엔드_API_연동_가이드.md`

### 구성

| 섹션 | 내용 |
|------|------|
| 공통 사항 | 응답 형식, 인증 헤더, HTTP 상태 코드 참고표, JS 에러 처리 예시, 토큰 만료 처리 흐름 |
| 화면 1~9 | 화면별 호출 API, 요청/응답 JSON 예시, 호출 순서, 에러 테이블 |
| 부록 | API 전체 목록 (44개 엔드포인트) |

### 에러 메시지 검증

- 문서에 적힌 모든 에러 메시지를 실제 코드와 대조 검증
- 8개 불일치 발견 → 전부 실제 코드 메시지로 수정 완료

---

## 2. 팀 상세 - 활동 요약 (Activity Log)

팀 프로젝트 메인 화면의 "활동 요약" 영역을 지원하기 위해 활동 로그 기능을 추가했다.

### 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `V3__create_activity_logs.sql` | `activity_logs` 테이블 생성 (Flyway 마이그레이션) |
| `ActivityLog.java` | 엔티티 — teamProject, document, actionType, documentName, documentType, version, performedBy |
| `ActivityLogRepository.java` | 최근 10개 조회 (`@EntityGraph`로 N+1 방지) |
| `TeamDetailResponse.java` | `ActivityLogInfo` 레코드 + `activityLogs` 필드 추가 |
| `TeamService.java` | `getTeamDetail()`에서 활동 로그 조회 및 응답 매핑 |
| `DocumentService.java` | `createDocument()`, `saveDocument()`, `createNewVersion()`에서 활동 로그 기록 |

### 동작

- 문서 생성 / 새 버전 생성 → `UPLOADED` 기록
- 문서 저장 → `UPDATED` 기록
- `GET /api/teams/{teamId}` 응답에 `activityLogs` (최근 10개, 최신순) 포함
- 각 로그에 수행자 이름, 이니셜, 시각 포함 → 프론트에서 상대 시간 계산

### 검증

- [x] 문서 생성 → UPLOADED 기록 확인
- [x] 문서 저장 → UPDATED 기록 확인
- [x] 새 버전 생성 → UPLOADED 기록 확인
- [x] 최신순 정렬 정상
- [x] 수행자 이니셜 정상

---

## 3. 팀 상세 - documentType 응답 추가

`GET /api/teams/{teamId}` 응답의 `documents[]`에 `documentType` 필드가 누락되어 있어 추가했다.

### 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `TeamDetailResponse.java` | `TeamDocumentInfo`에 `documentType` 필드 추가 |
| `TeamService.java` | `doc.getDocumentType()` 매핑 추가 |

---

## 4. 수정사항 추적 및 확인 (Change Tracking & Review Confirmation)

와이어프레임의 "수정사항 요약" 영역과 팀원의 "수정사항 확인" 기능을 지원하기 위해 변경사항 추적 시스템을 구축했다.

### 기능 설명

- **변경 감지**: 문서 저장(`PUT .../versions/{ver}`) 시 이전 데이터와 비교하여 변경사항 자동 기록
- **변경 유형**: `REQUIREMENT_ADDED`, `REQUIREMENT_MODIFIED`, `REQUIREMENT_DELETED`, `SCREEN_MODIFIED`
- **확인 처리**: 팀원이 개별/전체 수정사항을 "확인" 처리 가능 (사용자별 독립)
- **before/after**: 변경 전/후 데이터를 JSON으로 저장하여 프론트에서 diff 표시 가능

### 새 API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/documents/{id}/versions/{ver}/changes` | 수정사항 목록 + 확인 상태 조회 |
| POST | `/api/documents/{id}/versions/{ver}/changes/{changeId}/confirm` | 개별 수정사항 확인 |
| POST | `/api/documents/{id}/versions/{ver}/changes/confirm-all` | 전체 수정사항 확인 |

### 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `V4__create_change_tracking_tables.sql` | `document_changes`, `change_confirmations` 테이블 생성 |
| `DocumentChange.java` | 수정사항 엔티티 |
| `ChangeConfirmation.java` | 확인 이력 엔티티 (사용자별 unique 제약) |
| `DocumentChangeRepository.java` | 수정사항 조회/삭제 |
| `ChangeConfirmationRepository.java` | 확인 이력 조회 |
| `ChangeTrackingService.java` | 핵심 — diff 알고리즘 + 확인 처리 로직 |
| `ChangeSummaryResponse.java` | 응답 DTO (totalChanges, confirmedByMe, changes) |
| `DocumentService.java` | `saveDocument()`에서 `changeTrackingService.detectAndRecordChanges()` 호출 |
| `DocumentController.java` | 3개 엔드포인트 추가 |

### Diff 알고리즘

1. 기존 저장 데이터와 새 요청 데이터를 `pageNumber` → `pinNumber` → 요구사항 순서로 매칭
2. 화면명/화면ID 변경 → `SCREEN_MODIFIED`
3. 요구사항 비교: tabType, itemName, content 중 하나라도 다르면 `REQUIREMENT_MODIFIED`
4. 기존에 없는 요구사항 → `REQUIREMENT_ADDED`, 새 데이터에 없는 요구사항 → `REQUIREMENT_DELETED`
5. 재저장 시 이전 변경사항을 삭제하고 새로 계산
6. 최초 저장 (기존 데이터 없음) 시에는 변경사항을 기록하지 않음

### 검증

- [x] 요구사항 수정 → `REQUIREMENT_MODIFIED` 감지
- [x] 요구사항 추가 → `REQUIREMENT_ADDED` 감지
- [x] 화면명 변경 → `SCREEN_MODIFIED` 감지
- [x] 수정사항 목록 조회 정상 (totalChanges, confirmedByMe, unconfirmedByMe)
- [x] 개별 확인 → confirmed 1, unconfirmed 1
- [x] 전체 확인 → confirmed 2, unconfirmed 0
- [x] 중복 확인 → 409 에러

---

## 5. 대시보드 API 가이드 보정

- `/api/dashboard` 응답에 `user` (UserSummary: id, firstName, lastName, initial)가 포함되어 있음을 확인
- 가이드에서 대시보드 진입 시 호출을 `GET /api/dashboard` 하나로 정리
- `/api/users/me`는 설정 화면 등 상세 정보 (이메일, 국적, 언어) 필요 시 호출하도록 안내

---

## DB 마이그레이션 이력

| 버전 | 파일명 | 내용 |
|------|--------|------|
| V1 | `V1__init.sql` | 초기 스키마 |
| V2 | `V2__add_original_id_to_requirements.sql` | 요구사항 originalId 추가 |
| V3 | `V3__create_activity_logs.sql` | 활동 로그 테이블 |
| V4 | `V4__create_change_tracking_tables.sql` | 수정사항 추적/확인 테이블 |

---

## 배포 확인

- EC2 서버 (`3.35.208.88:8080`)에서 전체 기능 테스트 완료
- Flyway V3, V4 마이그레이션 자동 적용 확인
- 44개 엔드포인트 전체 정상 동작
