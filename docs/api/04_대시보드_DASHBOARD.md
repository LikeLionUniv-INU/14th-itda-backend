# 대시보드 API (Dashboard)

> **Base URL**: `/api/dashboard`  
> **인증 필요**: 모든 API JWT 필수

---

## 1. 메인 대시보드 조회

### `GET /api/dashboard`

메인 화면의 홈 탭에 필요한 데이터를 통합 조회한다.  
내 프로젝트 목록 + 최근 문서 목록을 한번에 반환.

**인증**: `Bearer {accessToken}`

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "대시보드 정보를 조회했습니다.",
  "data": {
    "user": {
      "id": 1,
      "firstName": "Seoyeon",
      "lastName": "Kim",
      "initial": "K"
    },
    "projects": [
      {
        "id": 1,
        "name": "잇다 프로젝트",
        "defaultLanguage": "ko",
        "memberLanguages": ["ko", "en", "ja"],
        "members": [
          {
            "firstName": "Seoyeon",
            "lastName": "Kim",
            "initial": "K"
          },
          {
            "firstName": "John",
            "lastName": "Smith",
            "initial": "S"
          }
        ],
        "lastDocumentUpdatedAt": "2026-08-12T14:30:00"
      }
    ],
    "recentDocuments": [
      {
        "id": 1,
        "name": "스토리보드1",
        "teamProjectId": 1,
        "teamProjectName": "잇다 프로젝트",
        "language": "ko",
        "latestVersion": 2,
        "updatedAt": "2026-08-12T14:30:00"
      }
    ]
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `user` | Object | 로그인 사용자 기본 정보 (배너 표시용) |
| `projects` | Array | 내가 참여한 프로젝트 목록 |
| `projects[].memberLanguages` | Array | 팀원들이 사용하는 언어 목록 |
| `projects[].members` | Array | 팀원 이름/이니셜 (최대 3명 + 나머지는 프론트에서 +N 처리) |
| `projects[].lastDocumentUpdatedAt` | String | 프로젝트 내 가장 최근 문서 업데이트 시간 |
| `recentDocuments` | Array | 최근 작업한 문서 목록 |

---

## 2. 내 프로젝트 목록 조회 (프로젝트 탭)

### `GET /api/dashboard/projects`

프로젝트 탭에서 사용. 내가 참여한 모든 프로젝트 목록.

**인증**: `Bearer {accessToken}`

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "프로젝트 목록을 조회했습니다.",
  "data": {
    "projects": [
      {
        "id": 1,
        "name": "잇다 프로젝트",
        "defaultLanguage": "ko",
        "memberLanguages": ["ko", "en", "ja"],
        "members": [
          {
            "firstName": "Seoyeon",
            "lastName": "Kim",
            "initial": "K"
          }
        ],
        "memberCount": 5,
        "documentCount": 3,
        "lastDocumentUpdatedAt": "2026-08-12T14:30:00"
      }
    ]
  }
}
```

---

## 3. 내 문서 목록 조회 (문서 탭)

### `GET /api/dashboard/documents`

문서 탭에서 사용. 내가 참여한 모든 프로젝트의 문서를 통합 조회.

**인증**: `Bearer {accessToken}`

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "문서 목록을 조회했습니다.",
  "data": {
    "documents": [
      {
        "id": 1,
        "name": "스토리보드1",
        "teamProjectId": 1,
        "teamProjectName": "잇다 프로젝트",
        "language": "ko",
        "documentType": "STORYBOARD",
        "latestVersion": 2,
        "updatedAt": "2026-08-12T14:30:00"
      }
    ]
  }
}
```
