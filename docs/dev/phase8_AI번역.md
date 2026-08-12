# Phase 8: AI 번역

> **완료일**: 2026-08-12

---

## 산출물

### 1. 번역 엔티티

| 엔티티 | 설명 |
|--------|------|
| `TranslationJob` | documentVersion, status, totalLanguages, completedLanguages, createdAt, completedAt |
| `TranslationLanguage` | translationJob, targetLanguage, targetUser, status, createdAt, completedAt |
| `TranslatedRequirement` | translationLanguage, requirement, translatedItemName, translatedContent, createdAt |

### 2. AI 번역 클라이언트

**AiTranslationClient** (`infra/ai/`)
- OpenAI GPT API 연동 (RestClient 사용)
- 시스템 프롬프트: 기술 문서 번역 맥락 설정
- 요청: 요구사항 목록(JSON) → 번역된 결과(JSON) 반환
- temperature 0.3으로 일관된 번역 품질 확보
- 마크다운 코드블록 자동 제거 처리

### 3. Redis Pub/Sub 설정

**RedisConfig** (`infra/redis/`)
- `StringRedisTemplate` Bean
- `RedisMessageListenerContainer` Bean — SSE 이벤트 수신용
- 채널명: `translation:{jobId}`

### 4. 번역 서비스

**TranslationService** (`domain/translation/service/`)
- `requestTranslation()` — 번역 Job 생성, `@Async`로 비동기 번역 시작
- `executeTranslationAsync()` — 언어별 순차 번역, 완료 시 Redis Pub/Sub 이벤트 발행
- `getJobStatus()` — 폴링 방식 상태 조회
- 번역 완료 시 `DocumentVersion.status = TRANSLATED`로 변경

### 5. API

| API | 메서드 | 설명 |
|-----|--------|------|
| `/api/documents/{documentId}/versions/{version}/translate` | POST | 번역 요청 — 202 Accepted |
| `/api/translations/{jobId}/stream` | GET | SSE 실시간 스트리밍 (5분 타임아웃) |
| `/api/translations/{jobId}` | GET | 폴링 방식 상태 조회 |

### 6. DTO

**Request:**
- `TranslateRequest` — translations[{userId, targetLanguage}]

**Response:**
- `TranslationJobResponse` — jobId, status, totalLanguages, completedLanguages, progress, languages[]

### 7. 문서 조회 번역 통합

- `GET /api/documents/{documentId}/versions/{version}?lang=en` 지원
- `lang` 파라미터 있으면 → `translated_requirements`에서 해당 언어 데이터로 대체
- 없으면 → 원본 `requirements` 반환

### 8. SSE 이벤트 종류

| 이벤트 | 설명 |
|--------|------|
| `language-status` | 언어별 번역 완료 시 — language, status, completedLanguages, totalLanguages, progress |
| `translation-complete` | 전체 번역 완료 — status, progress(100) |
| `translation-error` | 번역 오류 발생 — language, message |

### 9. 비동기 처리 설정

**AsyncConfig** (`global/config/`)
- `@EnableAsync` 설정
- `TranslationService.executeTranslationAsync()`에 `@Async` 적용

---

## 번역 플로우

```
1. POST /api/documents/{id}/versions/{v}/translate → 202 + jobId
2. 프론트에서 GET /api/translations/{jobId}/stream (SSE 연결)
3. 백엔드: 언어별 순차 번역 (OpenAI GPT API)
4. 각 언어 완료 시 → Redis Pub/Sub → SSE로 language-status 이벤트 전송
5. 전체 완료 시 → translation-complete 이벤트 + DocumentVersion.status = TRANSLATED
6. 프론트: GET /api/documents/{id}/versions/{v}?lang=en → 번역된 문서 조회
```
