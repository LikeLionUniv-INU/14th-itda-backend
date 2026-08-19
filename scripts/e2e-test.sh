#!/bin/bash
# ============================================
# E2E Test Script for ITDA Backend
# Usage: bash scripts/e2e-test.sh
# ============================================

BASE="https://docbridge.cloud"
PASS=0
FAIL=0
TOTAL=0

test_api() {
    local desc="$1"
    local expected_code="$2"
    local method="$3"
    local path="$4"
    local token="$5"
    local body="$6"

    TOTAL=$((TOTAL + 1))

    local args="-s -o /tmp/e2e_body.txt -w %{http_code} -X $method"
    if [ -n "$token" ]; then
        args="$args -H \"Authorization: Bearer $token\""
    fi
    if [ -n "$body" ]; then
        args="$args -H \"Content-Type: application/json\" -d '$body'"
    fi

    local actual_code
    actual_code=$(eval curl $args "${BASE}${path}")
    local response
    response=$(cat /tmp/e2e_body.txt 2>/dev/null)

    if [ "$actual_code" = "$expected_code" ]; then
        echo -e "  \033[32m✓\033[0m [$actual_code] $desc"
        PASS=$((PASS + 1))
    else
        echo -e "  \033[31m✗\033[0m [$actual_code] $desc (expected $expected_code)"
        echo "    Response: $(echo "$response" | head -c 200)"
        FAIL=$((FAIL + 1))
    fi

    # Return response for token extraction
    echo "$response" > /tmp/e2e_last.txt
}

extract_token() {
    local field="$1"
    python3 -c "import json; print(json.loads(open('/tmp/e2e_last.txt').read())['data']['$field'])" 2>/dev/null
}

echo "======================================"
echo "  ITDA E2E Test - $BASE"
echo "======================================"

# ============================================
echo ""
echo "--- 1. 인증 (Auth) ---"
# ============================================

# 로그인 - KO 유저
test_api "KO 유저 로그인" "200" "POST" "/api/auth/login" "" '{"email":"korea@itda.com","password":"test1234"}'
KO_ACCESS=$(extract_token "accessToken")
KO_REFRESH=$(extract_token "refreshToken")

# 로그인 - EN 유저
test_api "EN 유저 로그인" "200" "POST" "/api/auth/login" "" '{"email":"english@itda.com","password":"test1234"}'
EN_ACCESS=$(extract_token "accessToken")

# 로그인 - JA 유저
test_api "JA 유저 로그인" "200" "POST" "/api/auth/login" "" '{"email":"member1@itda.com","password":"test1234"}'
JA_ACCESS=$(extract_token "accessToken")

# 토큰 갱신
test_api "토큰 갱신" "200" "POST" "/api/auth/refresh" "" "{\"refreshToken\":\"$KO_REFRESH\"}"
KO_ACCESS=$(extract_token "accessToken")

# 에러: 잘못된 비밀번호
test_api "잘못된 비밀번호 → 401" "401" "POST" "/api/auth/login" "" '{"email":"korea@itda.com","password":"wrong123"}'

# 에러: 잘못된 이메일 형식 회원가입
test_api "잘못된 이메일 회원가입 → 400" "400" "POST" "/api/auth/signup" "" '{"firstName":"Test","lastName":"User","email":"notanemail","password":"test1234","country":"Korea","language":"ko"}'

# 에러: 영어 외 이름
test_api "한글 이름 회원가입 → 400" "400" "POST" "/api/auth/signup" "" '{"firstName":"테스트","lastName":"User","email":"new@test.com","password":"test1234","country":"Korea","language":"ko"}'

# 에러: 이메일 중복
test_api "이메일 중복 회원가입 → 409" "409" "POST" "/api/auth/signup" "" '{"firstName":"Test","lastName":"User","email":"korea@itda.com","password":"test1234","country":"Korea","language":"ko"}'

# 에러: 토큰 없이 접근
test_api "토큰 없이 API 접근 → 401" "401" "GET" "/api/users/me" "" ""

# ============================================
echo ""
echo "--- 2. 사용자 (User) ---"
# ============================================

test_api "내 정보 조회" "200" "GET" "/api/users/me" "$KO_ACCESS"
test_api "EN 유저 정보 조회" "200" "GET" "/api/users/me" "$EN_ACCESS"

