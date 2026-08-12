# 핀 API (Pin)

> **Base URL**: `/api/pages/{pageId}/pins`  
> **인증 필요**: 모든 API JWT 필수  
> **권한**: 팀장(LEADER)만 CUD 가능

> **참고**: 핀 데이터는 문서 전체 저장(`PUT /api/documents/{documentId}/versions/{version}`)으로도 저장 가능. 아래 API는 **개별 핀** 단위 조작이 필요한 경우에 사용.

---

## 1. 핀 추가

### `POST /api/pages/{pageId}/pins`

와이어프레임 이미지 위에 새 핀을 추가한다.  
핀 번호는 **자동 할당** — 해당 페이지의 기존 핀 번호 다음 번호가 부여된다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Path Parameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `pageId` | Long | 페이지 ID |

#### Request Body
```json
{
  "xCoordinate": 150.5,
  "yCoordinate": 200.3
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `xCoordinate` | Double | O | X 좌표 (이미지 내 상대 좌표) |
| `yCoordinate` | Double | O | Y 좌표 (이미지 내 상대 좌표) |

#### Response

**201 Created**
```json
{
  "success": true,
  "message": "핀이 추가되었습니다.",
  "data": {
    "id": 1,
    "pinNumber": 1,
    "xCoordinate": 150.5,
    "yCoordinate": 200.3,
    "createdAt": "2026-08-12T10:30:00"
  }
}
```

---

## 2. 핀 위치 수정

### `PUT /api/pages/{pageId}/pins/{pinId}`

핀의 좌표를 수정한다. (드래그로 위치 이동 시)

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Request Body
```json
{
  "xCoordinate": 180.0,
  "yCoordinate": 220.5
}
```

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "핀 위치가 수정되었습니다.",
  "data": {
    "id": 1,
    "pinNumber": 1,
    "xCoordinate": 180.0,
    "yCoordinate": 220.5,
    "updatedAt": "2026-08-12T10:35:00"
  }
}
```

---

## 3. 핀 삭제

### `DELETE /api/pages/{pageId}/pins/{pinId}`

핀과 해당 핀의 모든 요구사항을 삭제한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Response

**204 No Content** (성공 시 본문 없음)

---

## 4. 페이지 내 핀 전체 조회

### `GET /api/pages/{pageId}/pins`

특정 페이지의 모든 핀과 요구사항을 조회한다.

**인증**: `Bearer {accessToken}`

#### Query Parameter

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `tabType` | String | X | 특정 탭의 요구사항만 필터링: `COMMON`, `PLANNING`, `FRONTEND`, `BACKEND`, `DESIGN` |

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "핀 목록을 조회했습니다.",
  "data": {
    "pageId": 1,
    "pins": [
      {
        "id": 1,
        "pinNumber": 1,
        "xCoordinate": 150.5,
        "yCoordinate": 200.3,
        "requirements": {
          "COMMON": [
            { "id": 1, "itemName": "ID 입력", "content": "아이디 중복검사 기능..." }
          ],
          "PLANNING": [],
          "FRONTEND": [
            { "id": 2, "itemName": "ID 입력", "content": "이메일 형식 검증..." }
          ],
          "BACKEND": [
            { "id": 3, "itemName": "ID 입력", "content": "이메일 중복 체크..." }
          ],
          "DESIGN": []
        }
      },
      {
        "id": 2,
        "pinNumber": 2,
        "xCoordinate": 300.0,
        "yCoordinate": 250.0,
        "requirements": {
          "COMMON": [],
          "PLANNING": [],
          "FRONTEND": [
            { "id": 4, "itemName": "비밀번호", "content": "8~16자 영문+숫자..." }
          ],
          "BACKEND": [],
          "DESIGN": []
        }
      }
    ]
  }
}
```
