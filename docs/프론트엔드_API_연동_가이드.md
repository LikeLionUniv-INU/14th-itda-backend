# 프론트엔드 API 연동 가이드

> **API 서버**: `http://<서버IP>:8080`
> **Swagger UI**: `http://<서버IP>:8080/swagger-ui/index.html`
> **MinIO (파일)**: `http://<서버IP>:9000`

---

## 공통 사항

### 응답 형식

모든 API는 동일한 형식으로 응답합니다:

```json
{
  "success": true,
  "message": "처리 결과 메시지",
  "data": { ... }
}
```

실패 시:
```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null
}
```

### 인증

로그인/회원가입/토큰갱신을 제외한 **모든 API**에 토큰이 필요합니다:

```
Authorization: Bearer <accessToken>
```

### 에러 응답

에러 시에도 동일한 형식이며, HTTP 상태 코드로 구분합니다:

```json
{
  "success": false,
  "message": "에러 메시지 (한국어)",
  "data": null
}
```

| HTTP 상태 | 의미 | 프론트 처리 |
|-----------|------|------------|
| **400** Bad Request | 입력값 검증 실패 | `message`를 사용자에게 표시 |
| **401** Unauthorized | 인증 실패 (토큰 만료/잘못된 비밀번호) | 토큰 갱신 시도 or 로그인 화면 이동 |
| **403** Forbidden | 권한 없음 (팀장만 가능한 작업 등) | `message`를 사용자에게 표시 |
| **404** Not Found | 리소스 없음 | `message`를 사용자에게 표시 |
| **409** Conflict | 중복 (이메일 중복, 이미 참여한 팀 등) | `message`를 사용자에게 표시 |
| **500** Internal Server Error | 서버 오류 | "서버 오류가 발생했습니다" 표시 |

### 에러 처리 예시 (JavaScript)

```javascript
async function apiCall(url, options = {}) {
  const token = localStorage.getItem('accessToken');

  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers,
    },
  });

  // 401: 토큰 만료 → 갱신 시도
  if (response.status === 401) {
    const refreshed = await refreshToken();
    if (refreshed) {
      return apiCall(url, options);  // 새 토큰으로 재시도
    }
    window.location.href = '/login';
    return;
  }

  const result = await response.json();

  if (!result.success) {
    // 에러 메시지를 사용자에게 표시
    alert(result.message);
    return null;
  }

  return result.data;
}
```

---

### 토큰 만료 처리

- accessToken 만료: 15분
- refreshToken 만료: 7일
- 401 응답 받으면 → `POST /api/auth/refresh`로 토큰 갱신 → 실패 시 로그인 화면으로

```
API 호출 → 401 응답
  → POST /api/auth/refresh { refreshToken }
    → 성공: 새 토큰으로 원래 요청 재시도
    → 실패: 로그인 화면 이동
```

---

## 화면 1. 회원가입

### 호출 API

```
POST /api/auth/signup
```

### 요청

```json
{
  "firstName": "Gildong",     // 영문만 (a-z, A-Z), 필수
  "lastName": "Hong",         // 영문만 (a-z, A-Z), 필수
  "email": "hong@test.com",   // 이메일 형식, 필수
  "password": "test1234",     // 8~16자, 영문+숫자, 필수
  "country": "KR",            // 필수
  "language": "ko"            // 필수
}
```

### 응답 (201)

```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "id": 1,
    "email": "hong@test.com",
    "firstName": "Gildong",
    "lastName": "Hong",
    "country": "KR",
    "language": "ko",
    "createdAt": "2026-08-17T03:50:40"
  }
}
```

### 에러

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 400 | 필드별 검증 메시지 | 이름에 한글 입력, 비밀번호 8자 미만 등 |
| 409 | "해당 아이디는 사용할 수 없습니다." | 이메일 중복 |

### 주의사항
- firstName, lastName은 **영문만** 허용 (한글 입력 시 400 에러)
- 회원가입 성공 후 → 로그인 화면으로 이동 (자동 로그인 아님)

---

## 화면 2. 로그인

### 호출 API

```
POST /api/auth/login
```

### 요청

```json
{
  "email": "hong@test.com",
  "password": "test1234"
}
```

### 응답 (200)

