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

## 6. 사용자 설정 기능 (User Settings)

와이어프레임 설정 화면에 표시된 모든 기능을 구현했다.

### 새 API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| PUT | `/api/users/me` | 프로필 수정 (이름, 국적, 언어, bio) |
| POST | `/api/users/me/profile-image/presigned-url` | 프로필 이미지 업로드 URL 발급 |
| PUT | `/api/users/me/profile-image` | 프로필 이미지 저장 |
| DELETE | `/api/users/me/profile-image` | 프로필 이미지 삭제 |
| PUT | `/api/users/me/password` | 비밀번호 변경 |
| PUT | `/api/users/me/email` | 이메일 변경 |
| DELETE | `/api/users/me` | 회원 탈퇴 |

### 변경/생성 파일

| 파일 | 변경 내용 |
|------|----------|
| `V5__user_settings.sql` | users 테이블에 bio, profile_image_url 추가 + 계정 삭제를 위한 FK ON DELETE 정책 변경 |
| `User.java` | bio, profileImageUrl 필드 + updateProfile/Password/Email/ProfileImageUrl 메서드 |
| `UserResponse.java` | bio, profileImageUrl 필드 추가 |
| `UpdateProfileRequest.java` | 프로필 수정 요청 DTO |
| `ChangePasswordRequest.java` | 비밀번호 변경 요청 DTO |
| `ChangeEmailRequest.java` | 이메일 변경 요청 DTO |
| `DeleteAccountRequest.java` | 회원 탈퇴 요청 DTO |
| `ProfileImagePresignedUrlRequest.java` | 이미지 업로드 URL 요청 DTO |
| `UpdateProfileImageRequest.java` | 이미지 URL 저장 요청 DTO |
| `UserService.java` | 7개 신규 메서드 (프로필 수정, 이미지 관리, 비밀번호/이메일 변경, 회원 탈퇴) |
| `UserController.java` | 7개 엔드포인트 추가 |

### 계정 삭제 FK 처리 (V5 마이그레이션)

| 테이블.컬럼 | ON DELETE 정책 | 결과 |
|-------------|---------------|------|
| `refresh_tokens.user_id` | CASCADE (기존) | 토큰 삭제 |
| `team_members.user_id` | CASCADE (기존) | 멤버십 삭제 |
| `change_confirmations.confirmed_by` | CASCADE (변경) | 확인 이력 삭제 |
| `team_projects.created_by` | SET NULL (변경) | 프로젝트 유지, 작성자 null |
| `documents.created_by` | SET NULL (변경) | 문서 유지, 작성자 null |
| `document_versions.created_by` | SET NULL (변경) | 버전 유지, 작성자 null |
| `activity_logs.performed_by` | SET NULL (변경) | 로그 유지, 수행자 null |
| `document_changes.modified_by` | SET NULL (변경) | 변경사항 유지, 수정자 null |
| `translation_languages.target_user_id` | SET NULL (변경) | 번역 유지, 대상자 null |

### 핵심 로직

- **비밀번호 변경**: 현재 비밀번호 BCrypt 검증 → 동일 비밀번호 차단 → 새 비밀번호 인코딩
- **이메일 변경**: 비밀번호 확인 → 중복 체크 → 변경 → 리프레시 토큰 전체 삭제 (재로그인 강제)
- **프로필 이미지**: 기존 S3 Presigned URL 패턴 재사용, 이미지 교체 시 기존 S3 파일 자동 삭제
- **회원 탈퇴**: 비밀번호 확인 → S3 이미지 삭제 → DB CASCADE/SET NULL로 연관 데이터 처리

### 검증

- [x] 프로필 수정 (이름, 국적, 언어, bio) → 변경 반영 확인
- [x] 비밀번호 변경 → 새 비밀번호로 로그인 성공
- [x] 비밀번호 변경 → 틀린 현재 비밀번호 시 401
- [x] 이메일 변경 → 새 이메일로 로그인 성공
- [x] 이메일 중복 변경 시도 → "현재 이메일과 동일합니다" 400
- [x] 프로필 이미지 삭제 (이미지 없을 때) → 정상 처리
- [x] 회원 탈퇴 → 계정 삭제 후 로그인 불가 확인

---

## 7. 버전 복사 시 와이어프레임 이미지 누락 수정

새 버전 생성(`createNewVersion`) 시 `copyVersionContent()`에서 페이지/핀/요구사항만 복사하고 와이어프레임 이미지를 복사하지 않던 버그를 수정했다.

