# 요구사항 API (Requirement)

> **Base URL**: `/api/pins/{pinId}/requirements`  
> **인증 필요**: 모든 API JWT 필수  
> **권한**: 팀장(LEADER)만 CUD 가능

> **참고**: 요구사항 데이터는 문서 전체 저장(`PUT /api/documents/{documentId}/versions/{version}`)으로도 저장 가능. 아래 API는 **개별 요구사항** 단위 조작이 필요한 경우에 사용.

---

## 1. 요구사항 추가

### `POST /api/pins/{pinId}/requirements`

핀에 요구사항을 추가한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Path Parameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `pinId` | Long | 핀 ID |

#### Request Body
```json
{
  "tabType": "FRONTEND",
  "itemName": "ID 입력",
  "content": "이메일 형식 검증. 형식 아닐 경우 '올바른 이메일 형식이 아닙니다' 메시지 표시"
}
```

| 필드 | 타입 | 필수 | 설명 | 제약조건 |
|------|------|------|------|---------|
| `tabType` | String | O | 탭 유형 | `COMMON`, `PLANNING`, `FRONTEND`, `BACKEND`, `DESIGN` |
| `itemName` | String | O | 항목명 | 최대 10자 |
| `content` | String | O | 요구사항 내용 | 최대 200자 |

#### Response

**201 Created**
```json
{
  "success": true,
  "message": "요구사항이 추가되었습니다.",
  "data": {
    "id": 1,
    "pinId": 1,
    "tabType": "FRONTEND",
    "itemName": "ID 입력",
    "content": "이메일 형식 검증. 형식 아닐 경우 '올바른 이메일 형식이 아닙니다' 메시지 표시",
    "createdAt": "2026-08-12T10:30:00"
  }
}
```

---

## 2. 요구사항 수정

### `PUT /api/pins/{pinId}/requirements/{requirementId}`

기존 요구사항을 수정한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Request Body
```json
{
  "itemName": "ID 입력",
  "content": "이메일 형식 검증 + 중복 검사 추가. 형식 아닐 경우 에러 메시지 표시"
}
```

| 필드 | 타입 | 필수 | 설명 | 제약조건 |
|------|------|------|------|---------|
| `itemName` | String | O | 항목명 | 최대 10자 |
| `content` | String | O | 요구사항 내용 | 최대 200자, 빈칸 불가 |

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "요구사항이 수정되었습니다.",
  "data": {
    "id": 1,
    "pinId": 1,
    "tabType": "FRONTEND",
    "itemName": "ID 입력",
    "content": "이메일 형식 검증 + 중복 검사 추가. 형식 아닐 경우 에러 메시지 표시",
    "updatedAt": "2026-08-12T14:30:00"
  }
}
```

**400 Bad Request** — 빈 내용
```json
{
  "success": false,
  "message": "수정내용을 입력해주세요.",
  "data": null
}
```

---

## 3. 요구사항 삭제

### `DELETE /api/pins/{pinId}/requirements/{requirementId}`

요구사항을 삭제한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Response

**204 No Content** (성공 시 본문 없음)