# ============================================
echo ""
echo "--- 3. 대시보드 (Dashboard) ---"
# ============================================

test_api "홈 탭 조회" "200" "GET" "/api/dashboard" "$KO_ACCESS"
test_api "프로젝트 탭 조회" "200" "GET" "/api/dashboard/projects" "$KO_ACCESS"
test_api "문서 탭 조회" "200" "GET" "/api/dashboard/documents" "$KO_ACCESS"

# ============================================
echo ""
echo "--- 4. 팀 프로젝트 (Team) ---"
# ============================================

test_api "팀 상세 조회 (DocBridge MVP)" "200" "GET" "/api/teams/101" "$KO_ACCESS"
test_api "초대 코드 조회" "200" "GET" "/api/teams/101/invite-code" "$KO_ACCESS"
test_api "팀 알림 조회" "200" "GET" "/api/teams/101/notifications" "$KO_ACCESS"

# 에러: 비멤버 팀 조회
test_api "비멤버 팀 조회 → 403" "403" "GET" "/api/teams/101" "$JA_ACCESS"

# ============================================
echo ""
echo "--- 5. 문서 관리 (Document) ---"
# ============================================

# 문서 상세 조회 (메인화면 v2)
test_api "문서 상세 조회 (메인화면 v2)" "200" "GET" "/api/documents/101/versions/2" "$KO_ACCESS"

# 번역된 문서 조회
test_api "번역 문서 조회 (?lang=en)" "200" "GET" "/api/documents/101/versions/2?lang=en" "$KO_ACCESS"
test_api "번역 문서 조회 (?lang=ja)" "200" "GET" "/api/documents/101/versions/2?lang=ja" "$KO_ACCESS"

# 버전 목록 조회
test_api "버전 목록 조회" "200" "GET" "/api/documents/101/versions" "$KO_ACCESS"

# 수정사항 목록
test_api "수정사항 목록 조회" "200" "GET" "/api/documents/101/versions/2/changes" "$KO_ACCESS"

# 문서 상세 (로그인 v2)
test_api "문서 상세 (로그인 v2)" "200" "GET" "/api/documents/102/versions/2" "$KO_ACCESS"

# 문서 상세 (Product v2)
test_api "문서 상세 (Product v2)" "200" "GET" "/api/documents/103/versions/2" "$EN_ACCESS"

# MEMBER가 문서 생성 시도
test_api "MEMBER 문서 생성 → 403" "403" "POST" "/api/teams/101/documents" "$EN_ACCESS" '{"name":"test","language":"en"}'

# 마지막 버전 삭제 시도 (Doc 102 v2가 마지막은 아니지만, v1이 있으므로 삭제 가능해야)
# Doc 103의 v1 삭제 시도 (v2가 있으므로 가능)

# ============================================
echo ""
echo "--- 6. 페이지/핀/요구사항 (Page/Pin/Requirement) ---"
# ============================================

# 핀 목록 조회 (Page 104, 홈)
test_api "핀 목록 조회 (전체)" "200" "GET" "/api/pages/104/pins" "$KO_ACCESS"
test_api "핀 목록 조회 (?tabType=공통)" "200" "GET" "/api/pages/104/pins?tabType=%EA%B3%B5%ED%86%B5" "$KO_ACCESS"
test_api "핀 목록 조회 (?tabType=백엔드)" "200" "GET" "/api/pages/104/pins?tabType=%EB%B0%B1%EC%97%94%EB%93%9C" "$KO_ACCESS"

# ============================================
echo ""
echo "--- 7. 번역 (Translation) ---"
# ============================================

# 번역 상태 조회 (기존 job 101)
test_api "번역 상태 조회 (폴링)" "200" "GET" "/api/translations/101" "$KO_ACCESS"

# ============================================
echo ""
echo "--- 8. Swagger UI ---"
# ============================================

test_api "Swagger UI 접근" "200" "GET" "/swagger-ui/index.html" ""
test_api "OpenAPI docs 접근" "200" "GET" "/v3/api-docs" ""

# ============================================
echo ""
echo "======================================"
echo "  Results: $PASS passed / $FAIL failed / $TOTAL total"
echo "======================================"

if [ $FAIL -gt 0 ]; then
    exit 1
fi
