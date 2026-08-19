# TODO 리스트

> 해커톤 마감: 2026-08-28(금)
> 서버: https://docbridge.cloud (EC2: 13.209.171.51)
> Swagger: https://docbridge.cloud/swagger-ui/index.html

---

## 완료

- [x] **탭별 독립 핀 세트 구현** (2026-08-17)
- [x] **프론트엔드 API 연동 가이드 정비** (2026-08-17)
- [x] **ERD 최신화** (2026-08-17)
- [x] **채점용 테스트 계정 + 더미 데이터** (2026-08-19)
  - 4개 계정 (korea/english/member1/member2 @itda.com, 비번 test1234)
  - 팀 프로젝트 2개, 문서 3개, 버전 7개, 페이지 26개
  - 와이어프레임 이미지 13개 (MinIO), 핀 31개, 요구사항 40개
  - 번역 완료 (영어+일본어), 수정사항 4건, 알림, 활동 로그
- [x] **탄력적 IP 할당** (2026-08-19) → 13.209.171.51
- [x] **도메인 구매** (2026-08-19) → docbridge.cloud (가비아)
- [x] **Nginx + HTTPS** (2026-08-19) → Let's Encrypt SSL, 자동갱신
- [x] **HTTPS 전환 후 설정 변경** (2026-08-19) → S3_PUBLIC_ENDPOINT 도메인 적용
- [x] **CD 파이프라인 구축** (2026-08-19) → GitHub Actions + GHCR, main push 시 자동배포
- [x] **프론트용 더미 데이터 가이드** (2026-08-19) → docs/dummy-data-guide.md

---

## 남은 작업

- [ ] **프론트엔드 노션 API 가이드 최종 반영** (프론트 담당)
  - tabType 한글 값, 핀 tabType 필드 추가 등
