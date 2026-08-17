# Phase 10: 서버 배포

> **완료일**: 2026-08-17

---

## 배포 전략

| 기간 | 환경 | 비고 |
|------|------|------|
| 8/17(일) ~ 8/18(화) | AWS EC2 (t3.small) | 프론트 로컬 개발 연동용 |
| 8/18(화) ~ 8/28(금) | 가비아 서버 (2vCore/4GB) | 멋사대학 x 가비아 해커톤 서버 |

- EC2 → 가비아 이관 시 동일한 docker-compose.prod.yml + .env 사용
- 가비아 서버에서 도메인 연결 예정

---

## 인프라 구성

```
EC2 / 가비아 서버
├── docker-compose.prod.yml
│   ├── itda-app       (Spring Boot, :8080)
│   ├── itda-postgres   (PostgreSQL 17, :5432)
│   ├── itda-redis      (Redis 7.4, :6379)
│   └── itda-minio      (MinIO S3 호환, :9000/:9001)
└── .env               (환경변수 관리)
```

---

## EC2 인스턴스 설정

| 항목 | 설정값 |
|------|--------|
| AMI | Ubuntu 24.04 LTS |
| 인스턴스 유형 | t3.small (2vCPU / 2GB) |
| 스토리지 | 20GB gp3 |
| 키 페어 | itda-key.pem (RSA) |

### 보안 그룹

| 유형 | 포트 | 소스 | 용도 |
|------|------|------|------|
| SSH | 22 | 내 IP | SSH 접속 |
| TCP | 8080 | 0.0.0.0/0 | Spring Boot API |
| TCP | 9000 | 0.0.0.0/0 | MinIO API (파일 업로드) |
| TCP | 9001 | 내 IP | MinIO 콘솔 (관리용) |

---

## 환경변수 (.env)

```env
DB_URL=jdbc:postgresql://postgres:5432/itda
DB_USERNAME=itda
DB_PASSWORD=<비밀번호>
REDIS_HOST=redis
REDIS_PORT=6379
JWT_SECRET=<openssl rand -base64 64 로 생성>
AWS_ACCESS_KEY=minioadmin
AWS_SECRET_KEY=minioadmin
S3_BUCKET=itda
S3_ENDPOINT=http://minio:9000
S3_PUBLIC_ENDPOINT=http://<서버_공개_IP>:9000
AWS_REGION=ap-northeast-2
OPENAI_API_KEY=<팀 API 키>
```

- `S3_ENDPOINT`: 서버 내부 Docker 네트워크용 (백엔드 → MinIO)
- `S3_PUBLIC_ENDPOINT`: 외부 클라이언트용 (프론트 → MinIO 파일 업로드)

---

## 배포 순서

### 1. 서버 초기 세팅

```bash
# Docker 설치
sudo apt update && sudo apt install -y docker.io docker-compose-v2
sudo usermod -aG docker ubuntu
exit  # 재접속 필요

# 프로젝트 클론
git clone https://github.com/LikeLionUniv-INU/14th-itda-backend.git
cd 14th-itda-backend

# gradlew 실행 권한
chmod +x gradlew
```

### 2. 환경변수 설정

```bash
nano .env
# 위의 환경변수 내용 작성
```

### 3. 빌드 & 실행

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### 4. MinIO 버킷 생성 (최초 1회)

```bash
docker exec itda-minio sh -c "\
  curl -sL https://dl.min.io/client/mc/release/linux-amd64/mc -o /usr/bin/mc && \
  chmod +x /usr/bin/mc && \
  mc alias set local http://localhost:9000 minioadmin minioadmin && \
  mc mb local/itda --ignore-existing && \
  mc anonymous set download local/itda"
```

### 5. 확인

```bash
# 로그 확인 (Started ItdaApplication 나오면 성공)
docker compose -f docker-compose.prod.yml logs -f app

# API 동작 확인
curl http://localhost:8080/swagger-ui/index.html
```

---

## 코드 변경사항

### 1. CORS 설정 추가 (`SecurityConfig.java`)

프론트엔드에서 API 호출 시 브라우저 CORS 정책 대응.

```java
// 모든 Origin 허용, Credentials 포함
configuration.setAllowedOriginPatterns(List.of("*"));
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
configuration.setAllowCredentials(true);
```

### 2. S3 public-endpoint 분리 (`S3Config.java`, `S3Service.java`)

Docker 내부 네트워크에서는 `minio:9000`으로 통신하지만, 프론트엔드가 파일을 업로드할 때는 공개 IP가 필요.

- `S3Client` → 내부 endpoint (`minio:9000`) 사용 (서버→MinIO 통신)
- `S3Presigner` → public-endpoint (`<공개IP>:9000`) 사용 (프론트→MinIO 업로드)
- `S3Service.getFileUrl()` → public-endpoint로 URL 생성

### 3. application-prod.yml 환경변수 추가

```yaml
cloud:
  aws:
    s3:
      bucket: ${S3_BUCKET}
      endpoint: ${S3_ENDPOINT:}
      public-endpoint: ${S3_PUBLIC_ENDPOINT:}
```

---

## 운영 명령어

### 코드 업데이트 반영

