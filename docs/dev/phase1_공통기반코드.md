# Phase 1: 공통 기반 코드

> **완료일**: 2026-08-12

---

## 산출물

### 1. ApiResponse\<T\> Record

`global/common/ApiResponse.java`

```java
public record ApiResponse<T>(boolean success, String message, T data)
```

- `ApiResponse.ok(message, data)` — 성공 응답
- `ApiResponse.error(message)` — 실패 응답 (data: null)

---

### 2. 커스텀 예외 클래스

`global/error/` 패키지

| 예외 클래스 | HTTP 상태 | 용도 |
|------------|----------|------|
| `BusinessException` | (추상 베이스) | 모든 비즈니스 예외의 부모 |
| `NotFoundException` | 404 | 리소스 없음 |
| `DuplicateException` | 409 | 중복 데이터 |
| `UnauthorizedException` | 401 | 인증 실패 |
| `ForbiddenException` | 403 | 권한 없음 |
| `ValidationException` | 400 | 비즈니스 유효성 검사 실패 |

---

### 3. GlobalExceptionHandler

`global/error/GlobalExceptionHandler.java`

| 처리 대상 | HTTP 상태 | 응답 형식 |
|----------|----------|----------|
| `BusinessException` | 예외별 상태 코드 | `ApiResponse.error(message)` |
| `MethodArgumentNotValidException` | 400 | 첫 번째 필드 에러 메시지 |
| `Exception` (기타) | 500 | "서버 내부 오류가 발생했습니다." + 로그 |

---

### 4. BaseTimeEntity

`global/common/BaseTimeEntity.java`

- `@MappedSuperclass` — 모든 엔티티가 상속
- `createdAt` (`@CreatedDate`, updatable=false)
- `updatedAt` (`@LastModifiedDate`)
- JPA Auditing 활성화: `global/config/JpaConfig.java`

---

### 5. Flyway V1 마이그레이션

`resources/db/migration/V1__init.sql`

생성된 테이블 (13개):

| 테이블 | 설명 |
|--------|------|
| users | 사용자 |
| refresh_tokens | 리프레시 토큰 |
| team_projects | 팀 프로젝트 |
| team_members | 팀 멤버 |
| documents | 문서 |
| document_versions | 문서 버전 |
| pages | 페이지 |
| wireframe_images | 와이어프레임 이미지 |
| pins | 핀 |
| requirements | 요구사항 |
| translation_jobs | 번역 작업 |
| translation_languages | 번역 언어별 상태 |
| translated_requirements | 번역된 요구사항 |

---

## 검증 결과

- [x] Flyway V1 마이그레이션 → 13개 테이블 생성
- [x] Spring Boot 정상 기동
- [x] Swagger UI 접근 정상
