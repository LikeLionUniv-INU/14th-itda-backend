# 페이지 API (Page)

> **Base URL**: `/api/documents/{documentId}/versions/{version}/pages`  
> **인증 필요**: 모든 API JWT 필수  
> **권한**: 팀장(LEADER)만 CUD 가능

> **참고**: 페이지 데이터는 문서 전체 저장(`PUT /api/documents/{documentId}/versions/{version}`)으로도 저장 가능. 아래 API는 **개별 페이지** 단위 조작이 필요한 경우에 사용.

---

## 1. 페이지 추가

### `POST /api/documents/{documentId}/versions/{version}/pages`

문서에 새 페이지를 추가한다. [+ 페이지 추가] 버튼 클릭 시 사용.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Request Body
```json
{
  "screenName": "로그인",
  "screenId": "LOGIN_001"
}
```

| 필드 | 타입 | 필수 | 설명 | 제약조건 |
|------|------|------|------|---------|
| `screenName` | String | X | 화면 이름 | 최대 10자 |
| `screenId` | String | X | 화면 ID | 최대 10자 |

#### Response

**201 Created**
```json
{
  "success": true,
  "message": "페이지가 추가되었습니다.",
  "data": {
    "id": 2,
    "pageNumber": 2,
    "screenName": "로그인",
    "screenId": "LOGIN_001",
    "wireframeImages": [],
    "pins": [],
    "createdAt": "2026-08-12T10:30:00"
  }
}
```

---

## 2. 페이지 수정

### `PUT /api/documents/{documentId}/versions/{version}/pages/{pageId}`

페이지의 화면 정보를 수정한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Request Body
```json
{
  "screenName": "로그인 화면",
  "screenId": "LOGIN_001"
}
```

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "페이지가 수정되었습니다.",
  "data": {
    "id": 2,
    "pageNumber": 2,
    "screenName": "로그인 화면",
    "screenId": "LOGIN_001",
    "updatedAt": "2026-08-12T10:35:00"
  }
}
```

---

## 3. 페이지 삭제

### `DELETE /api/documents/{documentId}/versions/{version}/pages/{pageId}`

페이지를 삭제한다. 하위 와이어프레임/핀/요구사항도 함께 삭제.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Response

**204 No Content** (성공 시 본문 없음)

---

## 4. 페이지 순서 변경

### `PATCH /api/documents/{documentId}/versions/{version}/pages/reorder`

페이지의 순서를 변경한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Request Body
```json
{
  "pageOrders": [
    { "pageId": 1, "pageNumber": 1 },
    { "pageId": 3, "pageNumber": 2 },
    { "pageId": 2, "pageNumber": 3 }
  ]
}
```

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "페이지 순서가 변경되었습니다.",
  "data": {
    "pages": [
      { "pageId": 1, "pageNumber": 1, "screenName": "회원가입" },
      { "pageId": 3, "pageNumber": 2, "screenName": "메인화면" },
      { "pageId": 2, "pageNumber": 3, "screenName": "로그인" }
    ]
  }
}
```