```bash
cd ~/14th-itda-backend
git pull

# 코드 변경 시 (Java 소스 수정)
docker compose -f docker-compose.prod.yml build --no-cache app
docker compose -f docker-compose.prod.yml up -d

# 환경변수만 변경 시 (.env 수정)
docker compose -f docker-compose.prod.yml restart app
```

### 로그 확인

```bash
docker compose -f docker-compose.prod.yml logs -f app        # 앱 로그
docker compose -f docker-compose.prod.yml logs -f postgres   # DB 로그
```

### 서비스 상태 확인

```bash
docker compose -f docker-compose.prod.yml ps
```

### 전체 재시작

```bash
docker compose -f docker-compose.prod.yml down
docker compose -f docker-compose.prod.yml up -d
```

---

## API 테스트 결과

### 인증 (3/3)

| API | 결과 |
|-----|------|
| `POST /api/auth/signup` 회원가입 | ✅ |
| `POST /api/auth/login` 로그인 | ✅ |
| `POST /api/auth/refresh` 토큰 갱신 | ✅ |

### 사용자 (1/1)

| API | 결과 |
|-----|------|
| `GET /api/users/me` 내 정보 조회 | ✅ |

### 대시보드 (3/3)

| API | 결과 |
|-----|------|
| `GET /api/dashboard` 홈 탭 | ✅ |
| `GET /api/dashboard/projects` 프로젝트 탭 | ✅ |
| `GET /api/dashboard/documents` 문서 탭 | ✅ |

### 팀 프로젝트 (4/4)

| API | 결과 |
|-----|------|
| `POST /api/teams` 팀 생성 | ✅ |
| `POST /api/teams/join` 팀 참여 | ✅ |
| `GET /api/teams/{id}` 팀 상세 | ✅ |
| `GET /api/teams/{id}/invite-code` 초대코드 | ✅ |

### 문서 관리 (7/7)

| API | 결과 |
|-----|------|
| `POST /api/teams/{id}/documents` 문서 생성 | ✅ |
| `GET /api/documents/{id}/versions/{ver}` 문서 조회 | ✅ |
| `GET /api/documents/{id}/versions/{ver}?lang=en` 번역 조회 | ✅ |
| `PUT /api/documents/{id}/versions/{ver}` 문서 저장 | ✅ |
| `POST .../auto-save` 자동 저장 | ✅ |
| `POST /api/documents/{id}/versions` 새 버전 생성 | ✅ |
| `DELETE .../versions/{ver}` 버전 삭제 | ✅ |

### 페이지 (4/4)

| API | 결과 |
|-----|------|
| `POST .../pages` 페이지 추가 | ✅ |
| `PUT .../pages/{id}` 페이지 수정 | ✅ |
| `DELETE .../pages/{id}` 페이지 삭제 | ✅ |
| `PATCH .../pages/reorder` 순서 변경 | ✅ |

### 핀 (4/4)

| API | 결과 |
|-----|------|
| `POST /api/pages/{id}/pins` 핀 추가 | ✅ |
| `PUT .../pins/{id}` 핀 수정 | ✅ |
| `DELETE .../pins/{id}` 핀 삭제 | ✅ |
| `GET /api/pages/{id}/pins` 핀 목록 | ✅ |

### 요구사항 (3/3)

| API | 결과 |
|-----|------|
| `POST /api/pins/{id}/requirements` 추가 | ✅ |
| `PUT .../requirements/{id}` 수정 | ✅ |
| `DELETE .../requirements/{id}` 삭제 | ✅ |

### 파일 업로드 (4/4)

| API | 결과 |
|-----|------|
| `POST /api/files/presigned-url` URL 발급 | ✅ |
| Presigned URL로 실제 파일 업로드 | ✅ |
| `POST .../wireframe-images` 이미지 등록 | ✅ |
| `PUT .../wireframe-images/{id}` 이미지 수정 | ✅ |
| `DELETE .../wireframe-images/{id}` 이미지 삭제 | ✅ |

### AI 번역 (4/4)

| API | 결과 |
|-----|------|
| `POST .../translate` 번역 요청 | ✅ |
| `GET /api/translations/{id}` 상태 폴링 | ✅ |
| `GET /api/translations/{id}/stream` SSE | ✅ |
| 번역 결과 문서 반영 | ✅ |

### CORS (1/1)

| API | 결과 |
|-----|------|
| OPTIONS preflight 요청 | ✅ |

**총 41개 엔드포인트 전체 정상 동작 확인.**

---

## 가비아 서버 이관 가이드

1. 가비아 서버 SSH 접속
2. 위의 "배포 순서" 동일하게 진행
3. `.env`의 `S3_PUBLIC_ENDPOINT`를 가비아 서버 IP로 변경
4. 도메인 연결 시 Nginx 리버스 프록시 추가 필요

---

## 접속 정보 (EC2)

| 항목 | URL |
|------|-----|
| API 서버 | `http://<EC2_IP>:8080` |
| Swagger UI | `http://<EC2_IP>:8080/swagger-ui/index.html` |
| MinIO 콘솔 | `http://<EC2_IP>:9001` |
| MinIO API | `http://<EC2_IP>:9000` |
