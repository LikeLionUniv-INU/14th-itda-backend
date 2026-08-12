# 번역 API (Translation)

> **Base URL**: `/api/translations`  
> **인증 필요**: 모든 API JWT 필수

---

## 1. 번역 요청

### `POST /api/documents/{documentId}/versions/{version}/translate`

문서의 특정 버전에 대해 AI 번역을 요청한다.  
팀원별 번역 대상 언어를 지정하여 번역 작업(Job)을 생성.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Path Parameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `documentId` | Long | 문서 ID |
| `version` | Integer | 버전 번호 |

#### Request Body
```json
{
  "translations": [
    {
      "userId": 2,
      "targetLanguage": "en"
    },
    {
      "userId": 3,
      "targetLanguage": "ja"
    },
    {
      "userId": 4,
      "targetLanguage": "zh"
    }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `translations` | Array | O | 번역 대상 목록 |
| `translations[].userId` | Long | O | 번역 대상 팀원 ID |
| `translations[].targetLanguage` | String | O | 번역 대상 언어 (`en`, `ja`, `zh`, `vi`) |

#### Response

**202 Accepted**
```json
{
  "success": true,
  "message": "번역 요청이 접수되었습니다.",
  "data": {
    "jobId": 1,
    "documentId": 1,
    "version": 1,
    "status": "PENDING",
    "totalLanguages": 3,
    "completedLanguages": 0,
    "languages": [
      {
        "targetLanguage": "en",
        "targetUser": { "firstName": "John", "lastName": "Smith" },
        "status": "PENDING"
      },
      {
        "targetLanguage": "ja",
        "targetUser": { "firstName": "Taro", "lastName": "Yamada" },
        "status": "PENDING"
      },
      {
        "targetLanguage": "zh",
        "targetUser": { "firstName": "Wei", "lastName": "Zhang" },
        "status": "PENDING"
      }
    ],
    "createdAt": "2026-08-12T15:00:00"
  }
}
```

---

## 2. 번역 진행 상태 조회 (SSE)

### `GET /api/translations/{jobId}/stream`

번역 작업의 진행 상태를 **Server-Sent Events(SSE)**로 실시간 스트리밍한다.  
AI 번역 진행중 화면에서 사용.

**인증**: `Bearer {accessToken}`  
**Content-Type**: `text/event-stream`

#### Path Parameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `jobId` | Long | 번역 작업 ID |

#### SSE 이벤트 형식

**언어별 상태 변경 시:**
```
event: language-status
data: {"language":"en","status":"TRANSLATING","completedLanguages":0,"totalLanguages":3,"progress":0}

event: language-status
data: {"language":"en","status":"COMPLETED","completedLanguages":1,"totalLanguages":3,"progress":33}

event: language-status
data: {"language":"ja","status":"TRANSLATING","completedLanguages":1,"totalLanguages":3,"progress":33}

event: language-status
data: {"language":"ja","status":"COMPLETED","completedLanguages":2,"totalLanguages":3,"progress":66}

event: language-status
data: {"language":"zh","status":"TRANSLATING","completedLanguages":2,"totalLanguages":3,"progress":66}

event: language-status
data: {"language":"zh","status":"COMPLETED","completedLanguages":3,"totalLanguages":3,"progress":100}
```

**전체 완료 시:**
```
event: translation-complete
data: {"jobId":1,"status":"COMPLETED","completedLanguages":3,"totalLanguages":3,"progress":100,"message":"전체 문서의 번역이 완료되었습니다."}
```

**에러 발생 시:**
```
event: translation-error
data: {"jobId":1,"language":"zh","status":"FAILED","error":"번역 중 오류가 발생했습니다."}
```

#### SSE 이벤트 필드 설명

| 이벤트 | 필드 | 설명 |
|--------|------|------|
| `language-status` | `language` | 대상 언어 코드 |
| | `status` | `PENDING` / `TRANSLATING` / `COMPLETED` / `FAILED` |
| | `completedLanguages` | 완료된 언어 수 |
| | `totalLanguages` | 전체 대상 언어 수 |
| | `progress` | 진행률 (0~100, 1/n 방식 계산) |
| `translation-complete` | `message` | 완료 메시지 (3초간 표시 후 팀프로젝트 메인 이동) |

#### 연결 종료
- 전체 번역 완료(`translation-complete`) 이벤트 수신 후 클라이언트가 연결 종료
- 타임아웃: 5분 (300초). 5분 내 완료되지 않으면 연결 종료 → 폴링으로 전환

---

## 3. 번역 상태 조회 (폴링 방식)

### `GET /api/translations/{jobId}`

번역 작업의 현재 상태를 조회한다.  
SSE 연결이 끊긴 경우 **폴링 fallback**으로 사용.

**인증**: `Bearer {accessToken}`

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "번역 상태를 조회했습니다.",
  "data": {
    "jobId": 1,
    "documentId": 1,
    "version": 1,
    "status": "IN_PROGRESS",
    "totalLanguages": 3,
    "completedLanguages": 1,
    "progress": 33,
    "languages": [
      {
        "targetLanguage": "en",
        "status": "COMPLETED",
        "completedAt": "2026-08-12T15:01:30"
      },
      {
        "targetLanguage": "ja",
        "status": "TRANSLATING",
        "completedAt": null
      },
      {
        "targetLanguage": "zh",
        "status": "PENDING",
        "completedAt": null
      }
    ],
    "createdAt": "2026-08-12T15:00:00"
  }
}
```

---

## 진행률 계산 방식

```
progress = (completedLanguages / totalLanguages) × 100

예: 3개 언어 중 1개 완료 → 33%
    3개 언어 중 2개 완료 → 66%
    3개 언어 중 3개 완료 → 100%
```

## 번역 완료 후 동작

1. 클라이언트가 `translation-complete` 이벤트 수신
2. **"전체 문서의 번역이 완료되었습니다."** 메시지 3초간 표시
3. 3초 후 **팀프로젝트 메인 페이지**로 자동 이동
4. 번역된 문서는 팀프로젝트 메인의 문서 목록에 노출