### 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `DocumentService.java` | `copyVersionContent()`에 와이어프레임 이미지 복사 로직 추가 |

### 동작

- 새 버전 생성 시 기준 버전의 와이어프레임 이미지 DB 레코드를 새 페이지에 복사
- S3의 실제 이미지 파일은 복사하지 않고 동일 URL을 참조 (저장 공간 효율)

---

## 8. 프론트엔드 API 가이드 업데이트

- `GET /api/users/me` 응답에 `bio`, `profileImageUrl` 필드 추가 반영
- "화면 10. 설정" 섹션 추가 (프로필 수정, 이미지 관리, 비밀번호/이메일 변경, 회원 탈퇴)
- 부록 API 전체 목록에 7개 사용자 설정 엔드포인트 추가

---

## 9. 문서작성 기능 보완

와이어프레임 요구사항 문서와 백엔드 코드를 대조하여 미구현 3가지 기능을 추가했다.

### 9-1. 핀 삭제 시 자동 번호 재정렬

핀 삭제 후 남은 핀들의 번호가 연속되지 않는 문제를 수정했다.

| 파일 | 변경 내용 |
|------|----------|
| `Pin.java` | `updatePinNumber()` 메서드 추가 |
| `PinService.java` | `deletePin()`에서 삭제 후 남은 핀 번호 재정렬 로직 추가 |

**동작**: 핀 1,2,3,4에서 2번 삭제 → flush 후 남은 핀 3,4의 번호를 2,3으로 재정렬

### 9-2. 요구사항 "필수 여부" 필드 추가

와이어프레임에 표시된 "필수 여부" 컬럼을 지원하기 위해 `isRequired` 필드를 추가했다.

| 파일 | 변경 내용 |
|------|----------|
| `V6__add_requirement_is_required.sql` | `requirements` 테이블에 `is_required BOOLEAN DEFAULT false` 추가 |
| `Requirement.java` | `isRequired` 필드 + Builder/update 반영 |
| `CreateRequirementRequest.java` | `isRequired` 파라미터 추가 |
| `UpdateRequirementRequest.java` | `isRequired` 파라미터 추가 |
| `RequirementResponse.java` | `isRequired` 필드 추가 |
| `PinDetailResponse.java` | `RequirementInfo`에 `isRequired` 추가 |
| `PinService.java` | `getPins()` 매핑 수정 |
| `RequirementService.java` | create/update 시 `isRequired` 매핑 |
| `SaveDocumentRequest.java` | `RequirementData`에 `isRequired` 추가 |
| `DocumentDetailResponse.java` | `RequirementInfo`에 `isRequired` 추가 |
| `DocumentService.java` | `saveVersionContent()`, `copyVersionContent()`, `buildRequirementInfo()` 매핑 |

### 9-3. 핀 추가 시 요구사항 자동 생성

핀 생성 시 프론트에서 전달한 현재 활성 탭에 빈 요구사항 1개가 자동 생성되도록 했다.

| 파일 | 변경 내용 |
|------|----------|
| `CreatePinRequest.java` | `tabType` 파라미터 추가 (선택, 기본값 "공통") |
| `PinService.java` | `createPin()`에서 빈 요구사항 자동 생성 |

**동작**: `POST /api/pages/{pageId}/pins` 호출 시 `tabType` 전달 → 해당 탭에 빈 요구사항 생성. 미전달 시 "공통" 탭에 생성.

### 검증

- [x] 핀 3개 생성 → 2번 삭제 → 핀 목록 [1, 2] 정상 재정렬
- [x] 핀 생성 (tabType="기획") → 기획 탭에 빈 요구사항 자동 생성
- [x] 핀 생성 (tabType 미전달) → 공통 탭에 빈 요구사항 자동 생성
- [x] 요구사항 생성 isRequired=true → 조회 시 반영 확인
- [x] 문서 전체 저장 시 isRequired 포함 저장/조회 정상

---

## 10. 와이어프레임 이미지 변경 추적

수정문서확인 화면의 before/after 이미지 비교 기능을 지원하기 위해, 와이어프레임 이미지 등록/변경/삭제 시 `DocumentChange`를 자동 기록하도록 했다.

### 새 changeType

