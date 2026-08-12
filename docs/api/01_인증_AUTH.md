# 인증 API (Auth)

> **Base URL**: `/api/auth`  
> **인증 필요**: 토큰 갱신만 필요, 나머지는 공개

---

## 1. 회원가입

### `POST /api/auth/signup`

새로운 사용자 계정을 생성한다.

**인증**: 불필요

#### Request Body
```json
{
  "firstName": "Seoyeon",
  "lastName": "Kim",
  "email": "seoyeon@example.com",
  "password": "mypassword1",
  "country": "South Korea",
  "language": "ko"
}
```

| 필드 | 타입 | 필수 | 설명 | 제약조건 |
|------|------|------|------|---------|
| `firstName` | String | O | 이름 (영어만) | 영문자만 허용 |
| `lastName` | String | O | 성 (영어만) | 영문자만 허용 |
| `email` | String | O | 이메일 | 이메일 형식 검증 |
| `password` | String | O | 비밀번호 | 8~16자, 영문+숫자 조합 |
| `country` | String | O | 국적 | |
| `language` | String | O | 사용 언어 | `ko`, `en`, `ja`, `zh`, `vi` |

#### Response

**201 Created**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "id": 1,
    "email": "seoyeon@example.com",
    "firstName": "Seoyeon",
    "lastName": "Kim",
    "country": "South Korea",
    "language": "ko",
    "createdAt": "2026-08-12T10:00:00"
  }
}
```

**400 Bad Request** — 유효성 검사 실패
```json
{
  "success": false,
  "message": "8~16자의 영문, 숫자 조합으로 입력해주세요.",
  "data": null
}
```

**409 Conflict** — 이메일 중복
```json
{
  "success": false,
  "message": "해당 아이디는 사용할 수 없습니다.",
  "data": null
}
```

---

## 2. 로그인

### `POST /api/auth/login`

이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받는다.

**인증**: 불필요

#### Request Body
```json
{
  "email": "seoyeon@example.com",
  "password": "mypassword1"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `email` | String | O | 이메일 |
| `password` | String | O | 비밀번호 |

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

| 필드 | 설명 |
|------|------|
| `accessToken` | JWT Access Token (15분 유효) |
| `refreshToken` | JWT Refresh Token (7일 유효) |
| `tokenType` | 토큰 타입 (항상 "Bearer") |
| `expiresIn` | Access Token 만료 시간 (초) |

**401 Unauthorized** — 인증 실패
```json
{
  "success": false,
  "message": "알맞은 비밀번호를 입력해주세요.",
  "data": null
}
```

---

## 3. 토큰 갱신

### `POST /api/auth/refresh`

Refresh Token으로 새로운 Access Token을 발급받는다.

**인증**: 불필요 (Refresh Token을 Body로 전달)

#### Request Body
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "토큰이 갱신되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...(새 토큰)",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...(새 토큰)",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

**401 Unauthorized** — Refresh Token 만료 또는 무효
```json
{
  "success": false,
  "message": "로그인이 만료되었습니다. 다시 로그인해주세요.",
  "data": null
}
```

---

## 공통 에러 응답 형식

모든 API에서 에러 발생 시 아래 형식을 따른다.

```json
{
  "success": false,
  "message": "사용자에게 표시할 메시지",
  "data": null
}
```

---

## 인증 헤더 형식

로그인 필요한 API는 아래 헤더를 포함해야 한다:

```
Authorization: Bearer {accessToken}
```
