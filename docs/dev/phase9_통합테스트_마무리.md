# Phase 9: 통합 테스트 및 마무리

> **완료일**: 2026-08-12

---

## 산출물

### 1. Swagger 문서 정비

**OpenApiConfig** (`global/config/`)
- OpenAPI 3.0 설정: 제목, 설명, 버전
- JWT Bearer Authentication SecurityScheme 설정

**컨트롤러 어노테이션** (10개 컨트롤러)

| 컨트롤러 | @Tag |
|----------|------|
| AuthController | 인증 |
| UserController | 사용자 |
| DashboardController | 대시보드 |
| TeamController | 팀 프로젝트 |
| DocumentController | 문서 관리 |
| PageController | 페이지 |
| PinController | 핀 |
| RequirementController | 요구사항 |
| FileController | 파일 업로드 |
| TranslationController | AI 번역 |

- 모든 API 메서드에 `@Operation(summary, description)` 추가
- Swagger UI (`/swagger-ui/index.html`) 접근 확인: 28개 API, 10개 태그

### 2. 코드 품질

- 환경변수 분리 확인: `application-prod.yml`의 모든 민감정보(DB, Redis, JWT, AWS, OpenAI)가 `${}` 환경변수 참조
- `application-local.yml`의 OpenAI API 키도 `${OPENAI_API_KEY:sk-placeholder}`로 환경변수 우선

### 3. 전체 플로우 E2E 테스트

---

## 검증 결과

### 시나리오 1: 팀장 전체 플로우

- [x] 회원가입 → 201
- [x] 로그인 → 200, JWT 토큰 발급
- [x] 팀 프로젝트 생성 → 201, 초대 코드 발급
- [x] 문서 생성 → 201
- [x] 문서 전체 저장 (페이지+핀+요구사항 5탭) → 200
- [x] 문서 상세 조회 → 200, 1 page, 1 pin, 5 requirements
- [x] AI 번역 요청 → 202, 비동기 번역 완료
- [x] 번역 상태 폴링 → 200, COMPLETED, 100%
- [x] 번역된 문서 조회 (?lang=en) → 200, 영어로 번역된 요구사항 확인
- [x] 새 버전 생성 → 201, version 2

### 시나리오 2: 팀원 플로우

- [x] 대시보드 조회 → 200, projects 1개, recentDocuments 1개
- [x] 팀 프로젝트 상세 조회 → 200, myRole: MEMBER, members 2명
- [x] 번역된 문서 조회 (?lang=en) → 200, 번역된 텍스트
- [x] 문서 생성 시도 → 403 거부
- [x] 버전 목록 조회 → 200, version 1(TRANSLATED), version 2(EDITING)

### 시나리오 3: 에러 케이스

- [x] 잘못된 이메일 형식 회원가입 → 400
- [x] 영어 외 이름 입력 → 400
- [x] 이메일 중복 → 409
- [x] 잘못된 비밀번호 로그인 → 401
- [x] 잘못된 토큰 → 401
- [x] 비멤버 팀 조회 → 403
- [x] 잘못된 초대 코드 → 404
- [x] 마지막 버전 삭제 → 400
- [x] 빈 content 수정 → 400

### Swagger UI

- [x] `/swagger-ui/index.html` 접근 → 200
- [x] 전체 28개 API 경로 확인
- [x] 10개 태그 분류 확인
- [x] JWT Bearer Authentication 스키마 설정