```json
{
  "success": true,
  "message": "로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

### 에러

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 401 | "알맞은 비밀번호를 입력해주세요." | 비밀번호 틀림 |
| 401 | "로그인이 만료되었습니다. 다시 로그인해주세요." | 토큰 갱신 실패 |

### 프론트 처리
1. `accessToken`, `refreshToken`을 저장 (localStorage 또는 메모리)
2. 이후 모든 API 호출 시 헤더에 `Authorization: Bearer <accessToken>` 추가
3. `expiresIn`은 초 단위 (900 = 15분)

---

## 화면 3. 대시보드

로그인 후 처음 보는 메인 화면입니다. 3개 탭으로 구성됩니다.

### 진입 시 호출 순서

```
1. GET /api/dashboard             → 홈 탭 (인사말, 프로젝트 목록, 최근 문서)
```

- 응답의 `user`에 `firstName`, `lastName`, `initial`이 포함되어 있어 인사말 + 아바타 표시 가능
- `/api/users/me`는 설정 화면 등에서 이메일, 국적, 언어 같은 상세 정보가 필요할 때 호출

탭 전환 시:
```
프로젝트 탭 클릭 → GET /api/dashboard/projects
문서 탭 클릭    → GET /api/dashboard/documents
```

---

### 3-1. 내 정보 (프로필 영역)

```
GET /api/users/me
```

**응답:**

```json
{
  "data": {
    "id": 1,
    "email": "hong@test.com",
    "firstName": "Gildong",
    "lastName": "Hong",
    "country": "KR",
    "language": "ko",
    "bio": "언어의 경계를 넘어 더 나은 팀을 만듭니다.",
    "profileImageUrl": "http://..../profiles/1/1723893600000.png",
    "initial": "H",
    "createdAt": "2026-08-17T03:50:40"
  }
}
```

- `initial`: 프로필 아바타에 표시할 이니셜 (lastName 첫 글자)
- `bio`: 자기소개 (최대 500자, null 가능)
- `profileImageUrl`: 프로필 이미지 URL (null이면 이니셜 아바타 표시)

**에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "사용자를 찾을 수 없습니다." | 삭제된 계정 등 |

---

### 3-2. 홈 탭

```
GET /api/dashboard
```

**응답:**

```json
{
  "data": {
    "user": {
      "id": 1,
      "firstName": "Gildong",
      "lastName": "Hong",
      "initial": "H"
    },
    "projects": [
      {
        "id": 1,
        "name": "ITDA 프로젝트",
        "defaultLanguage": "ko",
        "memberLanguages": ["ko", "en"],
        "members": [
          { "firstName": "Gildong", "lastName": "Hong", "initial": "H" }
        ],
        "memberCount": 3,
        "documentCount": 2,
        "lastDocumentUpdatedAt": "2026-08-17T04:00:00"
      }
    ],
    "recentDocuments": [
      {
        "id": 1,
        "name": "기획문서",
        "teamProjectId": 1,
        "teamProjectName": "ITDA 프로젝트",
        "language": "ko",
        "documentType": "STORYBOARD",
        "latestVersion": 2,
        "updatedAt": "2026-08-17T04:00:00"
      }
    ]
  }
}
```

**화면 동작:**
- 프로젝트 카드 클릭 → 팀 상세 화면 (`/teams/{id}`)
- 문서 카드 클릭 → 문서 편집 화면 (`/documents/{id}/versions/{latestVersion}`)

---

### 3-3. 프로젝트 탭

```
GET /api/dashboard/projects
```

**응답:**

```json
{
  "data": {
    "projects": [
      {
        "id": 1,
        "name": "ITDA 프로젝트",
        "defaultLanguage": "ko",
        "memberLanguages": ["ko", "en"],
        "members": [
          { "firstName": "Gildong", "lastName": "Hong", "initial": "H" }
        ],
        "memberCount": 3,
        "documentCount": 2,
        "lastDocumentUpdatedAt": "2026-08-17T04:00:00"
      }
    ]
  }
}
```

---

### 3-4. 문서 탭

```
GET /api/dashboard/documents
```

**응답:**

```json
{
  "data": {
    "documents": [
      {
        "id": 1,
        "name": "기획문서",
        "teamProjectId": 1,
        "teamProjectName": "ITDA 프로젝트",
        "language": "ko",
        "documentType": "STORYBOARD",
        "latestVersion": 2,
        "updatedAt": "2026-08-17T04:00:00"
      }
    ]
  }
}
```

---

## 화면 4. 팀 생성

### 호출 API

```
POST /api/teams
```

### 요청

```json
{
  "name": "ITDA 프로젝트",    // 필수
  "defaultLanguage": "ko"     // 필수
}
```

### 응답 (201)

```json
{
  "data": {
    "id": 1,
    "name": "ITDA 프로젝트",
    "defaultLanguage": "ko",
    "inviteCode": "UPVUZ7",
    "createdBy": {
      "id": 1,
      "firstName": "Gildong",
      "lastName": "Hong"
    },
    "createdAt": "2026-08-17T03:51:10"
  }
}
```

- 생성자는 자동으로 **LEADER** 역할
- `inviteCode`를 팀원에게 공유하여 참여시킴

---

## 화면 5. 팀 참여 (초대코드 입력)

### 호출 API

```
POST /api/teams/join
```

### 요청

```json
{
  "inviteCode": "UPVUZ7"     // 필수
}
```

### 응답 (200)

```json
{
  "data": {
    "teamProjectId": 1,
    "name": "ITDA 프로젝트",
    "role": "MEMBER",
    "joinedAt": "2026-08-17T03:52:46"
  }
}
```

**에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "존재하지 않는 초대 코드입니다." | 잘못된 초대코드 입력 |
| 409 | "이미 참여 중인 팀 프로젝트입니다." | 이미 참여한 팀에 다시 참여 |

- 참여 후 → 팀 상세 화면으로 이동

---

## 화면 6. 팀 상세

### 진입 시 호출

```
GET /api/teams/{teamId}
```

### 응답 (200)

```json
{
  "data": {
    "id": 1,
    "name": "ITDA 프로젝트",
    "defaultLanguage": "ko",
    "inviteCode": "UPVUZ7",
    "createdAt": "2026-08-17T03:51:10",
    "myRole": "LEADER",
    "members": [
      {
        "id": 1,
        "firstName": "Gildong",
        "lastName": "Hong",
        "initial": "H",
        "role": "LEADER",
        "language": "ko",
        "country": "KR"
      },
      {
        "id": 2,
        "firstName": "John",
        "lastName": "Doe",
        "initial": "D",
        "role": "MEMBER",
        "language": "en",
        "country": "US"
      }
    ],
    "documents": [
      {
        "id": 1,
        "name": "기획문서",
        "documentType": "STORYBOARD",
        "language": "ko",
        "latestVersion": 2,
        "versions": [1, 2],
        "updatedAt": "2026-08-17T04:00:00",
        "updatedBy": {
          "firstName": "Gildong",
          "lastName": "Hong"
        }
      }
    ],
    "memberLanguages": ["ko", "en"],
    "activityLogs": [
      {
        "id": 1,
        "actionType": "UPLOADED",
        "documentName": "스토리보드",
        "documentType": "STORYBOARD",
        "version": 3,
        "performedByFirstName": "Gildong",
        "performedByLastName": "Hong",
        "performedByInitial": "H",
        "createdAt": "2026-08-17T04:00:00"
      },
      {
        "id": 2,
        "actionType": "UPDATED",
        "documentName": "스토리보드",
        "documentType": "STORYBOARD",
        "version": 2,
        "performedByFirstName": "Gildong",
        "performedByLastName": "Hong",
        "performedByInitial": "H",
        "createdAt": "2026-08-17T03:55:00"
      }
    ]
  }
}
```

**에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "팀 프로젝트를 찾을 수 없습니다." | 잘못된 teamId |
| 403 | "해당 팀 프로젝트의 멤버가 아닙니다." | 팀원이 아닌데 접근 |

**화면 동작:**
- `myRole`이 `"LEADER"`일 때만 문서 생성/편집 권한 표시
- 초대코드 복사 버튼 → `inviteCode` 사용
- 문서 클릭 → 문서 편집 화면
- `activityLogs`의 `actionType`: `UPLOADED`(문서/버전 생성) / `UPDATED`(문서 저장)
- 활동 요약 표시 예: `"{documentName}_version{version}이 업로드 되었습니다."` + `createdAt`으로 상대 시간 계산
- 최근 10개까지 반환됨

---

### 초대코드 조회 (별도 API)

```
GET /api/teams/{teamId}/invite-code
```

```json
{ "data": { "inviteCode": "UPVUZ7" } }
```

---

### 문서 생성 (팀장만)

```
POST /api/teams/{teamId}/documents
```

```json
{
  "name": "기획문서",       // 최대 10자, 필수
  "language": "ko",         // 필수
  "version": 1,             // 1 이상
  "documentType": "STORYBOARD"  // 선택 (기본값: STORYBOARD)
}
```

**응답 (201):**

```json
{
  "data": {
    "documentId": 1,
    "name": "기획문서",
    "language": "ko",
    "documentType": "STORYBOARD",
    "version": 1,
    "status": "DRAFT",
    "createdAt": "2026-08-17T03:52:00"
  }
}
```

**에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 403 | "팀장만 수행할 수 있는 작업입니다." | MEMBER가 문서 생성 시도 |
| 403 | "해당 팀 프로젝트의 멤버가 아닙니다." | 팀원이 아닌데 생성 시도 |

---

## 화면 7. 문서 편집 (핵심 화면)

가장 복잡한 화면입니다. 페이지 목록 + 와이어프레임 + 핀 + 요구사항을 모두 다룹니다.

### 진입 시 호출

```
GET /api/documents/{documentId}/versions/{version}
```

쿼리 파라미터:
- `lang` (선택): 번역된 문서 조회. 예) `?lang=en`

### 응답 (200) — 문서 전체 데이터

```json
{
  "data": {
    "documentId": 1,
    "name": "기획문서",
    "language": "ko",
    "documentType": "STORYBOARD",
    "version": 1,
    "status": "IN_PROGRESS",
    "isAutoSaved": false,
    "changeSummary": "로그인 화면 추가",
    "createdAt": "2026-08-17T03:52:00",
    "updatedAt": "2026-08-17T04:00:00",
    "pages": [
      {
        "id": 7,
        "pageNumber": 1,
        "screenName": "로그인화면",
        "screenId": "SCR001",
        "wireframeImages": [
          {
            "id": 1,
            "imageType": "WIREFRAME",
            "imageUrl": "http://<서버IP>:9000/itda/wireframes/1/1/7/wireframe_xxx.png",
            "originalWidth": 1920,
            "originalHeight": 1080,
            "displayWidth": 960,
            "displayHeight": 540
          }
        ],
        "pins": [
          {
            "id": 1,
            "pinNumber": 1,
            "xCoordinate": 150.0,
            "yCoordinate": 250.0,
            "requirements": [
              {
                "id": 1,
                "tabType": "SCREEN",
                "itemName": "이메일입력",
                "content": "이메일 형식 검증 필요"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

---

### 7-1. 페이지 관리

**페이지 추가:**
```
POST /api/documents/{documentId}/versions/{version}/pages
```
```json
{ "screenName": "홈화면", "screenId": "SCR002" }
```
- screenName, screenId 각각 최대 10자

**페이지 수정:**
```
PUT /api/documents/{documentId}/versions/{version}/pages/{pageId}
```
```json
{ "screenName": "메인화면", "screenId": "SCR002" }
```

**페이지 삭제:**
```
DELETE /api/documents/{documentId}/versions/{version}/pages/{pageId}
```
- 해당 페이지의 와이어프레임, 핀, 요구사항 모두 연쇄 삭제

**페이지 순서 변경:**
```
PATCH /api/documents/{documentId}/versions/{version}/pages/reorder
```
```json
{ "pageIds": [8, 7, 9] }
```
- 변경하고 싶은 순서대로 pageId 배열 전달

**페이지 관리 에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "해당 버전을 찾을 수 없습니다." | 잘못된 version |
| 404 | "페이지를 찾을 수 없습니다." | 잘못된 pageId |
| 403 | "팀장만 수행할 수 있는 작업입니다." | MEMBER가 페이지 수정/삭제 시도 |

---

### 7-2. 와이어프레임 이미지 업로드

3단계 프로세스입니다:

```
1. Presigned URL 발급  →  서버에서 업로드용 URL 받기
2. 파일 업로드          →  받은 URL로 MinIO에 직접 업로드
3. 메타데이터 등록      →  서버에 이미지 정보 저장
```

**Step 1. Presigned URL 발급:**
```
POST /api/files/presigned-url
```
```json
{
  "fileName": "wireframe.png",    // 필수
  "contentType": "image/png",     // 필수 (image/png, image/jpeg 등)
  "imageType": "WIREFRAME",       // 필수
  "pageId": 7                     // 필수
}
```

응답:
```json
{
  "data": {
    "presignedUrl": "http://<서버IP>:9000/itda/wireframes/...?X-Amz-Signature=...",
    "fileUrl": "http://<서버IP>:9000/itda/wireframes/.../wireframe_xxx.png",
    "key": "wireframes/1/1/7/wireframe_xxx.png"
  }
}
```

**Step 2. 파일 업로드 (프론트 → MinIO 직접):**
```javascript
// presignedUrl로 PUT 요청 (서버가 아닌 MinIO로 직접)
await fetch(presignedUrl, {
  method: 'PUT',
  headers: { 'Content-Type': 'image/png' },
  body: file  // File 객체
});
```

**Step 3. 메타데이터 등록:**
```
POST /api/pages/{pageId}/wireframe-images
```
```json
{
  "imageType": "WIREFRAME",       // 필수
  "imageUrl": "<Step1에서 받은 fileUrl>",  // 필수
  "originalWidth": 1920,
  "originalHeight": 1080,
  "displayWidth": 960,            // 필수
  "displayHeight": 540            // 필수
}
```

**이미지 교체:**
```
PUT /api/pages/{pageId}/wireframe-images/{imageId}
```
- 같은 형식, 새 이미지 URL로 요청 (기존 S3 파일 자동 삭제)

**이미지 삭제:**
```
DELETE /api/pages/{pageId}/wireframe-images/{imageId}
```
- S3 파일 + DB 메타데이터 모두 삭제

**이미지 관리 에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "페이지를 찾을 수 없습니다." | 잘못된 pageId |
| 404 | "이미지를 찾을 수 없습니다." | 잘못된 imageId |
| 403 | "팀장만 수행할 수 있는 작업입니다." | MEMBER가 이미지 업로드/수정/삭제 시도 |

---

### 7-3. 핀 관리

와이어프레임 이미지 위에 핀을 찍어서 요구사항을 연결합니다.

**핀 추가 (이미지 클릭 시):**
```
POST /api/pages/{pageId}/pins
```
```json
{
  "xCoordinate": 150.5,   // 필수 (double)
  "yCoordinate": 250.3    // 필수 (double)
}
```

응답:
```json
{
  "data": {
    "id": 1,
    "pinNumber": 1,       // 자동 부여된 번호
    "xCoordinate": 150.5,
    "yCoordinate": 250.3,
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

**핀 위치 수정 (드래그):**
```
PUT /api/pages/{pageId}/pins/{pinId}
```
```json
{ "xCoordinate": 200.0, "yCoordinate": 300.0 }
```

**핀 삭제:**
```
DELETE /api/pages/{pageId}/pins/{pinId}
```
- 연결된 요구사항도 모두 삭제

**핀 관리 에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "페이지를 찾을 수 없습니다." | 잘못된 pageId |
| 404 | "핀을 찾을 수 없습니다." | 잘못된 pinId |
| 403 | "팀장만 수행할 수 있는 작업입니다." | MEMBER가 핀 추가/수정/삭제 시도 |
| 403 | "해당 팀 프로젝트의 멤버가 아닙니다." | 팀원이 아닌데 접근 |

**핀 목록 조회 (요구사항 포함):**
```
GET /api/pages/{pageId}/pins
GET /api/pages/{pageId}/pins?tabType=SCREEN    // 탭 필터
```

응답:
```json
{
  "data": [
    {
      "id": 1,
      "pinNumber": 1,
      "xCoordinate": 150.5,
      "yCoordinate": 250.3,
      "requirements": [
        {
          "id": 1,
          "tabType": "SCREEN",
          "itemName": "이메일입력",
          "content": "이메일 형식 검증 필요"
        }
      ]
    }
  ]
}
```

---

### 7-4. 요구사항 관리

핀에 연결된 요구사항을 관리합니다.

**요구사항 추가:**
```
POST /api/pins/{pinId}/requirements
```
```json
{
  "tabType": "SCREEN",         // 필수 (SCREEN, FUNCTION, DATA 등)
  "itemName": "이메일입력",    // 최대 10자
  "content": "이메일 형식 검증 필요"  // 최대 200자
}
```

**요구사항 수정:**
```
PUT /api/pins/{pinId}/requirements/{requirementId}
```
```json
{
  "itemName": "이메일입력",
  "content": "이메일 형식 검증 및 중복 체크 필요"
}
```

**요구사항 삭제:**
```
DELETE /api/pins/{pinId}/requirements/{requirementId}
```

**요구사항 관리 에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "핀을 찾을 수 없습니다." | 잘못된 pinId |
| 404 | "요구사항을 찾을 수 없습니다." | 잘못된 requirementId |
| 400 | "수정내용을 입력해주세요." | content가 빈 문자열 |
| 403 | "팀장만 수행할 수 있는 작업입니다." | MEMBER가 요구사항 수정/삭제 시도 |

---

### 7-5. 문서 저장

**전체 저장 (저장 버튼 클릭):**
```
PUT /api/documents/{documentId}/versions/{version}
```
```json
{
  "status": "IN_PROGRESS",
  "changeSummary": "로그인 화면 요구사항 추가",
  "pages": [
    {
      "pageNumber": 1,
      "screenName": "로그인화면",
      "screenId": "SCR001",
      "pins": [
        {
          "pinNumber": 1,
          "xCoordinate": 150.0,
          "yCoordinate": 250.0,
          "requirements": [
            {
              "tabType": "SCREEN",
              "itemName": "이메일입력",
              "content": "이메일 형식 검증 필요"
            }
          ]
        }
      ]
    }
  ]
}
```

- 전체 문서 데이터를 한 번에 보냄 (pages/pins/requirements 일괄)

**자동 저장 (일정 간격으로 자동 호출):**
```
POST /api/documents/{documentId}/versions/{version}/auto-save
```
- 요청 형식은 전체 저장과 동일
- 자동 저장 시 `isAutoSaved`가 `true`로 변경됨

**문서 저장 에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "문서를 찾을 수 없습니다." | 잘못된 documentId |
| 404 | "해당 버전을 찾을 수 없습니다." | 잘못된 version |
| 403 | "팀장만 수행할 수 있는 작업입니다." | MEMBER가 저장 시도 |
| 403 | "해당 팀 프로젝트의 멤버가 아닙니다." | 팀원이 아닌데 저장 시도 |

---

### 7-6. 수정사항 요약

문서를 저장하면 이전 내용과 비교하여 변경사항이 자동 기록됩니다.

**수정사항 목록 조회:**
```
GET /api/documents/{documentId}/versions/{version}/changes
```

응답:
```json
{
  "data": {
    "totalChanges": 3,
    "confirmedByMe": 1,
    "unconfirmedByMe": 2,
    "changes": [
      {
        "id": 1,
        "changeType": "REQUIREMENT_MODIFIED",
        "pageNumber": 1,
        "screenName": "회원가입",
        "pinNumber": 1,
        "itemDescription": "ID입력",
        "beforeValue": "{\"tabType\":\"SCREEN\",\"itemName\":\"ID입력\",\"content\":\"이메일 형식 검증\"}",
        "afterValue": "{\"tabType\":\"SCREEN\",\"itemName\":\"ID입력\",\"content\":\"이메일 형식 검증 및 중복 체크\"}",
        "modifiedByFirstName": "Gildong",
        "modifiedByLastName": "Hong",
        "createdAt": "2026-08-17T04:00:00",
        "confirmedByMe": false
      },
      {
        "id": 2,
        "changeType": "REQUIREMENT_ADDED",
        "pageNumber": 1,
        "screenName": "회원가입",
        "pinNumber": 2,
        "itemDescription": "비밀번호",
        "beforeValue": null,
        "afterValue": "{\"tabType\":\"SCREEN\",\"itemName\":\"비밀번호\",\"content\":\"8자 이상 영문+숫자\"}",
        "modifiedByFirstName": "Gildong",
        "modifiedByLastName": "Hong",
        "createdAt": "2026-08-17T04:00:00",
        "confirmedByMe": true
      }
    ]
  }
}
```

- `changeType`: `REQUIREMENT_ADDED`, `REQUIREMENT_MODIFIED`, `REQUIREMENT_DELETED`, `SCREEN_MODIFIED`
- `beforeValue`/`afterValue`: JSON 문자열 (변경 전/후 데이터)
- `confirmedByMe`: 내가 이 수정사항을 확인했는지 여부
- 최초 저장 시 (기존 데이터 없음)에는 변경사항이 기록되지 않음

**개별 수정사항 확인:**
```
POST /api/documents/{documentId}/versions/{version}/changes/{changeId}/confirm
```

**전체 수정사항 확인:**
```
POST /api/documents/{documentId}/versions/{version}/changes/confirm-all
```

**수정사항 에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "수정사항을 찾을 수 없습니다." | 잘못된 changeId |
| 409 | "이미 확인한 수정사항입니다." | 이미 확인 처리된 수정사항 재확인 |

---

## 화면 8. 버전 관리

### 버전 목록 조회

```
GET /api/documents/{documentId}/versions
```

응답:
```json
{
  "data": [
    {
      "id": 1,
      "version": 1,
      "status": "IN_PROGRESS",
      "isAutoSaved": false,
      "changeSummary": "로그인 화면 추가",
      "createdByFirstName": "Gildong",
      "createdByLastName": "Hong",
      "createdAt": "2026-08-17T03:52:00",
      "updatedAt": "2026-08-17T04:00:00"
    },
    {
      "id": 2,
      "version": 2,
      "status": "EDITING",
      "isAutoSaved": false,
      "changeSummary": null,
      "createdByFirstName": "Gildong",
      "createdByLastName": "Hong",
      "createdAt": "2026-08-17T04:05:00",
      "updatedAt": "2026-08-17T04:05:00"
    }
  ]
}
```

### 새 버전 생성

```
POST /api/documents/{documentId}/versions
```
```json
{ "baseVersion": 1 }
```
- 기존 버전을 복사하여 새 버전 생성

### 버전 삭제

```
DELETE /api/documents/{documentId}/versions/{version}
```
- 마지막 남은 버전은 삭제 불가

**버전 관리 에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 404 | "해당 버전을 찾을 수 없습니다." | 잘못된 version |
| 404 | "기준 버전을 찾을 수 없습니다." | 새 버전 생성 시 baseVersion이 존재하지 않음 |
| 400 | "마지막 남은 버전은 삭제할 수 없습니다." | 버전이 1개만 남았을 때 삭제 시도 |

### 버전 전환

```
GET /api/documents/{documentId}/versions/{version}
```
- 다른 버전 번호로 문서 조회하면 됨

---

## 화면 9. AI 번역

### 호출 순서

```
1. POST .../translate          → 번역 요청 (비동기)
2. GET  .../stream             → SSE로 실시간 진행 상황 수신
   또는 GET .../translations/{jobId}  → 폴링으로 상태 확인
3. GET  .../versions/{ver}?lang=en    → 번역된 문서 조회
```

---

### Step 1. 번역 요청

```
POST /api/documents/{documentId}/versions/{version}/translate
```
```json
{
  "translations": [
    { "userId": 2, "targetLanguage": "en" },
    { "userId": 3, "targetLanguage": "ja" }
  ]
}
```

응답 (202 Accepted):
```json
{
  "data": {
    "jobId": 1,
    "status": "PENDING",
    "totalLanguages": 2,
    "completedLanguages": 0,
    "progress": 0,
    "createdAt": "2026-08-17T04:16:28",
    "completedAt": null,
    "languages": [
      { "id": 1, "targetLanguage": "en", "status": "PENDING", "completedAt": null },
      { "id": 2, "targetLanguage": "ja", "status": "PENDING", "completedAt": null }
    ]
  }
}
```

---

### Step 2a. SSE 실시간 스트리밍 (권장)

```javascript
const eventSource = new EventSource(
  `http://<서버IP>:8080/api/translations/${jobId}/stream`,
  // 주의: EventSource는 커스텀 헤더를 지원하지 않음
  // 토큰은 쿼리파라미터가 아닌 별도 방식으로 처리 필요
);

eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log(data);  // 진행 상황 업데이트
};

eventSource.addEventListener('translation-complete', (event) => {
  console.log('번역 완료!');
  eventSource.close();
});

eventSource.addEventListener('translation-error', (event) => {
  console.log('번역 실패');
  eventSource.close();
});
```

- 타임아웃: 5분
- SSE 연결이 어려우면 Step 2b 폴링 사용

---

### Step 2b. 폴링 (대안)

```
GET /api/translations/{jobId}
```

응답:
```json
{
  "data": {
    "jobId": 1,
    "status": "COMPLETED",       // PENDING → IN_PROGRESS → COMPLETED / FAILED
    "totalLanguages": 2,
    "completedLanguages": 2,
    "progress": 100,
    "completedAt": "2026-08-17T04:16:31",
    "languages": [
      { "id": 1, "targetLanguage": "en", "status": "COMPLETED", "completedAt": "..." },
      { "id": 2, "targetLanguage": "ja", "status": "COMPLETED", "completedAt": "..." }
    ]
  }
}
```

- 2~3초 간격으로 폴링하여 `status`가 `COMPLETED` 또는 `FAILED`가 될 때까지 반복

---

### Step 3. 번역된 문서 조회

```
GET /api/documents/{documentId}/versions/{version}?lang=en
```

- `lang` 파라미터에 번역 대상 언어를 넣으면, 요구사항의 `content`가 번역된 텍스트로 반환됨
- `lang` 파라미터 없이 호출하면 원문 반환

### 에러 응답

| 상황 | HTTP | message |
|------|------|---------|
| 문서 없음 | 404 | `"문서를 찾을 수 없습니다."` |
| 버전 없음 | 404 | `"해당 버전을 찾을 수 없습니다."` |
| 번역 작업 없음 | 404 | `"번역 작업을 찾을 수 없습니다."` |
| 팀 멤버 아님 | 403 | `"해당 팀 프로젝트의 멤버가 아닙니다."` |
| 팀장 아님 | 403 | `"팀장만 수행할 수 있는 작업입니다."` |
| OpenAI API 오류 | 500 | `"서버 내부 오류가 발생했습니다."` |

---

## 화면 10. 설정

### 진입 시 호출

```
GET /api/users/me    → 내 정보 전체 (이메일, 국적, 언어, bio, 프로필 이미지 등)
```

### 10-1. 프로필 수정

```
PUT /api/users/me
```

**요청:**

```json
{
  "firstName": "Minjun",
  "lastName": "Park",
  "country": "대한민국",
  "language": "English",
  "bio": "언어의 경계를 넘어 더 나은 팀을 만듭니다."
}
```

- `firstName`, `lastName`: 영문만 (a-z, A-Z), 필수
- `bio`: 최대 500자, 생략 가능 (null 허용)

**응답 (200):** 수정된 `UserResponse` 반환 (3-1과 동일 구조)

**에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 400 | "이름은 영문자만 입력 가능합니다." | 한글 등 입력 |
| 400 | "자기소개는 500자 이내로 입력해주세요." | 초과 |

### 10-2. 프로필 이미지 변경

**Step 1. Presigned URL 발급:**

```
POST /api/users/me/profile-image/presigned-url
```

```json
{
  "fileName": "profile.png",
  "contentType": "image/png"
}
```

**응답:**

```json
{
  "data": {
    "presignedUrl": "https://s3.../profiles/1/1723893600000.png?X-Amz-...",
    "fileUrl": "http://..../profiles/1/1723893600000.png",
    "key": "profiles/1/1723893600000.png"
  }
}
```

**Step 2. S3에 직접 업로드:**

```javascript
await fetch(presignedUrl, {
  method: "PUT",
  headers: { "Content-Type": "image/png" },
  body: file,
});
```

**Step 3. 이미지 URL 저장:**

```
PUT /api/users/me/profile-image
```

```json
{
  "profileImageUrl": "http://..../profiles/1/1723893600000.png"
}
```

**응답 (200):** 수정된 `UserResponse` 반환

> 기존 이미지가 있으면 S3에서 자동 삭제 후 교체됩니다.

**이미지 삭제:**

```
DELETE /api/users/me/profile-image
```

### 10-3. 비밀번호 변경

```
PUT /api/users/me/password
```

```json
{
  "currentPassword": "test1234",
  "newPassword": "newpass1234"
}
```

- `newPassword`: 8~16자, 영문+숫자 조합

**에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 401 | "현재 비밀번호가 일치하지 않습니다." | 비밀번호 틀림 |
| 400 | "새 비밀번호는 현재 비밀번호와 다르게 설정해주세요." | 동일 비밀번호 |
| 400 | "8~16자의 영문, 숫자 조합으로 입력해주세요." | 형식 불일치 |

### 10-4. 이메일 변경

```
PUT /api/users/me/email
```

```json
{
  "password": "test1234",
  "newEmail": "newemail@example.com"
}
```

**에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 401 | "비밀번호가 일치하지 않습니다." | 비밀번호 틀림 |
| 400 | "현재 이메일과 동일합니다." | 동일 이메일 |
| 409 | "이미 사용 중인 이메일입니다." | 중복 |

> 이메일 변경 시 기존 리프레시 토큰이 모두 삭제됩니다. 프론트에서 재로그인 처리가 필요합니다.

### 10-5. 회원 탈퇴

```
DELETE /api/users/me
```

```json
{
  "password": "test1234"
}
```

**에러:**

| 상태 | 메시지 | 상황 |
|------|--------|------|
| 401 | "비밀번호가 일치하지 않습니다." | 비밀번호 틀림 |

> 계정 삭제 시 팀 멤버십, 리프레시 토큰, 수정사항 확인 이력이 함께 삭제됩니다. 생성한 문서/프로젝트/활동 로그는 유지되며 작성자 정보만 null 처리됩니다.

---

## 부록: API 전체 목록

### 인증 (토큰 불필요)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/refresh` | 토큰 갱신 |

### 사용자

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/users/me` | 내 정보 조회 |
| PUT | `/api/users/me` | 프로필 수정 |
| POST | `/api/users/me/profile-image/presigned-url` | 프로필 이미지 업로드 URL 발급 |
| PUT | `/api/users/me/profile-image` | 프로필 이미지 저장 |
| DELETE | `/api/users/me/profile-image` | 프로필 이미지 삭제 |
| PUT | `/api/users/me/password` | 비밀번호 변경 |
| PUT | `/api/users/me/email` | 이메일 변경 |
| DELETE | `/api/users/me` | 회원 탈퇴 |

### 대시보드

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/dashboard` | 홈 탭 |
| GET | `/api/dashboard/projects` | 프로젝트 탭 |
| GET | `/api/dashboard/documents` | 문서 탭 |

### 팀 프로젝트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/teams` | 팀 생성 |
| POST | `/api/teams/join` | 팀 참여 |
| GET | `/api/teams/{teamId}` | 팀 상세 조회 |
| GET | `/api/teams/{teamId}/invite-code` | 초대코드 조회 |

### 문서 관리

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/teams/{teamId}/documents` | 문서 생성 (팀장만) |
| GET | `/api/documents/{id}/versions/{ver}` | 문서 상세 조회 |
| PUT | `/api/documents/{id}/versions/{ver}` | 문서 저장 |
| POST | `/api/documents/{id}/versions/{ver}/auto-save` | 자동 저장 |
| GET | `/api/documents/{id}/versions` | 버전 목록 |
| POST | `/api/documents/{id}/versions` | 새 버전 생성 |
| DELETE | `/api/documents/{id}/versions/{ver}` | 버전 삭제 |
| GET | `/api/documents/{id}/versions/{ver}/changes` | 수정사항 목록 조회 |
| POST | `/api/documents/{id}/versions/{ver}/changes/{changeId}/confirm` | 수정사항 개별 확인 |
| POST | `/api/documents/{id}/versions/{ver}/changes/confirm-all` | 수정사항 전체 확인 |

### 페이지

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/documents/{id}/versions/{ver}/pages` | 페이지 추가 |
| PUT | `/api/documents/{id}/versions/{ver}/pages/{pageId}` | 페이지 수정 |
| DELETE | `/api/documents/{id}/versions/{ver}/pages/{pageId}` | 페이지 삭제 |
| PATCH | `/api/documents/{id}/versions/{ver}/pages/reorder` | 순서 변경 |

### 핀

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/pages/{pageId}/pins` | 핀 추가 |
| PUT | `/api/pages/{pageId}/pins/{pinId}` | 핀 수정 |
| DELETE | `/api/pages/{pageId}/pins/{pinId}` | 핀 삭제 |
| GET | `/api/pages/{pageId}/pins` | 핀 목록 조회 |

### 요구사항

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/pins/{pinId}/requirements` | 요구사항 추가 |
| PUT | `/api/pins/{pinId}/requirements/{id}` | 요구사항 수정 |
| DELETE | `/api/pins/{pinId}/requirements/{id}` | 요구사항 삭제 |

### 파일 업로드

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/files/presigned-url` | Presigned URL 발급 |
| POST | `/api/pages/{pageId}/wireframe-images` | 이미지 등록 |
| PUT | `/api/pages/{pageId}/wireframe-images/{id}` | 이미지 수정 |
| DELETE | `/api/pages/{pageId}/wireframe-images/{id}` | 이미지 삭제 |

### AI 번역

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/documents/{id}/versions/{ver}/translate` | 번역 요청 |
| GET | `/api/translations/{jobId}` | 번역 상태 조회 |
| GET | `/api/translations/{jobId}/stream` | SSE 스트리밍 |
