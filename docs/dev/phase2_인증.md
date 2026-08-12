# Phase 2: 인증 (Auth)

> **완료일**: 2026-08-12

---

## 산출물

### 1. User 엔티티

`domain/user/entity/User.java`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK (IDENTITY) |
| email | String | 이메일 (UNIQUE) |
| password | String | BCrypt 해시 비밀번호 |
| firstName | String | 이름 (영문) |
| lastName | String | 성 (영문) |
| country | String | 국적 |
| language | String | 사용 언어 |

- `BaseTimeEntity` 상속 (createdAt, updatedAt)
- `UserRepository`: `findByEmail()`, `existsByEmail()`

---

### 2. RefreshToken 엔티티

`domain/auth/entity/RefreshToken.java`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| user | User | FK (ManyToOne LAZY) |
| token | String | 리프레시 토큰 값 (UNIQUE) |
| expiresAt | LocalDateTime | 만료 일시 |
| createdAt | LocalDateTime | 생성 일시 (@PrePersist) |

- `RefreshTokenRepository`: `findByToken()`, `deleteByUser_Id()`

---

### 3. JWT 토큰

`global/security/JwtTokenProvider.java`

| 메서드 | 설명 |
|--------|------|
| `createAccessToken(userId)` | Access Token 생성 (15분) |
| `createRefreshToken(userId)` | Refresh Token 생성 (7일) |
| `getUserId(token)` | 토큰에서 userId 추출 |
| `validateToken(token)` | 토큰 유효성 검증 |

- subject에 userId만 포함 (role은 팀별이므로 토큰에 넣지 않음)
- HS512 알고리즘 사용

---

### 4. Spring Security

`global/config/SecurityConfig.java`

**permitAll 경로:**
- `/api/auth/signup`, `/api/auth/login`, `/api/auth/refresh`
- `/swagger-ui/**`, `/v3/api-docs/**`

**인증 실패 응답:**
- 401: `{ success: false, message: "로그인이 필요합니다." }`
- 403: `{ success: false, message: "접근 권한이 없습니다." }`

`global/security/JwtAuthenticationFilter.java`
- `Authorization: Bearer {token}` 헤더에서 토큰 추출
- 유효한 토큰 → SecurityContext에 userId 설정

---

### 5. API 목록

| API | 메서드 | 설명 |
|-----|--------|------|
| `/api/auth/signup` | POST | 회원가입 (201 Created) |
| `/api/auth/login` | POST | 로그인 (200 OK + 토큰) |
| `/api/auth/refresh` | POST | 토큰 갱신 (200 OK + 새 토큰) |

---

### 6. DTO

**Request:**
- `SignupRequest` — firstName, lastName, email, password, country, language (Bean Validation)
- `LoginRequest` — email, password
- `RefreshRequest` — refreshToken

**Response:**
- `SignupResponse` — id, email, firstName, lastName, country, language, createdAt
- `TokenResponse` — accessToken, refreshToken, tokenType("Bearer"), expiresIn(초)

---

### 7. 유효성 검증 규칙

| 필드 | 규칙 |
|------|------|
| firstName/lastName | 영문자만 (`^[a-zA-Z]+$`) |
| email | 이메일 형식 (`@Email`) |
| password | 8~16자 영문+숫자 조합 |

---

## 검증 결과

- [x] 회원가입 → 201 정상
- [x] 이메일 중복 → 409 정상
- [x] 유효성 실패 (비밀번호) → 400 정상
- [x] 로그인 → 200 + 토큰 발급 정상
- [x] 잘못된 비밀번호 → 401 정상
- [x] 토큰 없이 보호 API 접근 → 401 정상
- [x] 토큰 갱신 → 200 + 새 토큰 발급 정상 (Rotation)
