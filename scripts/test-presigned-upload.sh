#!/bin/bash
# ==============================================
# Presigned URL 이미지 업로드 E2E 테스트
# 프론트엔드와 동일한 플로우로 검증
# ==============================================

BASE_URL="https://docbridge.cloud"
PASS=0
FAIL=0

echo "===== Presigned URL 업로드 테스트 ====="
echo ""

# 1. 로그인 (리더 계정)
echo "[1/6] 로그인..."
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"korea@itda.com","password":"test1234"}')

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "  FAIL: 로그인 실패"
  echo "$LOGIN_RESP"
  exit 1
fi
echo "  OK: 토큰 획득"
PASS=$((PASS+1))

# 2. 기존 이미지 조회 (기존 기능 깨지지 않았는지 확인)
echo ""
echo "[2/6] 기존 이미지 GET 테스트 (regression check)..."
EXISTING_IMG_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  "$BASE_URL/storage/itda/wireframes/101/101/104/original_1723900000000.png")

if [ "$EXISTING_IMG_STATUS" = "200" ]; then
  echo "  OK: 기존 이미지 조회 정상 (HTTP $EXISTING_IMG_STATUS)"
  PASS=$((PASS+1))
else
  echo "  FAIL: 기존 이미지 조회 실패 (HTTP $EXISTING_IMG_STATUS)"
  FAIL=$((FAIL+1))
fi

# 3. Presigned URL 발급
echo ""
echo "[3/6] Presigned URL 발급..."
PRESIGNED_RESP=$(curl -s -X POST "$BASE_URL/api/files/presigned-url" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"fileName":"test-upload.png","contentType":"image/png","imageType":"WIREFRAME","pageId":104}')

echo "  응답: $PRESIGNED_RESP"

PRESIGNED_URL=$(echo "$PRESIGNED_RESP" | grep -o '"presignedUrl":"[^"]*"' | cut -d'"' -f4)
FILE_URL=$(echo "$PRESIGNED_RESP" | grep -o '"fileUrl":"[^"]*"' | cut -d'"' -f4)

if [ -z "$PRESIGNED_URL" ]; then
  echo "  FAIL: presignedUrl 없음"
  FAIL=$((FAIL+1))
else
  echo "  OK: presignedUrl 발급됨"
  # URL에 /storage/ 경로가 포함되어 있는지 확인
  if echo "$PRESIGNED_URL" | grep -q "docbridge.cloud/storage/"; then
    echo "  OK: URL이 public endpoint 경유 (docbridge.cloud/storage/...)"
  else
    echo "  WARN: URL 형식 확인 필요: $PRESIGNED_URL"
  fi
  PASS=$((PASS+1))
fi

# 4. CORS preflight 테스트 (브라우저가 PUT 전에 보내는 OPTIONS)
echo ""
echo "[4/6] CORS preflight (OPTIONS) 테스트..."
CORS_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X OPTIONS \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: PUT" \
  -H "Access-Control-Request-Headers: Content-Type" \
  "$BASE_URL/storage/itda/test-cors")

if [ "$CORS_RESP" = "204" ]; then
  echo "  OK: CORS preflight 통과 (HTTP $CORS_RESP)"
  PASS=$((PASS+1))
else
  echo "  FAIL: CORS preflight 실패 (HTTP $CORS_RESP)"
  FAIL=$((FAIL+1))
fi

# 5. Presigned URL로 이미지 PUT 업로드
echo ""
echo "[5/6] Presigned URL로 이미지 PUT 업로드..."

if [ -z "$PRESIGNED_URL" ]; then
  echo "  SKIP: presignedUrl 없어서 스킵"
  FAIL=$((FAIL+1))
else
  # 1x1 PNG 테스트 이미지 생성
  printf '\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x02\x00\x00\x00\x90wS\xde\x00\x00\x00\x0cIDATx\x9cc\xf8\x0f\x00\x00\x01\x01\x00\x05\x18\xd8N\x00\x00\x00\x00IEND\xaeB`\x82' > /tmp/test-upload.png

  # Content-Type을 presigned URL 요청 시와 동일하게 설정 (중요!)
  UPLOAD_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X PUT \
    -H "Content-Type: image/png" \
    --data-binary @/tmp/test-upload.png \
    "$PRESIGNED_URL")

  if [ "$UPLOAD_STATUS" = "200" ]; then
    echo "  OK: 업로드 성공 (HTTP $UPLOAD_STATUS)"
    PASS=$((PASS+1))
  else
    echo "  FAIL: 업로드 실패 (HTTP $UPLOAD_STATUS)"
    # 디버깅용 상세 응답
    echo "  상세:"
    curl -s -X PUT -H "Content-Type: image/png" --data-binary @/tmp/test-upload.png "$PRESIGNED_URL"
    echo ""
    FAIL=$((FAIL+1))
  fi

  rm -f /tmp/test-upload.png
fi

# 6. 업로드된 파일 GET으로 확인
echo ""
echo "[6/6] 업로드된 파일 GET 확인..."

if [ -z "$FILE_URL" ]; then
  echo "  SKIP: fileUrl 없어서 스킵"
  FAIL=$((FAIL+1))
else
  GET_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$FILE_URL")
  if [ "$GET_STATUS" = "200" ]; then
    echo "  OK: 업로드된 파일 조회 성공 (HTTP $GET_STATUS)"
    PASS=$((PASS+1))
  else
    echo "  FAIL: 업로드된 파일 조회 실패 (HTTP $GET_STATUS)"
    FAIL=$((FAIL+1))
  fi
fi

# 결과
echo ""
echo "===== 결과: $PASS PASS / $FAIL FAIL ====="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
