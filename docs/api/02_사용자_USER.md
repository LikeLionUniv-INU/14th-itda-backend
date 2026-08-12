# 사용자 API (User)

> **Base URL**: `/api/users`  
> **인증 필요**: 모든 API JWT 필수

---

## 1. 내 정보 조회

### `GET /api/users/me`

현재 로그인한 사용자의 프로필 정보를 조회한다.  
메인 대시보드 상단의 배너("안녕하세요, OOO님")와 이니셜 표시에 사용.

**인증**: `Bearer {accessToken}`

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "사용자 정보를 조회했습니다.",
  "data": {
    "id": 1,
    "email": "seoyeon@example.com",
    "firstName": "Seoyeon",
    "lastName": "Kim",
    "country": "South Korea",
    "language": "ko",
    "initial": "K",
    "createdAt": "2026-08-12T10:00:00"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 사용자 ID |
| `email` | String | 이메일 |
| `firstName` | String | 이름 |
| `lastName` | String | 성 |
| `country` | String | 국적 |
| `language` | String | 사용 언어 |
| `initial` | String | 성의 이니셜 (예: "Kim" → "K") |
| `createdAt` | String | 가입일시 (ISO 8601) |

**401 Unauthorized** — 토큰 없음/만료
```json
{
  "success": false,
  "message": "로그인이 필요합니다.",
  "data": null
}
```
