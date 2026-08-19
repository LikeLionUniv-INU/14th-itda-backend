# 더미 데이터 가이드 (프론트엔드용)

## 주요 URL

| 용도 | URL |
|---|---|
| API 서버 | `https://docbridge.cloud` |
| Swagger UI | `https://docbridge.cloud/swagger-ui/index.html` |

모든 API는 `https://docbridge.cloud/api/...` 형태입니다.

---

## 테스트 계정

모든 비밀번호: `test1234`

| 이메일 | 이름 | 언어 | 역할 (DocBridge MVP) | 역할 (Global Shopping App) |
|---|---|---|---|---|
| `korea@itda.com` | Minjun Kim | 한국어 | **LEADER** | MEMBER |
| `english@itda.com` | Sarah Johnson | 영어 | MEMBER | **LEADER** |
| `member1@itda.com` | Yuto Tanaka | 일본어 | MEMBER | - |
| `member2@itda.com` | Wei Chen | 중국어 | - | MEMBER |

### 로그인 예시

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "korea@itda.com",
  "password": "test1234"
}
```

응답의 `data.accessToken`을 이후 모든 요청의 헤더에 사용:

```
Authorization: Bearer {accessToken}
```

---

## 팀 프로젝트

| ID | 이름 | 기본 언어 | 초대 코드 | 멤버 |
|---|---|---|---|---|
| 101 | DocBridge MVP | ko | `DOC101` | Kim(LEADER), Johnson(MEMBER), Tanaka(MEMBER) |
| 102 | Global Shopping App | en | `SHP102` | Johnson(LEADER), Kim(MEMBER), Chen(MEMBER) |

---

## 문서 구조

### 프로젝트 101 (DocBridge MVP)

#### 문서 101: 메인화면 (STORYBOARD)

| 버전 | 상태 | 설명 |
|---|---|---|
| v1 | COMPLETED | 초기 스토리보드 (3 페이지) |
| v2 | COMPLETED | 레이아웃 변경 (4 페이지, **번역 완료**) |
| v3 | DRAFT | 수정 중 (4 페이지) |

**v2 페이지 구성:**

| 페이지 | screenName | screenId | 이미지 | 핀 수 |
|---|---|---|---|---|
| 1 | 홈 | S-001 | ✅ | 4 (공통, 기획, 프론트엔드, 백엔드) |
| 2 | 검색 | S-002 | ✅ | 3 (공통, 디자인, 프론트엔드) |
| 3 | 마이페이지 | S-003 | ✅ | 2 (백엔드, 공통) |
| 4 | 알림 | S-004 | ✅ | 2 (기획, 프론트엔드) |

#### 문서 102: 로그인 (WIREFRAME)

| 버전 | 상태 | 설명 |
|---|---|---|
| v1 | COMPLETED | 로그인/회원가입 (3 페이지) |
| v2 | DRAFT | 소셜 로그인 추가 (4 페이지) |

**v2 페이지 구성:**

| 페이지 | screenName | screenId | 이미지 | 핀 수 |
|---|---|---|---|---|
| 1 | 로그인 | L-001 | ✅ | 3 (공통, 프론트엔드, 백엔드) |
| 2 | 회원가입 | L-002 | ✅ | 3 (기획, 공통, 백엔드) |
| 3 | 비번찾기 | L-003 | ✅ | 0 |
| 4 | 소셜로그인 | L-004 | ✅ | 2 (프론트엔드, 백엔드) |

### 프로젝트 102 (Global Shopping App)

#### 문서 103: Product (STORYBOARD)

| 버전 | 상태 | 설명 |
|---|---|---|
| v1 | COMPLETED | 초기 레이아웃 (3 페이지) |
| v2 | COMPLETED | 리뷰+결제 추가 (5 페이지) |

**v2 페이지 구성:**

| 페이지 | screenName | screenId | 이미지 | 핀 수 |
|---|---|---|---|---|
| 1 | ProdList | P-001 | ✅ | 3 (공통, 디자인, 프론트엔드) |
| 2 | ProductDtl | P-002 | ✅ | 4 (공통, 기획, 프론트엔드, 백엔드) |
| 3 | Cart | P-003 | ✅ | 0 |
| 4 | Reviews | P-004 | ✅ | 2 (공통, 프론트엔드) |
| 5 | Checkout | P-005 | ✅ | 3 (기획, 백엔드, 공통) |

---

## 탭별 핀 필터링

핀은 5가지 탭으로 분류됩니다. 쿼리 파라미터로 필터링:

```
GET /api/pages/{pageId}/pins              → 전체 핀
GET /api/pages/{pageId}/pins?tabType=공통  → 공통 탭 핀만
```

| tabType | 설명 |
|---|---|
| 공통 | 모든 직군 공통 |
| 기획 | 기획자용 |
| 디자인 | 디자이너용 |
| 프론트엔드 | 프론트엔드용 |
| 백엔드 | 백엔드용 |

---

## 번역 데이터

문서 101(메인화면) v2에 영어/일본어 번역이 완료되어 있습니다.

```
GET /api/documents/101/versions/2          → 원본 (한국어)
GET /api/documents/101/versions/2?lang=en  → 영어 번역
GET /api/documents/101/versions/2?lang=ja  → 일본어 번역
```

번역 조회 시 요구사항의 `itemName`, `content`가 해당 언어로 바뀌어 응답됩니다.

### 번역 상태 조회

```
GET /api/translations/101
```

```json
{
  "jobId": 101,
  "status": "COMPLETED",
  "totalLanguages": 2,
  "completedLanguages": 2,
  "progress": 100,
  "languages": [
    { "targetLanguage": "en", "status": "COMPLETED" },
    { "targetLanguage": "ja", "status": "COMPLETED" }
  ]
}
```

---

## 수정사항 (Change Tracking)

문서 101 v2에 4건의 수정사항이 있습니다:

```
GET /api/documents/101/versions/2/changes
```

| 유형 | 설명 | 확인 상태 |
|---|---|---|
| REQUIREMENT_ADDED | 알림 페이지 요구사항 추가 | Sarah 확인 |
| REQUIREMENT_MODIFIED | 홈 헤더 요구사항 수정 | Sarah 확인 |
| SCREEN_MODIFIED | 검색 화면 이름 변경 | 미확인 |
| REQUIREMENT_DELETED | 마이페이지 요구사항 삭제 | 미확인 |

---

## 알림 (Notifications)

```
GET /api/teams/{teamId}/notifications
```

KO 유저(Kim)는 본인이 수행한 알림이므로 빈 배열이 반환됩니다.
EN 유저(Sarah)로 로그인하면 팀 101의 미읽은 알림을 확인할 수 있습니다.

---

## 와이어프레임 이미지

모든 페이지에 플레이스홀더 이미지가 등록되어 있습니다.
이미지 URL은 문서 상세 조회 응답의 `pages[].wireframeImages[].imageUrl`에 포함됩니다.

이미지 직접 접근 예시:
```
https://docbridge.cloud/storage/itda/wireframes/101/101/104/original_1723900000000.png
```

---

## 자주 쓰는 API 흐름

### 1. 로그인 → 대시보드

```
POST /api/auth/login          → accessToken 획득
GET  /api/dashboard           → 프로젝트 목록 + 최근 문서
```

### 2. 팀 프로젝트 진입

```
GET /api/teams/101            → 팀 상세 (멤버, 문서 목록, 활동 로그)
GET /api/teams/101/invite-code → 초대 코드 조회
```

### 3. 문서 편집 화면

```
GET /api/documents/101/versions/2   → 페이지 + 이미지 + 핀 + 요구사항 전체
GET /api/pages/104/pins?tabType=공통 → 특정 탭 핀만 필터링
```

### 4. 번역 조회

```
GET /api/documents/101/versions/2?lang=en  → 영어 번역된 문서
GET /api/translations/101                  → 번역 작업 상태
```

### 5. 팀 초대

```
POST /api/teams/join
{ "inviteCode": "DOC101" }
```
