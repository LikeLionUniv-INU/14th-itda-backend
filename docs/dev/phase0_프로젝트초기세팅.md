# Phase 0: 프로젝트 초기 세팅

> **완료일**: 2026-08-12

---

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 21 LTS (Temurin) |
| Spring Boot | 3.3.2 |
| Gradle | 8.9 (Wrapper) |
| PostgreSQL | 17 |
| Redis | 7.4 |
| MinIO | latest (S3 호환) |

---

## 의존성 목록

| 의존성 | 용도 |
|--------|------|
| spring-boot-starter-web | REST API |
| spring-boot-starter-data-jpa | JPA/Hibernate ORM |
| spring-boot-starter-security | Spring Security |
| spring-boot-starter-validation | Bean Validation |
| spring-boot-starter-data-redis | Redis 연동 |
| postgresql | PostgreSQL JDBC 드라이버 |
| flyway-core / flyway-database-postgresql | DB 마이그레이션 |
| jjwt-api / impl / jackson (0.12.6) | JWT 토큰 |
| springdoc-openapi-starter-webmvc-ui (2.6.0) | Swagger UI |
| aws-sdk-s3 (2.27.0) | S3/MinIO 파일 업로드 |
| lombok | 보일러플레이트 제거 |
| spring-boot-starter-test | JUnit 5 + Mockito |
| spring-security-test | Security 테스트 |
| testcontainers (postgresql) | 통합 테스트용 컨테이너 |

---

## 패키지 구조

```
com.itda
├── ItdaApplication.java
├── domain/
│   ├── auth/
│   ├── user/
│   ├── team/
│   ├── document/
│   ├── page/
│   ├── pin/
│   ├── requirement/
│   └── translation/
│       각 도메인 하위:
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
│           ├── request/
│           └── response/
├── global/
│   ├── config/       — 설정 클래스 (Security, JPA, Swagger 등)
│   ├── error/        — 예외 클래스, GlobalExceptionHandler
│   ├── common/       — ApiResponse 등 공통 클래스
│   └── security/     — JWT 관련 (TokenProvider, Filter)
└── infra/
    ├── s3/           — S3/MinIO 클라이언트
    ├── redis/        — Redis 설정
    └── ai/           — OpenAI 클라이언트
```

---

## 설정 파일

### application.yml (공통)
- JPA: `ddl-auto: validate` (Flyway가 스키마 관리)
- `open-in-view: false`
- Flyway: `classpath:db/migration`
- SpringDoc: `/swagger-ui.html`, `/v3/api-docs`

### application-local.yml
- PostgreSQL: `localhost:5432/itda` (itda/itda1234)
- Redis: `localhost:6379`
- JWT: 로컬 개발용 시크릿 키 (Access 15분 / Refresh 7일)
- MinIO: `localhost:9000` (minioadmin/minioadmin), 버킷 `itda`
- OpenAI: 환경변수 `OPENAI_API_KEY` (기본값 placeholder)

### application-prod.yml
- 모든 민감정보 환경변수 처리 (`${DB_URL}`, `${JWT_SECRET}`, `${AWS_ACCESS_KEY}` 등)

---

## Docker Compose

```bash
docker compose up -d
```

| 서비스 | 컨테이너명 | 포트 |
|--------|-----------|------|
| PostgreSQL 17 | itda-postgres | 5432 |
| Redis 7.4 | itda-redis | 6379 |
| MinIO | itda-minio | 9000 (API) / 9001 (Console) |

- MinIO Console: http://localhost:9001 (minioadmin / minioadmin)

---

## Dockerfile

멀티스테이지 빌드:
1. **Build stage**: `eclipse-temurin:21-jdk` — Gradle 빌드, bootJar 생성
2. **Run stage**: `eclipse-temurin:21-jre` — JAR 실행만

---

## 검증 결과

- [x] `docker compose up -d` — PostgreSQL/Redis/MinIO 정상 기동
- [x] `./gradlew compileJava` — 빌드 성공
- [x] `./gradlew bootRun` — Spring Boot 정상 기동
- [x] Swagger UI (`/swagger-ui.html`) — HTTP 302 리다이렉트 정상
- [x] API Docs (`/v3/api-docs`) — HTTP 200 정상
