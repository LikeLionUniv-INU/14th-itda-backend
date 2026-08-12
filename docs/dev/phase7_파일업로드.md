# Phase 7: 파일 업로드 (S3/MinIO)

> **완료일**: 2026-08-12

---

## 산출물

### 1. S3 설정

**S3Config** (`infra/s3/`)
- `S3Client` Bean — 로컬: MinIO endpoint + pathStyle, 운영: AWS S3
- `S3Presigner` Bean — Presigned URL 생성용, pathStyle 활성화

**S3Service** (`infra/s3/`)
- `generatePresignedUploadUrl()` — PUT용 Presigned URL 생성 (10분 유효)
- `getFileUrl()` — 파일 접근 URL 생성
- `deleteFile()` — S3 파일 삭제
- `extractKeyFromUrl()` — URL에서 S3 key 추출

### 2. 엔티티

| 엔티티 | 설명 |
|--------|------|
| `WireframeImage` | page, imageType, imageUrl, originalWidth/Height, displayWidth/Height, createdAt |

### 3. API

| API | 메서드 | 설명 |
|-----|--------|------|
| `/api/files/presigned-url` | POST | Presigned URL 발급 — 업로드 경로: `wireframes/{projectId}/{documentId}/{pageId}/{type}_{timestamp}.{ext}` |
| `/api/pages/{pageId}/wireframe-images` | POST | 이미지 정보 DB 등록 (201) |
| `/api/pages/{pageId}/wireframe-images/{imageId}` | PUT | 이미지 변경 (기존 S3 파일 삭제 + DB 업데이트) |
| `/api/pages/{pageId}/wireframe-images/{imageId}` | DELETE | 이미지 삭제 (S3 파일 + DB) |

### 4. DTO

**Request:**
- `PresignedUrlRequest` — fileName, contentType, imageType, pageId
- `CreateWireframeImageRequest` — imageType, imageUrl, originalWidth/Height, displayWidth/Height
- `UpdateWireframeImageRequest` — 동일 구조

**Response:**
- `PresignedUrlResponse` — presignedUrl, fileUrl, key
- `WireframeImageResponse` — id, imageType, imageUrl, sizes, createdAt

### 5. 문서 조회 통합

- `DocumentDetailResponse.PageInfo`에 `wireframeImages[]` 필드 추가
- 문서 상세 조회 시 페이지별 와이어프레임 이미지 포함

### 6. 업로드 플로우

```
1. POST /api/files/presigned-url → presignedUrl + fileUrl 반환
2. 프론트에서 presignedUrl로 MinIO/S3에 직접 PUT 업로드
3. POST /api/pages/{pageId}/wireframe-images → fileUrl로 DB 등록
```

---

## 검증 결과

- [x] Presigned URL 발급 → path-style URL (localhost:9000) 확인
- [x] Presigned URL로 MinIO 업로드 → 200
- [x] 이미지 정보 DB 등록 → 201
- [x] 문서 조회 시 wireframeImages 포함 (DESKTOP + MOBILE 2개)
- [x] 이미지 변경 → 200 (기존 S3 파일 삭제 + DB 업데이트)
- [x] 이미지 삭제 → 200 (S3 + DB 모두 삭제)
- [x] 삭제 후 문서 조회 → wireframeImages 1개로 감소 확인
