# 파일 API (File)

> **Base URL**: `/api/files`  
> **인증 필요**: 모든 API JWT 필수

---

## 1. 와이어프레임 이미지 업로드 URL 발급

### `POST /api/files/presigned-url`

S3 Presigned URL을 발급받아 클라이언트에서 직접 S3로 이미지를 업로드할 수 있게 한다.  
서버 부하를 최소화하고 대용량 이미지도 안전하게 처리.

**인증**: `Bearer {accessToken}`

#### Request Body
```json
{
  "fileName": "wireframe_signup.png",
  "contentType": "image/png",
  "imageType": "DESKTOP",
  "pageId": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `fileName` | String | O | 파일명 |
| `contentType` | String | O | MIME 타입 (`image/png`, `image/jpeg`) |
| `imageType` | String | O | 와이어프레임 유형: `DESKTOP` / `MOBILE` |
| `pageId` | Long | O | 업로드 대상 페이지 ID |

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "업로드 URL이 발급되었습니다.",
  "data": {
    "presignedUrl": "https://itda-bucket.s3.ap-northeast-2.amazonaws.com/wireframes/1/1/1/desktop_1723456789.png?X-Amz-Algorithm=...",
    "imageUrl": "https://itda-bucket.s3.ap-northeast-2.amazonaws.com/wireframes/1/1/1/desktop_1723456789.png",
    "expiresIn": 600
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `presignedUrl` | String | S3 업로드용 Presigned URL (10분 유효) |
| `imageUrl` | String | 업로드 완료 후 접근 가능한 이미지 URL (DB 저장용) |
| `expiresIn` | Integer | Presigned URL 유효 시간 (초) |

#### 프론트엔드 업로드 흐름
```
1. POST /api/files/presigned-url → presignedUrl, imageUrl 받음
2. PUT {presignedUrl} (Body: 이미지 파일) → S3에 직접 업로드
3. 업로드 완료 후 imageUrl을 문서 저장 시 wireframeImages에 포함
```

---

## 2. 와이어프레임 이미지 등록 (업로드 완료 알림)

### `POST /api/pages/{pageId}/wireframe-images`

S3 업로드 완료 후 이미지 정보를 DB에 등록한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Request Body
```json
{
  "imageType": "DESKTOP",
  "imageUrl": "https://itda-bucket.s3.../wireframes/1/1/1/desktop_1723456789.png",
  "originalWidth": 1920,
  "originalHeight": 1080,
  "displayWidth": 660,
  "displayHeight": 370
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `imageType` | String | O | `DESKTOP` / `MOBILE` |
| `imageUrl` | String | O | S3 이미지 URL |
| `originalWidth` | Integer | X | 원본 가로 크기 (px) |
| `originalHeight` | Integer | X | 원본 세로 크기 (px) |
| `displayWidth` | Integer | O | 표시 가로 크기 (데스크톱: 660, 모바일: 214) |
| `displayHeight` | Integer | O | 표시 세로 크기 (비율 계산값) |

#### Response

**201 Created**
```json
{
  "success": true,
  "message": "와이어프레임 이미지가 등록되었습니다.",
  "data": {
    "id": 1,
    "pageId": 1,
    "imageType": "DESKTOP",
    "imageUrl": "https://itda-bucket.s3.../wireframes/1/1/1/desktop_1723456789.png",
    "displayWidth": 660,
    "displayHeight": 370,
    "createdAt": "2026-08-12T10:30:00"
  }
}
```

---

## 3. 와이어프레임 이미지 변경

### `PUT /api/pages/{pageId}/wireframe-images/{imageId}`

기존 와이어프레임 이미지를 새 이미지로 교체한다.  
[이미지 변경] 버튼 클릭 시 사용.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Request Body
```json
{
  "imageUrl": "https://itda-bucket.s3.../wireframes/1/1/1/desktop_1723456790.png",
  "originalWidth": 1920,
  "originalHeight": 1080,
  "displayWidth": 660,
  "displayHeight": 370
}
```

#### Response

**200 OK**
```json
{
  "success": true,
  "message": "와이어프레임 이미지가 변경되었습니다.",
  "data": {
    "id": 1,
    "pageId": 1,
    "imageType": "DESKTOP",
    "imageUrl": "https://itda-bucket.s3.../wireframes/1/1/1/desktop_1723456790.png",
    "displayWidth": 660,
    "displayHeight": 370,
    "updatedAt": "2026-08-12T11:00:00"
  }
}
```

---

## 4. 와이어프레임 이미지 삭제

### `DELETE /api/pages/{pageId}/wireframe-images/{imageId}`

와이어프레임 이미지를 삭제한다.

**인증**: `Bearer {accessToken}`  
**권한**: **팀장(LEADER)만**

#### Response

**204 No Content** (성공 시 본문 없음)

---

## 와이어프레임 이미지 규격

| 유형 | 기본 해상도 | 리사이즈 규칙 |
|------|-----------|-------------|
| **데스크톱 (DESKTOP)** | 660 × 370 | 가로 **660px** 고정, 세로는 비율에 맞게 자동 조정 |
| **모바일 (MOBILE)** | 214 × 463 | 가로 **214px** 고정, 세로는 비율에 맞게 자동 조정 |

> 리사이즈는 **프론트엔드에서 처리**. 백엔드는 원본 크기와 표시 크기를 함께 저장하여 프론트에서 활용할 수 있게 한다.
