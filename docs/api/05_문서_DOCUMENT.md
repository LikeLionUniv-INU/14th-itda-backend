# 문서 API (Document)

> **Base URL**: `/api/teams/{teamId}/documents`  
> **인증 필요**: 모든 API JWT 필수

---

## 1. 문서 생성

### `POST /api/teams/{teamId}/documents`

팀 프로젝트 내에 새 문서를 생성한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만** 가능

#### Path Parameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `teamId` | Long | 팀 프로젝트 ID |

#### Request Body
```json
{
  "name": "스토리보드1",
  "language": "ko",
  "version": 1,
  "documentType": "STORYBOARD"
}
```

| 필드 | 타입 | 필수 | 설명 | 제약조건 |
|------|------|------|------|---------|
| `name` | String | O | 문서 이름 | 최대 10자 |
| `language` | String | O | 작성 언어 | 현재 `ko`만 활성화 |
| `version` | Integer | O | 버전 번호 | 기본값 1, 숫자만 |
| `documentType` | String | O | 문서 유형 | 현재 `STORYBOARD`만 |

#### Response

**201 Created**
```json
{
  "success": true,
  "message": "문서가 생성되었습니다.",
  "data": {
    "id": 1,
    "name": "스토리보드1",
    "language": "ko",
    "version": 1,
    "documentType": "STORYBOARD",
    "createdBy": {
      "id": 1,
      "firstName": "Seoyeon",
      "lastName": "Kim"
    },
    "createdAt": "2026-08-12T10:00:00"
  }
}
```

**403 Forbidden** — 팀원(MEMBER)이 문서 생성 시도
```json
{
  "success": false,
  "message": "문서 생성은 팀장만 가능합니다.",
  "data": null
}
```

---

## 2. 문서 상세 조회 (버전별)

### `GET /api/documents/{documentId}/versions/{version}`

문서의 특정 버전 전체 데이터를 조회한다.  
페이지 목록, 핀, 요구사항을 모두 포함하여 반환.  
**문서 확인** 화면과 **문서 작성** 화면 양쪽에서 사용.

**인증**: `Bearer {accessToken}`  
**권한**: 해당 팀의 멤버

#### Path Parameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `documentId` | Long | 문서 ID |
| `version` | Integer | 버전 번호 |

#### Query Parameter

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `lang` | String | X | 번역 언어 (미지정 시 원본 언어) |

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "문서를 조회했습니다.",
  "data": {
    "id": 1,
    "documentId": 1,
    "name": "스토리보드1",
    "language": "ko",
    "version": 1,
    "status": "COMPLETED",
    "isAutoSaved": false,
    "createdBy": {
      "id": 1,
      "firstName": "Seoyeon",
      "lastName": "Kim"
    },
    "pages": [
      {
        "id": 1,
        "pageNumber": 1,
        "screenName": "회원가입",
        "screenId": "SIGN_UP_01",
        "wireframeImages": [
          {
            "id": 1,
            "imageType": "DESKTOP",
            "imageUrl": "https://s3.../wireframes/1/1/1/desktop_1723456789.png",
            "displayWidth": 660,
            "displayHeight": 370
          }
        ],
        "pins": [
          {
            "id": 1,
            "pinNumber": 1,
            "xCoordinate": 150.5,
            "yCoordinate": 200.3,
            "requirements": {
              "COMMON": [
                {
                  "id": 1,
                  "itemName": "ID 입력",
                  "content": "아이디 중복검사 기능 버튼..."
                }
              ],
              "PLANNING": [],
              "FRONTEND": [
                {
                  "id": 2,
                  "itemName": "ID 입력",
                  "content": "이메일 형식 검증..."
                }
              ],
              "BACKEND": [
                {
                  "id": 3,
                  "itemName": "ID 입력",
                  "content": "이메일 중복 체크 API..."
                }
              ],
              "DESIGN": []
            }
          }
        ]
      }
    ],
    "updatedAt": "2026-08-12T14:30:00"
  }
}
```

> **`lang` 파라미터 사용 시**: 팀원이 번역된 문서를 볼 때 `?lang=en`으로 요청하면, `requirements` 내용이 `translated_requirements` 테이블에서 해당 언어로 번역된 텍스트로 대체되어 반환된다. 와이어프레임/핀 구조는 동일.

---

## 3. 문서 버전 목록 조회

### `GET /api/documents/{documentId}/versions`

문서의 모든 버전 이력을 조회한다.  
문서 수정 화면 하단의 **버전 변경 이력** 테이블에서 사용.

**인증**: `Bearer {accessToken}`

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "문서 버전 목록을 조회했습니다.",
  "data": {
    "documentId": 1,
    "documentName": "스토리보드1",
    "versions": [
      {
        "version": 2,
        "status": "EDITING",
        "changeSummary": "요구사항 3건 수정",
        "createdBy": {
          "firstName": "Seoyeon",
          "lastName": "Kim"
        },
        "createdAt": "2026-08-12T14:30:00"
      },
      {
        "version": 1,
        "status": "TRANSLATED",
        "changeSummary": "스토리보드 최초 작성",
        "createdBy": {
          "firstName": "Seoyeon",
          "lastName": "Kim"
        },
        "createdAt": "2026-08-12T10:00:00"
      }
    ]
  }
}
```

