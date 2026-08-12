# 팀 프로젝트 API (Team)

> **Base URL**: `/api/teams`  
> **인증 필요**: 모든 API JWT 필수

---

## 1. 팀 프로젝트 생성

### `POST /api/teams`

새로운 팀 프로젝트를 생성하고 초대 코드를 발급한다.  
생성한 사용자는 자동으로 **LEADER** 역할이 된다.

**인증**: `Bearer {accessToken}`

#### Request Body
```json
{
  "name": "잇다 프로젝트",
  "defaultLanguage": "ko"
}
```

| 필드 | 타입 | 필수 | 설명 | 제약조건 |
|------|------|------|------|---------|
| `name` | String | O | 프로젝트 이름 | 미입력 시 에러 |
| `defaultLanguage` | String | O | 기본 언어 | 현재 `ko`만 활성화 |

#### Response

**201 Created**
```json
{
  "success": true,
  "message": "팀 프로젝트가 생성되었습니다.",
  "data": {
    "id": 1,
    "name": "잇다 프로젝트",
    "defaultLanguage": "ko",
    "inviteCode": "A3K9F2",
    "createdBy": {
      "id": 1,
      "firstName": "Seoyeon",
      "lastName": "Kim"
    },
    "createdAt": "2026-08-12T10:00:00"
  }
}
```

**400 Bad Request** — 프로젝트 이름 미입력
```json
{
  "success": false,
  "message": "프로젝트 이름을 입력해주세요.",
  "data": null
}
```

---

## 2. 팀 프로젝트 참여 (초대 코드)

### `POST /api/teams/join`

초대 코드로 팀 프로젝트에 참여한다.  
참여한 사용자는 **MEMBER** 역할이 된다.

**인증**: `Bearer {accessToken}`

#### Request Body
```json
{
  "inviteCode": "A3K9F2"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `inviteCode` | String | O | 6자리 초대 코드 (영대문자+숫자) |

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "팀 프로젝트에 참여했습니다.",
  "data": {
    "teamProjectId": 1,
    "name": "잇다 프로젝트",
    "role": "MEMBER",
    "joinedAt": "2026-08-12T11:00:00"
  }
}
```

**404 Not Found** — 유효하지 않은 초대 코드
```json
{
  "success": false,
  "message": "존재하지 않는 초대 코드입니다.",
  "data": null
}
```

**409 Conflict** — 이미 참여 중인 팀
```json
{
  "success": false,
  "message": "이미 참여 중인 팀 프로젝트입니다.",
  "data": null
}
```

---

## 3. 팀 프로젝트 상세 조회

### `GET /api/teams/{teamId}`

팀 프로젝트의 상세 정보를 조회한다.  
팀프로젝트 메인화면에서 사용.

**인증**: `Bearer {accessToken}`  
**권한**: 해당 팀의 멤버만 조회 가능

#### Path Parameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `teamId` | Long | 팀 프로젝트 ID |

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "팀 프로젝트 정보를 조회했습니다.",
  "data": {
    "id": 1,
    "name": "잇다 프로젝트",
    "defaultLanguage": "ko",
    "inviteCode": "A3K9F2",
    "createdAt": "2026-08-12T10:00:00",
    "myRole": "LEADER",
    "members": [
      {
        "id": 1,
        "firstName": "Seoyeon",
        "lastName": "Kim",
        "initial": "K",
        "role": "LEADER",
        "language": "ko",
        "country": "South Korea"
      },
      {
        "id": 2,
        "firstName": "John",
        "lastName": "Smith",
        "initial": "S",
        "role": "MEMBER",
        "language": "en",
        "country": "United States"
      }
    ],
    "documents": [
      {
        "id": 1,
        "name": "스토리보드1",
        "language": "ko",
        "latestVersion": 2,
        "versions": [1, 2],
        "updatedAt": "2026-08-12T14:30:00",
        "updatedBy": {
          "firstName": "Seoyeon",
          "lastName": "Kim"
        }
      }
    ],
    "recentActivities": [
      {
        "authorName": "Kim Seoyeon",
        "authorInitial": "K",
        "documentName": "스토리보드1",
        "version": 2,
        "action": "UPLOADED",
        "timestamp": "2026-08-12T14:30:00"
      }
    ],
    "memberLanguages": ["ko", "en", "ja"]
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `myRole` | String | 현재 로그인 사용자의 역할 (`LEADER` / `MEMBER`) |
| `members` | Array | 팀 멤버 목록 (이름, 역할, 언어, 국적) |
| `documents` | Array | 팀 내 문서 목록 (최신 버전 기준 정렬) |
| `recentActivities` | Array | 활동 요약 (최근 업데이트 이력) |
| `memberLanguages` | Array | 팀 멤버들이 사용하는 언어 목록 (중복 제거) |

**403 Forbidden** — 팀 멤버가 아닌 경우
```json
{
  "success": false,
  "message": "해당 팀 프로젝트의 멤버가 아닙니다.",
  "data": null
}
```

---

## 4. 초대 코드 재조회

### `GET /api/teams/{teamId}/invite-code`

팀 프로젝트의 초대 코드를 조회한다.  
멤버 초대하기 버튼 클릭 시 사용.

**인증**: `Bearer {accessToken}`  
**권한**: 해당 팀의 멤버

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "초대 코드를 조회했습니다.",
  "data": {
    "inviteCode": "A3K9F2"
  }
}
```
