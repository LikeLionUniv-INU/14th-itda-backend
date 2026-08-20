#!/bin/bash
# ============================================
# 잇다(ITDA) 시연용 데모 데이터 세팅 스크립트
# 와이어프레임 이미지 업로드 + 문서/페이지/핀/요구사항 생성
# ============================================
set -e

API_BASE="${API_BASE:-https://docbridge.cloud}"
WIREFRAME_DIR="$(cd "$(dirname "$0")/.." && pwd)/잇다와이어프레임"

echo "============================================"
echo "  ITDA 시연 데모 데이터 세팅"
echo "  API: $API_BASE"
echo "============================================"

# ---- 1. 로그인 (korea@itda.com = 팀장) ----
echo ""
echo "[1/6] 로그인 중..."
LOGIN_RES=$(curl -s -X POST "$API_BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"korea@itda.com","password":"test1234"}')

TOKEN=$(echo "$LOGIN_RES" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',d).get('accessToken',d.get('token','')))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "❌ 로그인 실패: $LOGIN_RES"
  exit 1
fi
echo "✅ 로그인 성공"

AUTH="Authorization: Bearer $TOKEN"

# ---- 2. 문서 생성 ----
echo ""
echo "[2/6] 문서 생성 중..."
DOC_RES=$(curl -s -X POST "$API_BASE/api/teams/101/documents" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "name": "잇다 서비스 기획서",
    "language": "ko",
    "version": 1,
    "documentType": "STORYBOARD"
  }')

DOC_ID=$(echo "$DOC_RES" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',d).get('documentId', d.get('data',{}).get('id','')))" 2>/dev/null)

if [ -z "$DOC_ID" ] || [ "$DOC_ID" = "" ]; then
  echo "❌ 문서 생성 실패: $DOC_RES"
  exit 1
fi
echo "✅ 문서 생성 완료: documentId=$DOC_ID"

# ---- 3. 문서 저장 (9페이지 + 핀 + 요구사항) ----
echo ""
echo "[3/6] 문서 저장 중 (9페이지, 핀, 요구사항)..."
SAVE_RES=$(curl -s -X PUT "$API_BASE/api/documents/$DOC_ID/versions/1" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
  "status": "COMPLETED",
  "changeSummary": "잇다 서비스 전체 화면 기획서 작성 완료",
  "pages": [
    {
      "pageNumber": 1,
      "screenName": "로그인",
      "screenId": "AUTH-001",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 750.0, "yCoordinate": 280.0,
          "requirements": [
            {"tabType": "공통", "itemName": "로그인 폼", "content": "이메일과 비밀번호 입력 필드, 로그인 버튼, 회원가입 링크를 포함한 중앙 정렬 카드 형태의 로그인 폼", "isRequired": true},
            {"tabType": "공통", "itemName": "서비스 소개", "content": "좌측 영역에 글로벌 협업, AI 자동 동기화, 팀 협업, 버전 관리 4가지 핵심 기능 소개 텍스트 배치", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "프론트엔드",
          "xCoordinate": 900.0, "yCoordinate": 450.0,
          "requirements": [
            {"tabType": "프론트엔드", "itemName": "이메일 유효성 검증", "content": "이메일 입력 시 실시간으로 형식을 검증하고, 올바르지 않은 형식일 경우 빨간색 에러 메시지를 입력 필드 하단에 표시", "isRequired": true},
            {"tabType": "프론트엔드", "itemName": "비밀번호 표시 토글", "content": "비밀번호 입력 필드 우측에 눈 아이콘을 배치하여 클릭 시 비밀번호 표시/숨김 전환", "isRequired": false}
          ]
        },
        {
          "pinNumber": 3, "tabType": "백엔드",
          "xCoordinate": 900.0, "yCoordinate": 600.0,
          "requirements": [
            {"tabType": "백엔드", "itemName": "JWT 인증", "content": "POST /api/auth/login 엔드포인트에서 이메일/비밀번호 검증 후 accessToken(15분)과 refreshToken(7일)을 발급", "isRequired": true},
            {"tabType": "백엔드", "itemName": "로그인 실패 처리", "content": "잘못된 이메일 또는 비밀번호 입력 시 401 응답과 함께 구체적인 에러 메시지를 반환", "isRequired": true}
          ]
        }
      ]
    },
    {
      "pageNumber": 2,
      "screenName": "회원가입",
      "screenId": "AUTH-002",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 800.0, "yCoordinate": 200.0,
          "requirements": [
            {"tabType": "공통", "itemName": "가입 폼", "content": "이름, 이메일, 비밀번호, 비밀번호 확인, 국적, 사용 언어 총 6개 입력 필드로 구성된 회원가입 폼", "isRequired": true},
            {"tabType": "공통", "itemName": "국적/언어 선택", "content": "국적은 드롭다운으로 국가 목록 제공, 사용 언어는 ko/en/ja/zh/es/fr/de/vi 중 선택", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "백엔드",
          "xCoordinate": 800.0, "yCoordinate": 500.0,
          "requirements": [
            {"tabType": "백엔드", "itemName": "이메일 중복 검사", "content": "POST /api/auth/signup 호출 시 이메일 중복이면 409 Conflict 응답, 성공 시 201 Created와 함께 사용자 정보 반환", "isRequired": true},
            {"tabType": "백엔드", "itemName": "비밀번호 암호화", "content": "BCrypt 알고리즘으로 비밀번호를 해싱하여 저장, 원문은 절대 DB에 저장하지 않음", "isRequired": true}
          ]
        }
      ]
    },
    {
      "pageNumber": 3,
      "screenName": "메인 홈",
      "screenId": "HOME-001",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 400.0, "yCoordinate": 150.0,
          "requirements": [
            {"tabType": "공통", "itemName": "환영 배너", "content": "로그인한 사용자의 이름을 포함한 환영 메시지와 서비스 핵심 가치를 보여주는 상단 배너 영역", "isRequired": true},
            {"tabType": "공통", "itemName": "글로벌 네비게이션", "content": "상단 고정 GNB에 홈/프로젝트/문서/설정 메뉴와 프로젝트 생성/입장 버튼, 알림 아이콘, 프로필 아바타 배치", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "기획",
          "xCoordinate": 400.0, "yCoordinate": 350.0,
          "requirements": [
            {"tabType": "기획", "itemName": "내 프로젝트", "content": "참여 중인 프로젝트를 카드 형태로 최대 4개 표시, 프로젝트명/기본언어/멤버 아바타/최종 업데이트 정보 포함", "isRequired": true},
            {"tabType": "기획", "itemName": "최근 문서", "content": "최근 수정된 문서를 테이블 형태로 표시, 문서 이름/프로젝트/언어/버전/최종 업데이트 컬럼 구성", "isRequired": true}
          ]
        },
        {
          "pinNumber": 3, "tabType": "프론트엔드",
          "xCoordinate": 700.0, "yCoordinate": 400.0,
          "requirements": [
            {"tabType": "프론트엔드", "itemName": "반응형 카드 그리드", "content": "프로젝트 카드를 CSS Grid로 배치, 화면 너비에 따라 4열→2열→1열로 반응형 전환", "isRequired": true}
          ]
        }
      ]
    },
    {
      "pageNumber": 4,
      "screenName": "프로젝트 목록",
      "screenId": "HOME-002",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 400.0, "yCoordinate": 300.0,
          "requirements": [
            {"tabType": "공통", "itemName": "프로젝트 카드", "content": "모든 참여 프로젝트를 카드로 표시, 각 카드에 프로젝트명/기본언어/지원언어/멤버/최종 업데이트 포함", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "백엔드",
          "xCoordinate": 600.0, "yCoordinate": 400.0,
          "requirements": [
            {"tabType": "백엔드", "itemName": "대시보드 API", "content": "GET /api/dashboard - 사용자의 프로젝트 목록과 최근 문서를 통합하여 한 번의 API 호출로 응답", "isRequired": true}
          ]
        }
      ]
    },
    {
      "pageNumber": 5,
      "screenName": "팀 프로젝트 메인",
      "screenId": "TEAM-001",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 400.0, "yCoordinate": 130.0,
          "requirements": [
            {"tabType": "공통", "itemName": "프로젝트 헤더", "content": "프로젝트명, 기본 언어, 멤버 아바타, 생성일을 표시하는 상단 요약 영역", "isRequired": true},
            {"tabType": "공통", "itemName": "최근 문서 테이블", "content": "해당 프로젝트의 최근 수정 문서를 문서종류/언어/버전/최종업데이트 컬럼으로 표시", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "기획",
          "xCoordinate": 400.0, "yCoordinate": 500.0,
          "requirements": [
            {"tabType": "기획", "itemName": "활동 요약", "content": "프로젝트 내 최근 활동 이력을 타임라인 형태로 표시 (문서 업로드/수정/번역 완료 등)", "isRequired": true},
            {"tabType": "기획", "itemName": "프로젝트 멤버", "content": "팀장/팀원 역할 구분과 함께 멤버 목록 표시, 팀장은 멤버 초대 버튼 활성화", "isRequired": true}
          ]
        },
        {
          "pinNumber": 3, "tabType": "프론트엔드",
          "xCoordinate": 1000.0, "yCoordinate": 300.0,
          "requirements": [
            {"tabType": "프론트엔드", "itemName": "문서 모아보기", "content": "우측 사이드바에 프로젝트의 전체 문서를 카드 형태로 나열, 버전 드롭다운으로 빠른 버전 전환", "isRequired": true}
          ]
        }
      ]
    },
    {
      "pageNumber": 6,
      "screenName": "문서 작성",
      "screenId": "DOC-001",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 200.0, "yCoordinate": 100.0,
          "requirements": [
            {"tabType": "공통", "itemName": "문서 헤더", "content": "문서명, 버전 정보, 업데이트 일시, 버전 전환 드롭다운, 임시저장/저장 버튼을 포함한 상단 헤더", "isRequired": true},
            {"tabType": "공통", "itemName": "수정사항 요약", "content": "현재 버전에서 변경된 항목을 페이지/항목/수정내용/수정자/수정일 테이블로 표시", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "기획",
          "xCoordinate": 150.0, "yCoordinate": 400.0,
          "requirements": [
            {"tabType": "기획", "itemName": "페이지 네비게이터", "content": "좌측에 페이지 목록을 가로 스크롤 가능한 탭으로 표시, 페이지 추가 버튼 포함", "isRequired": true},
            {"tabType": "기획", "itemName": "화면 정보", "content": "현재 선택된 페이지의 화면 이름과 화면 ID를 편집 가능한 입력 필드로 표시", "isRequired": true}
          ]
        },
        {
          "pinNumber": 3, "tabType": "프론트엔드",
          "xCoordinate": 300.0, "yCoordinate": 550.0,
          "requirements": [
            {"tabType": "프론트엔드", "itemName": "와이어프레임 캔버스", "content": "업로드된 와이어프레임 이미지 위에 핀을 드래그&드롭으로 배치, 데스크탑/모바일 뷰 전환 지원", "isRequired": true},
            {"tabType": "프론트엔드", "itemName": "핀 인터랙션", "content": "핀 클릭 시 우측 요구사항 섹션에서 해당 핀의 요구사항이 하이라이트, 핀 추가/삭제 버튼 제공", "isRequired": true}
          ]
        },
        {
          "pinNumber": 4, "tabType": "백엔드",
          "xCoordinate": 700.0, "yCoordinate": 300.0,
          "requirements": [
            {"tabType": "백엔드", "itemName": "자동 저장", "content": "30초 간격으로 문서 내용을 자동 저장, POST /api/documents/{id}/versions/{ver}/auto-save 호출", "isRequired": false},
            {"tabType": "백엔드", "itemName": "전체 저장 API", "content": "PUT /api/documents/{id}/versions/{ver} - 페이지/핀/요구사항 전체 데이터를 한 번에 저장", "isRequired": true}
          ]
        }
      ]
    },
    {
      "pageNumber": 7,
      "screenName": "문서 수정",
      "screenId": "DOC-002",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 400.0, "yCoordinate": 100.0,
          "requirements": [
            {"tabType": "공통", "itemName": "수정 모드 헤더", "content": "Version.X 수정 타이틀, 임시저장 상태 표시, 저장 버튼을 포함한 수정 전용 헤더", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "기획",
          "xCoordinate": 600.0, "yCoordinate": 350.0,
          "requirements": [
            {"tabType": "기획", "itemName": "요구사항 편집", "content": "공통/기획/프론트/백엔드/디자인 탭별로 요구사항을 항목명+내용 형태로 편집, 수정하기 버튼으로 인라인 편집 활성화", "isRequired": true},
            {"tabType": "기획", "itemName": "변경 추적", "content": "수정된 요구사항은 보라색 하이라이트로 표시, 수정사항 요약 테이블에 자동 반영", "isRequired": true}
          ]
        },
        {
          "pinNumber": 3, "tabType": "백엔드",
          "xCoordinate": 600.0, "yCoordinate": 500.0,
          "requirements": [
            {"tabType": "백엔드", "itemName": "변경 이력 API", "content": "GET /api/documents/{id}/versions/{ver}/changes - 이전 버전 대비 변경사항 목록을 반환", "isRequired": true}
          ]
        }
      ]
    },
    {
      "pageNumber": 8,
      "screenName": "AI 번역",
      "screenId": "TRANS-001",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 700.0, "yCoordinate": 200.0,
          "requirements": [
            {"tabType": "공통", "itemName": "번역 진행 UI", "content": "AI 번역 진행중 타이틀, 전체 진행률 프로그레스바, 언어별 상태 카드(완료/번역중/대기중) 표시", "isRequired": true},
            {"tabType": "공통", "itemName": "언어 카드", "content": "각 대상 언어를 국기 이미지와 함께 카드로 표시, 완료(초록)/진행중(보라)/대기중(분홍) 뱃지 구분", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "백엔드",
          "xCoordinate": 700.0, "yCoordinate": 450.0,
          "requirements": [
            {"tabType": "백엔드", "itemName": "비동기 번역", "content": "POST /api/documents/{id}/versions/{ver}/translate 호출 후 @Async로 OpenAI GPT-4o API를 통해 각 언어별 순차 번역 실행", "isRequired": true},
            {"tabType": "백엔드", "itemName": "실시간 상태", "content": "Redis Pub/Sub으로 번역 진행 상태를 실시간 발행, 프론트엔드는 2.5초 간격 폴링으로 상태 조회", "isRequired": true}
          ]
        }
      ]
    },
    {
      "pageNumber": 9,
      "screenName": "수정 확인 (팀원)",
      "screenId": "REVIEW-001",
      "pins": [
        {
          "pinNumber": 1, "tabType": "공통",
          "xCoordinate": 200.0, "yCoordinate": 100.0,
          "requirements": [
            {"tabType": "공통", "itemName": "비교 뷰 헤더", "content": "문서명, 현재/이전 버전 표시, 수정사항 요약(총 수정/확인/미확인 카운트) 포함", "isRequired": true},
            {"tabType": "공통", "itemName": "수정사항 요약 테이블", "content": "변경된 항목을 페이지/항목/수정내용미리보기/수정자/수정일로 표시, 클릭 시 해당 위치로 스크롤", "isRequired": true}
          ]
        },
        {
          "pinNumber": 2, "tabType": "기획",
          "xCoordinate": 600.0, "yCoordinate": 400.0,
          "requirements": [
            {"tabType": "기획", "itemName": "변경 전/후 비교", "content": "요구사항 영역에서 변경 전(빨간)/변경 후(초록)/추가사항(보라) 3개 탭으로 변경 내용을 시각적으로 비교", "isRequired": true},
            {"tabType": "기획", "itemName": "확인 처리", "content": "팀원이 수정사항을 확인하면 체크 표시가 되고, 수정사항 요약의 확인 카운트가 증가", "isRequired": true}
          ]
        }
      ]
    }
  ]
}')

echo "✅ 문서 저장 완료"

# ---- 4. 저장된 페이지 ID 조회 ----
echo ""
echo "[4/6] 페이지 정보 조회 중..."
DETAIL_RES=$(curl -s -X GET "$API_BASE/api/documents/$DOC_ID/versions/1" \
  -H "$AUTH")

# 페이지 ID 추출
PAGE_IDS=$(echo "$DETAIL_RES" | python3 -c "
import sys, json
d = json.load(sys.stdin)
data = d.get('data', d)
pages = data.get('pages', [])
for p in pages:
    print(f\"{p['pageNumber']}:{p['id']}:{p['screenName']}\")
" 2>/dev/null)

if [ -z "$PAGE_IDS" ]; then
  echo "❌ 페이지 조회 실패: $DETAIL_RES"
  exit 1
fi

echo "✅ 페이지 조회 완료:"
echo "$PAGE_IDS" | while read line; do echo "   $line"; done

# ---- 5. 와이어프레임 이미지 업로드 ----
echo ""
echo "[5/6] 와이어프레임 이미지 업로드 중..."

# 페이지번호 → 이미지파일 매핑
declare -A IMAGE_MAP
IMAGE_MAP[1]="$WIREFRAME_DIR/로그인, 회원가입/로그인.png"
IMAGE_MAP[2]="$WIREFRAME_DIR/로그인, 회원가입/회원가입.png"
IMAGE_MAP[3]="$WIREFRAME_DIR/메인화면/메인화면홈데이터있을때.png"
IMAGE_MAP[4]="$WIREFRAME_DIR/메인화면/메인화면프로젝트데이터있을때.png"
IMAGE_MAP[5]="$WIREFRAME_DIR/팀프로젝트메인화면/팀프로젝트메인화면.png"
IMAGE_MAP[6]="$WIREFRAME_DIR/문서작성/문서작성.png"
IMAGE_MAP[7]="$WIREFRAME_DIR/수정화면/수정화면-수정데이터가있을때.png"
IMAGE_MAP[8]="$WIREFRAME_DIR/번역/AI번역진행중.png"
IMAGE_MAP[9]="$WIREFRAME_DIR/수정문서확인-팀원/수정문서-요구사항작성에수정사항있을때.png"

UPLOAD_SUCCESS=0
UPLOAD_FAIL=0

echo "$PAGE_IDS" | while IFS=: read PAGE_NUM PAGE_ID SCREEN_NAME; do
  IMAGE_FILE="${IMAGE_MAP[$PAGE_NUM]}"

  if [ ! -f "$IMAGE_FILE" ]; then
    echo "   ⚠️  페이지 $PAGE_NUM ($SCREEN_NAME): 이미지 파일 없음"
    continue
  fi

  FILE_NAME="wireframe_page${PAGE_NUM}_$(date +%s).png"

  # Step 1: Presigned URL 발급
  PRESIGNED_RES=$(curl -s -X POST "$API_BASE/api/files/presigned-url" \
    -H "Content-Type: application/json" \
    -H "$AUTH" \
    -d "{
      \"fileName\": \"$FILE_NAME\",
      \"contentType\": \"image/png\",
      \"imageType\": \"WIREFRAME\",
      \"pageId\": $PAGE_ID
    }")

  PRESIGNED_URL=$(echo "$PRESIGNED_RES" | python3 -c "import sys,json; d=json.load(sys.stdin); r=d.get('data',d); print(r.get('presignedUrl',''))" 2>/dev/null)
  FILE_URL=$(echo "$PRESIGNED_RES" | python3 -c "import sys,json; d=json.load(sys.stdin); r=d.get('data',d); print(r.get('fileUrl',''))" 2>/dev/null)

  if [ -z "$PRESIGNED_URL" ]; then
    echo "   ❌ 페이지 $PAGE_NUM ($SCREEN_NAME): Presigned URL 발급 실패"
    continue
  fi

  # Step 2: MinIO에 이미지 업로드
  UPLOAD_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$PRESIGNED_URL" \
    -H "Content-Type: image/png" \
    --data-binary "@$IMAGE_FILE")

  if [ "$UPLOAD_STATUS" != "200" ] && [ "$UPLOAD_STATUS" != "204" ]; then
    echo "   ❌ 페이지 $PAGE_NUM ($SCREEN_NAME): MinIO 업로드 실패 (HTTP $UPLOAD_STATUS)"
    continue
  fi

  # Step 3: 메타데이터 등록
  META_RES=$(curl -s -X POST "$API_BASE/api/pages/$PAGE_ID/wireframe-images" \
    -H "Content-Type: application/json" \
    -H "$AUTH" \
    -d "{
      \"imageType\": \"WIREFRAME\",
      \"imageUrl\": \"$FILE_URL\",
      \"originalWidth\": 1456,
      \"originalHeight\": 816,
      \"displayWidth\": 660,
      \"displayHeight\": 370
    }")

  echo "   ✅ 페이지 $PAGE_NUM ($SCREEN_NAME): 업로드 완료"
done

# ---- 6. 번역 요청 ----
echo ""
echo "[6/6] AI 번역 요청 중 (영어, 일본어)..."
TRANS_RES=$(curl -s -X POST "$API_BASE/api/documents/$DOC_ID/versions/1/translate" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "translations": [
      {"userId": 102, "targetLanguage": "en"},
      {"userId": 103, "targetLanguage": "ja"}
    ]
  }')

echo "번역 응답: $TRANS_RES"

JOB_ID=$(echo "$TRANS_RES" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',d).get('jobId', d.get('data',{}).get('id','')))" 2>/dev/null)

echo ""
echo "============================================"
echo "  🎉 데모 데이터 세팅 완료!"
echo "============================================"
echo "  문서 ID: $DOC_ID"
echo "  번역 Job ID: $JOB_ID"
echo "  페이지: 9개 (로그인~수정확인)"
echo "  와이어프레임: 9개 업로드"
echo "  번역: 영어 + 일본어"
echo ""
echo "  확인 방법:"
echo "  - korea@itda.com: 팀프로젝트 → 잇다 서비스 기획서"
echo "  - english@itda.com: 동일 문서 → 영어 번역 확인"
echo "  - member1@itda.com: 동일 문서 → 일본어 번역 확인"
echo "============================================"