---

## 4. 문서 저장 (전체 저장)

### `PUT /api/documents/{documentId}/versions/{version}`

문서의 특정 버전 전체 데이터를 저장한다.  
페이지, 핀, 요구사항 데이터를 한번에 저장.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만** 가능

#### Request Body
```json
{
  "status": "COMPLETED",
  "changeSummary": "요구사항 3건 수정",
  "pages": [
    {
      "pageNumber": 1,
      "screenName": "회원가입",
      "screenId": "SIGN_UP_01",
      "pins": [
        {
          "pinNumber": 1,
          "xCoordinate": 150.5,
          "yCoordinate": 200.3,
          "requirements": [
            {
              "tabType": "COMMON",
              "itemName": "ID 입력",
              "content": "아이디 중복검사 기능..."
            },
            {
              "tabType": "FRONTEND",
              "itemName": "ID 입력",
              "content": "이메일 형식 검증..."
            }
          ]
        }
      ]
    }
  ]
}
```

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "문서가 저장되었습니다.",
  "data": {
    "documentId": 1,
    "version": 1,
    "status": "COMPLETED",
    "savedAt": "2026-08-12T14:30:00"
  }
}
```

---

## 5. 문서 임시저장

### `POST /api/documents/{documentId}/versions/{version}/auto-save`

작성 중인 문서를 임시저장한다.  
요청 형식은 **전체 저장(PUT)**과 동일하되, `status`가 `DRAFT`로 자동 설정.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만** 가능

#### Request Body
전체 저장과 동일한 형식

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "문서가 임시저장되었습니다.",
  "data": {
    "documentId": 1,
    "version": 1,
    "status": "DRAFT",
    "isAutoSaved": true,
    "savedAt": "2026-08-12T14:25:00"
  }
}
```

---

## 6. 새 버전 생성 (문서 수정 시작)

### `POST /api/documents/{documentId}/versions`

기존 버전을 기반으로 새 버전을 생성한다.  
이전 버전의 페이지/핀/요구사항을 복사하여 새 버전을 만든다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만** 가능

#### Request Body
```json
{
  "baseVersion": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `baseVersion` | Integer | O | 기반이 될 이전 버전 번호 |

#### Response

**201 Created**
```json
{
  "success": true,
  "message": "새 버전이 생성되었습니다.",
  "data": {
    "documentId": 1,
    "newVersion": 2,
    "baseVersion": 1,
    "status": "EDITING",
    "createdAt": "2026-08-12T15:00:00"
  }
}
```

---

## 7. 버전 삭제

### `DELETE /api/documents/{documentId}/versions/{version}`

특정 버전을 삭제한다.  
삭제 후 이전 버전이 최신 버전으로 승격된다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만** 가능

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "버전이 삭제되었습니다.",
  "data": {
    "documentId": 1,
    "deletedVersion": 2,
    "currentLatestVersion": 1
  }
}
```

**400 Bad Request** — 마지막 남은 버전 삭제 시도
```json
{
  "success": false,
  "message": "마지막 버전은 삭제할 수 없습니다.",
  "data": null
}
```