| changeType | 설명 | beforeValue | afterValue |
|------------|------|-------------|------------|
| `IMAGE_ADDED` | 이미지 등록 | null | `{"imageUrl":"..."}` |
| `IMAGE_MODIFIED` | 이미지 변경 | `{"imageUrl":"기존URL"}` | `{"imageUrl":"새URL"}` |
| `IMAGE_DELETED` | 이미지 삭제 | `{"imageUrl":"..."}` | null |

### 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `WireframeImageService.java` | `DocumentChangeRepository`, `UserRepository` 의존성 추가. `createWireframeImage()`, `updateWireframeImage()`, `deleteWireframeImage()`에서 `recordImageChange()` 호출. 공통 헬퍼 메서드 `recordImageChange()` 추가 |

### 동작

- 이미지 등록 → `IMAGE_ADDED` 기록 (afterValue에 새 이미지 URL)
- 이미지 변경 → `IMAGE_MODIFIED` 기록 (beforeValue에 기존 URL, afterValue에 새 URL)
- 이미지 삭제 → `IMAGE_DELETED` 기록 (beforeValue에 삭제된 URL)
- 기존 `GET .../changes` API로 이미지 변경사항도 함께 조회됨

### 검증

- [x] 이미지 등록 → IMAGE_ADDED 기록 확인
- [x] 이미지 변경 → IMAGE_MODIFIED (before/after URL) 기록 확인
- [x] 이미지 삭제 → IMAGE_DELETED 기록 확인
- [x] 수정사항 목록 조회 시 이미지 변경사항 포함 확인

---

## 11. 프론트엔드 API 가이드 업데이트

- 핀 추가 요청에 `tabType` 파라미터 및 빈 요구사항 자동 생성 설명 추가
- 핀 삭제 시 번호 자동 재정렬 설명 추가
- 요구사항 추가/수정 요청에 `isRequired` 파라미터 추가
- 문서 상세 조회, 핀 목록 조회, 문서 전체 저장 응답/요청에 `isRequired` 필드 반영
- 수정사항 요약 섹션에 `IMAGE_ADDED/MODIFIED/DELETED` changeType 및 응답 예시 추가
- changeType 종류 테이블 신규 추가 (7가지 유형)

---

## 12. 팀 프로젝트 알림 기능

문서 수정/새 버전 생성 시 같은 팀의 다른 팀원에게 알림을 제공하는 기능을 구현했다.
페이지 진입 시 조회하는 폴링 방식으로 구현 (문서 수정은 빈도가 낮아 SSE/WebSocket 불필요).

### 새 API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/teams/{teamId}/notifications` | 안 읽은 알림 조회 (본인 수행 알림 제외) |
| POST | `/api/teams/{teamId}/notifications/{notificationId}/read` | 알림 읽음 처리 (멱등) |

### 변경/생성 파일

| 파일 | 변경 내용 |
|------|----------|
| `V7__create_team_notifications.sql` | `team_notifications`, `team_notification_reads` 테이블 생성 |
| `TeamNotification.java` | 엔티티 — teamProject, document, documentName, beforeVersion, afterVersion, performedBy |
| `TeamNotificationRead.java` | 사용자별 읽음 추적 엔티티 (notification_id + user_id UNIQUE) |
| `TeamNotificationRepository.java` | JPQL 커스텀 쿼리 — 안 읽은 알림 조회 (본인 수행 제외) |
| `TeamNotificationReadRepository.java` | 읽음 여부 확인 |
| `TeamNotificationResponse.java` | 응답 DTO |
| `TeamService.java` | `getUnreadNotifications()`, `readNotification()` 추가 |
| `TeamController.java` | 2개 엔드포인트 추가 |
| `DocumentService.java` | `createNewVersion()`, `saveDocument()`에서 알림 생성 |

### 핵심 로직

- **알림 생성**: 문서 저장/새 버전 생성 시 `TeamNotification` 자동 생성
- **자기 제외**: JPQL에서 `performedBy.id != :userId` 조건으로 본인 알림 제외
- **읽음 처리**: `TeamNotificationRead` 엔티티로 사용자별 독립 추적, 이미 읽은 알림 재호출 시 무시 (멱등)

### 검증

- [x] 문서 저장 → 알림 생성 확인
- [x] 새 버전 생성 → 알림 생성 확인
- [x] 본인 알림 제외 확인 (LEADER가 수정 → LEADER 조회 시 빈 배열)
- [x] 다른 팀원(MEMBER) 조회 시 알림 표시 확인
- [x] 읽음 처리 후 알림 수 감소 확인
- [x] 이미 읽은 알림 재호출 시 에러 없음 (멱등성)
- [x] 존재하지 않는 알림 → 404
- [x] 다른 팀의 알림 읽기 시도 → 403

---

## 13. AI 번역 안정성 강화

OpenAI API 호출의 비결정적 특성에 대한 방어 로직을 전면 강화했다.

### 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `AiTranslationClient.java` | 전면 리팩터링 — 아래 6가지 개선 적용 |

### 개선 내용

| 항목 | 개선 전 | 개선 후 |
|------|--------|--------|
| JSON 보장 | 프롬프트로만 JSON 요청 | `response_format: json_schema` + `strict: true` (토큰 레벨 강제) |
| 응답 잘림 감지 | 없음 | `finish_reason=length` 체크 → 재시도 |
| 재시도 | 없음 (1회 실패 → 즉시 에러) | 3회 재시도 + 지수 백오프 (1s→2s→4s) |
| 대량 처리 | 한 번에 전부 전송 | 30건씩 청크 분할 처리 |
| ID 검증 | 없음 (AI 누락 시 데이터 유실) | 입력 ID 대조 → 누락분 원본 유지 |
| 타임아웃 | 미설정 (무한 대기 가능) | 연결 10초, 읽기 60초 |

### json_schema 구조

```json
{
  "type": "json_schema",
  "json_schema": {
    "name": "translation_result",
    "strict": true,
    "schema": {
      "type": "object",
      "properties": {
        "items": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "id": { "type": "number" },
              "itemName": { "type": "string" },
              "content": { "type": "string" }
            },
            "required": ["id", "itemName", "content"],
            "additionalProperties": false
          }
        }
      },
      "required": ["items"],
      "additionalProperties": false
    }
  }
}
```

- OpenAI가 토큰 생성 레벨에서 이 스키마를 강제하므로, 구조적으로 잘못된 JSON이 반환될 수 없음
- `gpt-4o` 모델에서 지원

### 방어 로직 흐름

```
1. 요구사항 30건 이상 → 청크 분할
2. 각 청크별 OpenAI API 호출 (json_schema + strict)
3. finish_reason 확인 (length면 재시도)
4. JSON 파싱 → items 배열 추출
5. 입력 ID와 결과 ID 대조 검증
6. 누락된 ID → 원본 텍스트 유지 (경고 로그)
7. 실패 시 → 지수 백오프 후 최대 3회 재시도
```

### 검증

- [x] 기본 번역 (3개 요구사항, 한→영) — 3회 연속 COMPLETED
- [x] 다중 언어 동시 번역 (영+일) — 2개 언어 모두 COMPLETED
- [x] 대량 요구사항 (12개) — 전부 정확히 번역 (빈 원본은 빈 채 유지)
- [x] ID 매핑 정합성 — 입력 ID와 출력 ID 일치 확인
- [x] 일본어 번역 품질 — "ID入力", "パスワード入力" 등 정확

---

## 14. 프론트엔드 API 가이드 업데이트

- 팀 알림 API 2개 섹션 추가 (알림 조회, 읽음 처리)
- 부록 API 전체 목록에 알림 API 추가
- 전수 API 테스트 (40+ 정상 케이스 + 44 에러 케이스 + 13개 응답 DTO 필드 검증) 완료

---

## DB 마이그레이션 이력

| 버전 | 파일명 | 내용 |
|------|--------|------|
| V1 | `V1__init.sql` | 초기 스키마 |
| V2 | `V2__add_original_id_to_requirements.sql` | 요구사항 originalId 추가 |
| V3 | `V3__create_activity_logs.sql` | 활동 로그 테이블 |
| V4 | `V4__create_change_tracking_tables.sql` | 수정사항 추적/확인 테이블 |
| V5 | `V5__user_settings.sql` | 사용자 프로필 필드 추가 + FK ON DELETE 정책 변경 |
| V6 | `V6__add_requirement_is_required.sql` | 요구사항 필수 여부 필드 추가 |
| V7 | `V7__create_team_notifications.sql` | 팀 알림/읽음 추적 테이블 |

---

## 배포 확인

- EC2 서버 (`3.35.208.88:8080`)에서 전체 기능 테스트 완료
- Flyway V3~V7 마이그레이션 자동 적용 확인
- 53개 엔드포인트 전체 정상 동작 (기존 51개 + 알림 2개)
- 전수 API 테스트: 정상 40+ 케이스, 에러 44건, 응답 DTO 13개 필드 구조 100% 일치 확인
