-- ============================================
-- V9: Dummy Data for Grading / Frontend Development
-- ============================================
-- Password: test1234 (BCrypt hash)
-- BCrypt hash of 'test1234': $2a$10$qZxGdfzlIzWTM.C3yqBUn.tj9L0IqhVHJbGEO6Ne2p.2JU475cONC

-- ============================================
-- 1. Users (KO user + EN user + extra members)
-- ============================================
INSERT INTO users (id, email, password, first_name, last_name, country, language, bio, created_at, updated_at) VALUES
(101, 'korea@itda.com',   '$2a$10$qZxGdfzlIzWTM.C3yqBUn.tj9L0IqhVHJbGEO6Ne2p.2JU475cONC', 'Minjun',  'Kim',    'South Korea', 'ko', '프론트엔드 개발자입니다. React와 TypeScript를 주로 사용합니다.', NOW(), NOW()),
(102, 'english@itda.com', '$2a$10$qZxGdfzlIzWTM.C3yqBUn.tj9L0IqhVHJbGEO6Ne2p.2JU475cONC', 'Sarah',   'Johnson', 'United States', 'en', 'UX Designer with 3 years of experience.', NOW(), NOW()),
(103, 'member1@itda.com', '$2a$10$qZxGdfzlIzWTM.C3yqBUn.tj9L0IqhVHJbGEO6Ne2p.2JU475cONC', 'Yuto',    'Tanaka',  'Japan', 'ja', 'バックエンド開発者です。', NOW(), NOW()),
(104, 'member2@itda.com', '$2a$10$qZxGdfzlIzWTM.C3yqBUn.tj9L0IqhVHJbGEO6Ne2p.2JU475cONC', 'Wei',     'Chen',    'China', 'zh', '全栈工程师，专注于云原生技术。', NOW(), NOW());

SELECT setval('users_id_seq', GREATEST((SELECT MAX(id) FROM users), 104));

-- ============================================
-- 2. Team Projects (2 projects)
-- ============================================
INSERT INTO team_projects (id, name, default_language, invite_code, created_by, created_at, updated_at) VALUES
(101, 'DocBridge MVP',        'ko', 'DOC101', 101, NOW() - INTERVAL '7 days', NOW() - INTERVAL '1 day'),
(102, 'Global Shopping App',  'en', 'SHP102', 102, NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days');

SELECT setval('team_projects_id_seq', GREATEST((SELECT MAX(id) FROM team_projects), 102));

-- ============================================
-- 3. Team Members
-- ============================================
-- Project 101: Kim(LEADER), Sarah(MEMBER), Yuto(MEMBER)
-- Project 102: Sarah(LEADER), Kim(MEMBER), Wei(MEMBER)
INSERT INTO team_members (id, team_project_id, user_id, role, joined_at) VALUES
(101, 101, 101, 'LEADER', NOW() - INTERVAL '7 days'),
(102, 101, 102, 'MEMBER', NOW() - INTERVAL '6 days'),
(103, 101, 103, 'MEMBER', NOW() - INTERVAL '6 days'),
(104, 102, 102, 'LEADER', NOW() - INTERVAL '5 days'),
(105, 102, 101, 'MEMBER', NOW() - INTERVAL '4 days'),
(106, 102, 104, 'MEMBER', NOW() - INTERVAL '4 days');

SELECT setval('team_members_id_seq', GREATEST((SELECT MAX(id) FROM team_members), 106));

-- ============================================
-- 4. Documents (3 documents across 2 projects)
-- ============================================
INSERT INTO documents (id, team_project_id, name, language, document_type, created_by, created_at, updated_at) VALUES
(101, 101, '메인화면',    'ko', 'STORYBOARD', 101, NOW() - INTERVAL '6 days', NOW() - INTERVAL '1 day'),
(102, 101, '로그인',      'ko', 'WIREFRAME',  101, NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days'),
(103, 102, 'ProductPage', 'en', 'STORYBOARD', 102, NOW() - INTERVAL '4 days', NOW() - INTERVAL '1 day');

SELECT setval('documents_id_seq', GREATEST((SELECT MAX(id) FROM documents), 103));

-- ============================================
-- 5. Document Versions
-- ============================================
-- Doc 101: v1(COMPLETED) + v2(COMPLETED) + v3(DRAFT)
-- Doc 102: v1(COMPLETED) + v2(DRAFT)
-- Doc 103: v1(COMPLETED) + v2(COMPLETED)
INSERT INTO document_versions (id, document_id, version, status, is_auto_saved, change_summary, created_by, created_at, updated_at) VALUES
(101, 101, 1, 'COMPLETED', false, '초기 스토리보드 작성',                    101, NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'),
(102, 101, 2, 'COMPLETED', false, '대시보드 레이아웃 변경, 네비게이션 추가', 101, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),
(103, 101, 3, 'DRAFT',     true,  '사이드바 메뉴 수정 중',                   101, NOW() - INTERVAL '1 day',  NOW() - INTERVAL '1 hour'),
(104, 102, 1, 'COMPLETED', false, '로그인/회원가입 와이어프레임',            101, NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days'),
(105, 102, 2, 'DRAFT',     false, '소셜 로그인 버튼 추가',                   101, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),
(106, 103, 1, 'COMPLETED', false, 'Initial product page layout',             102, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),
(107, 103, 2, 'COMPLETED', false, 'Added cart and review sections',          102, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day');

SELECT setval('document_versions_id_seq', GREATEST((SELECT MAX(id) FROM document_versions), 107));

-- ============================================
-- 6. Pages
-- ============================================
-- Version 101 (메인화면 v1): 3 pages
INSERT INTO pages (id, document_version_id, page_number, screen_name, screen_id, created_at, updated_at) VALUES
(101, 101, 1, '홈',         'S-001', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(102, 101, 2, '검색',       'S-002', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(103, 101, 3, '마이페이지', 'S-003', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days');

-- Version 102 (메인화면 v2): 4 pages
INSERT INTO pages (id, document_version_id, page_number, screen_name, screen_id, created_at, updated_at) VALUES
(104, 102, 1, '홈',         'S-001', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(105, 102, 2, '검색',       'S-002', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(106, 102, 3, '마이페이지', 'S-003', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(107, 102, 4, '알림',       'S-004', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Version 103 (메인화면 v3 DRAFT): 4 pages
INSERT INTO pages (id, document_version_id, page_number, screen_name, screen_id, created_at, updated_at) VALUES
(108, 103, 1, '홈',         'S-001', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(109, 103, 2, '검색',       'S-002', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(110, 103, 3, '마이페이지', 'S-003', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(111, 103, 4, '알림',       'S-004', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

-- Version 104 (로그인 v1): 3 pages
INSERT INTO pages (id, document_version_id, page_number, screen_name, screen_id, created_at, updated_at) VALUES
(112, 104, 1, '로그인',     'L-001', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(113, 104, 2, '회원가입',   'L-002', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(114, 104, 3, '비번찾기',   'L-003', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

-- Version 105 (로그인 v2): 4 pages
INSERT INTO pages (id, document_version_id, page_number, screen_name, screen_id, created_at, updated_at) VALUES
(115, 105, 1, '로그인',     'L-001', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(116, 105, 2, '회원가입',   'L-002', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(117, 105, 3, '비번찾기',   'L-003', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(118, 105, 4, '소셜로그인', 'L-004', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Version 106 (ProductPage v1): 3 pages
INSERT INTO pages (id, document_version_id, page_number, screen_name, screen_id, created_at, updated_at) VALUES
(119, 106, 1, 'ProductList', 'P-001', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(120, 106, 2, 'ProductDtl',  'P-002', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(121, 106, 3, 'Cart',        'P-003', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Version 107 (ProductPage v2): 5 pages
INSERT INTO pages (id, document_version_id, page_number, screen_name, screen_id, created_at, updated_at) VALUES
(122, 107, 1, 'ProductList', 'P-001', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(123, 107, 2, 'ProductDtl',  'P-002', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(124, 107, 3, 'Cart',        'P-003', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(125, 107, 4, 'Reviews',     'P-004', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(126, 107, 5, 'Checkout',    'P-005', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

SELECT setval('pages_id_seq', GREATEST((SELECT MAX(id) FROM pages), 126));

-- ============================================
-- 6-1. Wireframe Images (placeholder images uploaded to MinIO)
-- ============================================
-- URL pattern: http://3.35.208.88:9000/itda/wireframes/{projectId}/{docId}/{pageId}/{type}_{timestamp}.png
-- Version 102 (메인화면 v2) pages
INSERT INTO wireframe_images (id, page_id, image_type, image_url, original_width, original_height, display_width, display_height, created_at) VALUES
(101, 104, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/101/104/original_1723900000000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '4 days'),
(102, 105, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/101/105/original_1723900001000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '4 days'),
(103, 106, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/101/106/original_1723900002000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '4 days'),
(104, 107, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/101/107/original_1723900003000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '4 days');

-- Version 103 (메인화면 v3 DRAFT) pages - copied from v2
INSERT INTO wireframe_images (id, page_id, image_type, image_url, original_width, original_height, display_width, display_height, created_at) VALUES
(105, 108, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/101/104/original_1723900000000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '1 day'),
(106, 109, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/101/105/original_1723900001000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '1 day'),
(107, 110, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/101/106/original_1723900002000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '1 day'),
(108, 111, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/101/107/original_1723900003000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '1 day');

-- Version 104 (로그인 v1) pages
INSERT INTO wireframe_images (id, page_id, image_type, image_url, original_width, original_height, display_width, display_height, created_at) VALUES
(109, 112, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/102/112/original_1723900004000.png', 1440, 900, 720, 450, NOW() - INTERVAL '5 days'),
(110, 113, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/102/113/original_1723900005000.png', 1440, 900, 720, 450, NOW() - INTERVAL '5 days'),
(111, 114, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/102/114/original_1723900006000.png', 1440, 900, 720, 450, NOW() - INTERVAL '5 days');

-- Version 105 (로그인 v2) pages
INSERT INTO wireframe_images (id, page_id, image_type, image_url, original_width, original_height, display_width, display_height, created_at) VALUES
(112, 115, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/102/112/original_1723900004000.png', 1440, 900, 720, 450, NOW() - INTERVAL '2 days'),
(113, 116, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/102/113/original_1723900005000.png', 1440, 900, 720, 450, NOW() - INTERVAL '2 days'),
(114, 117, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/102/114/original_1723900006000.png', 1440, 900, 720, 450, NOW() - INTERVAL '2 days'),
(115, 118, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/101/102/118/original_1723900007000.png', 1440, 900, 720, 450, NOW() - INTERVAL '2 days');

-- Version 106 (ProductPage v1) pages
INSERT INTO wireframe_images (id, page_id, image_type, image_url, original_width, original_height, display_width, display_height, created_at) VALUES
(116, 119, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/102/103/119/original_1723900008000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '4 days'),
(117, 120, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/102/103/120/original_1723900009000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '4 days'),
(118, 121, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/102/103/121/original_1723900010000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '4 days');

-- Version 107 (ProductPage v2) pages
INSERT INTO wireframe_images (id, page_id, image_type, image_url, original_width, original_height, display_width, display_height, created_at) VALUES
(119, 122, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/102/103/119/original_1723900008000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '2 days'),
(120, 123, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/102/103/120/original_1723900009000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '2 days'),
(121, 124, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/102/103/121/original_1723900010000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '2 days'),
(122, 125, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/102/103/125/original_1723900011000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '2 days'),
(123, 126, 'ORIGINAL', 'http://3.35.208.88:9000/itda/wireframes/102/103/126/original_1723900012000.png', 1920, 1080, 960, 540, NOW() - INTERVAL '2 days');

SELECT setval('wireframe_images_id_seq', GREATEST((SELECT MAX(id) FROM wireframe_images), 123));

-- ============================================
-- 7. Pins (on latest versions with various tab_types)
-- ============================================
-- Version 102 (메인화면 v2) - Page 104 (홈): 4 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(101, 104, 1, 50.0,  120.5, '공통',       NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(102, 104, 2, 200.0, 80.0,  '기획',       NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(103, 104, 3, 150.0, 300.0, '프론트엔드', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(104, 104, 4, 320.0, 200.0, '백엔드',     NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Version 102 - Page 105 (검색): 3 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(105, 105, 1, 100.0, 50.0,  '공통',       NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(106, 105, 2, 250.0, 150.0, '디자인',     NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(107, 105, 3, 180.0, 280.0, '프론트엔드', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Version 102 - Page 106 (마이페이지): 2 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(108, 106, 1, 160.0, 90.0,  '백엔드',     NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(109, 106, 2, 80.0,  350.0, '공통',       NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Version 102 - Page 107 (알림): 2 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(110, 107, 1, 200.0, 100.0, '기획',       NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(111, 107, 2, 120.0, 250.0, '프론트엔드', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Version 105 (로그인 v2) - Page 115 (로그인): 3 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(112, 115, 1, 180.0, 200.0, '공통',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(113, 115, 2, 180.0, 300.0, '프론트엔드', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(114, 115, 3, 180.0, 400.0, '백엔드',     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Version 105 - Page 116 (회원가입): 3 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(115, 116, 1, 160.0, 100.0, '기획',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(116, 116, 2, 160.0, 250.0, '공통',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(117, 116, 3, 160.0, 400.0, '백엔드',     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Version 105 - Page 118 (소셜로그인): 2 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(118, 118, 1, 200.0, 150.0, '프론트엔드', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(119, 118, 2, 200.0, 300.0, '백엔드',     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Version 107 (ProductPage v2) - Page 122 (ProductList): 3 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(120, 122, 1, 100.0, 80.0,  '공통',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(121, 122, 2, 300.0, 150.0, '디자인',     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(122, 122, 3, 200.0, 350.0, '프론트엔드', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Version 107 - Page 123 (ProductDtl): 4 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(123, 123, 1, 50.0,  100.0, '공통',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(124, 123, 2, 250.0, 200.0, '기획',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(125, 123, 3, 150.0, 350.0, '프론트엔드', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(126, 123, 4, 350.0, 400.0, '백엔드',     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Version 107 - Page 125 (Reviews): 2 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(127, 125, 1, 180.0, 120.0, '공통',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(128, 125, 2, 180.0, 300.0, '프론트엔드', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Version 107 - Page 126 (Checkout): 3 pins
INSERT INTO pins (id, page_id, pin_number, x_coordinate, y_coordinate, tab_type, created_at, updated_at) VALUES
(129, 126, 1, 100.0, 80.0,  '기획',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(130, 126, 2, 250.0, 200.0, '백엔드',     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(131, 126, 3, 150.0, 380.0, '공통',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

SELECT setval('pins_id_seq', GREATEST((SELECT MAX(id) FROM pins), 131));

-- ============================================
-- 8. Requirements (with is_required flag, various tab_types)
-- ============================================
-- Pin 101 (홈, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(101, 101, '공통', '헤더',     '상단 고정 네비게이션 바, 로고 + 메뉴 + 프로필 아이콘 배치', true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(102, 101, '공통', '푸터',     '하단 고정, 이용약관/개인정보처리방침/고객센터 링크 포함',    false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 102 (홈, 기획)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(103, 102, '기획', '배너영역', '프로모션 배너 슬라이드, 자동 롤링 5초 간격, 최대 5장',    true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(104, 102, '기획', '추천섹션', '사용자 활동 기반 추천 콘텐츠 영역, 최근 본 항목 우선',    true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 103 (홈, 프론트엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(105, 103, '프론트엔드', '무한스크롤',   '콘텐츠 목록 무한 스크롤 구현, Intersection Observer 사용', true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(106, 103, '프론트엔드', '스켈레톤UI',   '데이터 로딩 중 스켈레톤 화면 표시',                        false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 104 (홈, 백엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(107, 104, '백엔드', 'API설계',   'GET /api/home - 배너, 추천, 최근활동 데이터 통합 응답', true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(108, 104, '백엔드', '캐싱',      '홈 화면 데이터 Redis 캐싱, TTL 5분',                    false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 105 (검색, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(109, 105, '공통', '검색바',     '화면 상단 검색 입력 필드, 돋보기 아이콘, 최근 검색어 표시', true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 106 (검색, 디자인)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(110, 106, '디자인', '검색결과',   '카드형 레이아웃, 썸네일 + 제목 + 요약, 그리드 2열 배치', true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(111, 106, '디자인', '필터UI',     '좌측 필터 패널, 체크박스 + 슬라이더 조합, 접기/펼치기',  false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 107 (검색, 프론트엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(112, 107, '프론트엔드', '디바운싱', '검색 입력 디바운싱 300ms, 한글 조합 완료 후 검색 실행', true, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 108 (마이페이지, 백엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(113, 108, '백엔드', '프로필API', 'GET /api/users/me - 사용자 정보 + 프로젝트 목록 반환',  true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(114, 108, '백엔드', '비번변경',  'PUT /api/users/password - 현재 비밀번호 확인 후 변경',   true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 109 (마이페이지, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(115, 109, '공통', '프로필카드', '프로필 이미지 + 이름 + 이메일 + 소속 팀 수 표시',        true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 110 (알림, 기획)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(116, 110, '기획', '알림목록',   '시간순 정렬, 읽음/안읽음 구분, 문서 수정 알림 포함',     true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(117, 110, '기획', '알림설정',   '알림 종류별 ON/OFF 토글 (문서변경, 팀초대, 번역완료)',    false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 111 (알림, 프론트엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(118, 111, '프론트엔드', '뱃지',     '헤더 알림 아이콘에 안읽은 알림 수 뱃지 표시',            true,  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- Pin 112 (로그인, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(119, 112, '공통', '로그인폼',   '이메일 + 비밀번호 입력, 로그인 버튼, 회원가입 링크',      true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 113 (로그인, 프론트엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(120, 113, '프론트엔드', '유효성검증', '이메일 형식 검사, 비밀번호 8자 이상, 실시간 에러 메시지', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 114 (로그인, 백엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(121, 114, '백엔드', 'JWT발급',   'POST /api/auth/login - accessToken + refreshToken 반환',  true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(122, 114, '백엔드', '토큰갱신',  'POST /api/auth/refresh - Rotation 방식 갱신',              true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 115 (회원가입, 기획)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(123, 115, '기획', '가입필드',   '이름/성/이메일/비밀번호/국적/언어 총 6개 필드',            true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 116 (회원가입, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(124, 116, '공통', '약관동의',   '서비스 이용약관 및 개인정보처리방침 동의 체크박스',        false, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 117 (회원가입, 백엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(125, 117, '백엔드', '이메일중복', 'POST /api/auth/signup - 이메일 중복 시 409 응답',          true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 118 (소셜로그인, 프론트엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(126, 118, '프론트엔드', '소셜버튼', 'Google/GitHub OAuth 버튼, 각 브랜드 가이드라인 준수',      false, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 119 (소셜로그인, 백엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(127, 119, '백엔드', 'OAuth콜백', 'GET /api/auth/callback/{provider} - OAuth 인증 후 JWT 발급', false, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 120 (ProductList, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(128, 120, '공통', 'Header',     'Top navigation bar with logo, search, and cart icon',        true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 121 (ProductList, 디자인)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(129, 121, '디자인', 'Grid',       'Product cards in 3-column grid, image + name + price + rating', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 122 (ProductList, 프론트엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(130, 122, '프론트엔드', 'Pagination', 'Client-side pagination with 20 items per page, page indicator', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 123 (ProductDtl, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(131, 123, '공통', 'ImageArea',  'Product image carousel with zoom on hover, max 5 images',    true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(132, 123, '공통', 'InfoArea',   'Product name, price, description, stock status display',      true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 124 (ProductDtl, 기획)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(133, 124, '기획', 'SizeGuide',  'Size selection dropdown, size guide popup with measurement table', false, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 125 (ProductDtl, 프론트엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(134, 125, '프론트엔드', 'AddToCart', 'Add to cart button with quantity selector, optimistic UI update', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 126 (ProductDtl, 백엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(135, 126, '백엔드', 'ProductAPI', 'GET /api/products/{id} - product detail with reviews, related items', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 127 (Reviews, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(136, 127, '공통', 'ReviewList', 'Star rating + text review, sorted by newest, pagination',     true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 128 (Reviews, 프론트엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(137, 128, '프론트엔드', 'ReviewForm', 'Star selector (1-5) + textarea + image upload, 500 char limit', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 129 (Checkout, 기획)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(138, 129, '기획', 'OrderFlow',  '3-step checkout: shipping info → payment → confirmation',     true,  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 130 (Checkout, 백엔드)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(139, 130, '백엔드', 'PaymentAPI', 'POST /api/orders - validate stock, create order, return orderId', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Pin 131 (Checkout, 공통)
INSERT INTO requirements (id, pin_id, tab_type, item_name, content, is_required, created_at, updated_at) VALUES
(140, 131, '공통', 'Summary',    'Order summary sidebar: items, quantities, subtotal, tax, total', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

SELECT setval('requirements_id_seq', GREATEST((SELECT MAX(id) FROM requirements), 140));

-- ============================================
-- 9. Translation Jobs (1 completed translation for Doc 101 v2)
-- ============================================
INSERT INTO translation_jobs (id, document_version_id, status, total_languages, completed_languages, created_at, completed_at) VALUES
(101, 102, 'COMPLETED', 2, 2, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

SELECT setval('translation_jobs_id_seq', GREATEST((SELECT MAX(id) FROM translation_jobs), 101));

-- Translation Languages
INSERT INTO translation_languages (id, translation_job_id, target_language, target_user_id, status, created_at, completed_at) VALUES
(101, 101, 'en', 102, 'COMPLETED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(102, 101, 'ja', 103, 'COMPLETED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

SELECT setval('translation_languages_id_seq', GREATEST((SELECT MAX(id) FROM translation_languages), 102));

-- Translated Requirements (English translations for Doc 101 v2 requirements)
INSERT INTO translated_requirements (id, translation_language_id, requirement_id, translated_item_name, translated_content, created_at) VALUES
-- English translations (language_id = 101)
(101, 101, 101, 'Header',         'Fixed top navigation bar with logo, menu, and profile icon layout', NOW() - INTERVAL '3 days'),
(102, 101, 102, 'Footer',         'Fixed bottom area with Terms of Service, Privacy Policy, and Customer Service links', NOW() - INTERVAL '3 days'),
(103, 101, 103, 'Banner Area',    'Promotional banner slideshow, auto-rotating every 5 seconds, maximum 5 slides', NOW() - INTERVAL '3 days'),
(104, 101, 104, 'Recommended',    'User activity-based recommended content area, prioritizing recently viewed items', NOW() - INTERVAL '3 days'),
(105, 101, 105, 'Infinite Scroll','Content list infinite scroll implementation using Intersection Observer', NOW() - INTERVAL '3 days'),
(106, 101, 106, 'Skeleton UI',    'Display skeleton screen during data loading', NOW() - INTERVAL '3 days'),
(107, 101, 107, 'API Design',     'GET /api/home - Integrated response with banner, recommendations, and recent activity data', NOW() - INTERVAL '3 days'),
(108, 101, 108, 'Caching',        'Home screen data Redis caching with TTL 5 minutes', NOW() - INTERVAL '3 days'),
(109, 101, 109, 'Search Bar',     'Search input field at the top of the screen with magnifying glass icon and recent searches', NOW() - INTERVAL '3 days'),
(110, 101, 110, 'Search Results', 'Card layout with thumbnail, title, and summary in 2-column grid', NOW() - INTERVAL '3 days'),
(111, 101, 111, 'Filter UI',      'Left filter panel with checkbox and slider combination, collapsible', NOW() - INTERVAL '3 days'),
(112, 101, 112, 'Debouncing',     'Search input debouncing at 300ms, execute search after Korean character composition completes', NOW() - INTERVAL '3 days'),
(113, 101, 113, 'Profile API',    'GET /api/users/me - Returns user information and project list', NOW() - INTERVAL '3 days'),
(114, 101, 114, 'Change Password','PUT /api/users/password - Change password after verifying current password', NOW() - INTERVAL '3 days'),
(115, 101, 115, 'Profile Card',   'Display profile image, name, email, and number of teams', NOW() - INTERVAL '3 days'),
(116, 101, 116, 'Notifications',  'Chronological order, read/unread distinction, includes document edit notifications', NOW() - INTERVAL '3 days'),
(117, 101, 117, 'Notification Settings', 'Per-type ON/OFF toggle (document changes, team invites, translation complete)', NOW() - INTERVAL '3 days'),
(118, 101, 118, 'Badge',          'Unread notification count badge on header notification icon', NOW() - INTERVAL '3 days'),

-- Japanese translations (language_id = 102)
(119, 102, 101, 'ヘッダー',       '上部固定ナビゲーションバー、ロゴ＋メニュー＋プロフィールアイコン配置', NOW() - INTERVAL '3 days'),
(120, 102, 102, 'フッター',       '下部固定、利用規約・プライバシーポリシー・カスタマーサービスリンク含む', NOW() - INTERVAL '3 days'),
(121, 102, 103, 'バナーエリア',   'プロモーションバナースライド、5秒間隔自動ローリング、最大5枚', NOW() - INTERVAL '3 days'),
(122, 102, 104, 'おすすめ',       'ユーザー活動基づくおすすめコンテンツエリア、最近閲覧した項目優先', NOW() - INTERVAL '3 days'),
(123, 102, 105, '無限スクロール', 'コンテンツリスト無限スクロール実装、Intersection Observer使用', NOW() - INTERVAL '3 days'),
(124, 102, 106, 'スケルトンUI',   'データ読み込み中スケルトン画面表示', NOW() - INTERVAL '3 days'),
(125, 102, 107, 'API設計',        'GET /api/home - バナー、おすすめ、最近のアクティビティデータ統合レスポンス', NOW() - INTERVAL '3 days'),
(126, 102, 108, 'キャッシュ',     'ホーム画面データRedisキャッシュ、TTL 5分', NOW() - INTERVAL '3 days'),
(127, 102, 109, '検索バー',       '画面上部の検索入力フィールド、虫眼鏡アイコン、最近の検索履歴表示', NOW() - INTERVAL '3 days'),
(128, 102, 110, '検索結果',       'カード型レイアウト、サムネイル＋タイトル＋要約、グリッド2列配置', NOW() - INTERVAL '3 days'),
(129, 102, 111, 'フィルターUI',   '左側フィルターパネル、チェックボックス＋スライダー組合せ、折りたたみ', NOW() - INTERVAL '3 days'),
(130, 102, 112, 'デバウンス',     '検索入力デバウンス300ms、日本語変換完了後に検索実行', NOW() - INTERVAL '3 days'),
(131, 102, 113, 'プロフィールAPI','GET /api/users/me - ユーザー情報＋プロジェクトリスト返却', NOW() - INTERVAL '3 days'),
(132, 102, 114, 'パスワード変更', 'PUT /api/users/password - 現在のパスワード確認後変更', NOW() - INTERVAL '3 days'),
(133, 102, 115, 'プロフィール',   'プロフィール画像＋名前＋メール＋所属チーム数表示', NOW() - INTERVAL '3 days'),
(134, 102, 116, '通知一覧',       '時系列順、既読・未読区別、ドキュメント編集通知含む', NOW() - INTERVAL '3 days'),
(135, 102, 117, '通知設定',       '通知種類別ON/OFFトグル（ドキュメント変更、チーム招待、翻訳完了）', NOW() - INTERVAL '3 days'),
(136, 102, 118, 'バッジ',         'ヘッダー通知アイコンに未読通知数バッジ表示', NOW() - INTERVAL '3 days');

SELECT setval('translated_requirements_id_seq', GREATEST((SELECT MAX(id) FROM translated_requirements), 136));

-- ============================================
-- 10. Activity Logs
-- ============================================
INSERT INTO activity_logs (id, team_project_id, document_id, action_type, document_name, document_type, version, performed_by, created_at) VALUES
(101, 101, 101, 'UPLOADED', '메인화면',    'STORYBOARD', 1, 101, NOW() - INTERVAL '6 days'),
(102, 101, 102, 'UPLOADED', '로그인',      'WIREFRAME',  1, 101, NOW() - INTERVAL '5 days'),
(103, 101, 101, 'UPDATED',  '메인화면',    'STORYBOARD', 1, 101, NOW() - INTERVAL '5 days'),
(104, 101, 101, 'UPLOADED', '메인화면',    'STORYBOARD', 2, 101, NOW() - INTERVAL '4 days'),
(105, 101, 101, 'UPDATED',  '메인화면',    'STORYBOARD', 2, 101, NOW() - INTERVAL '3 days'),
(106, 101, 102, 'UPLOADED', '로그인',      'WIREFRAME',  2, 101, NOW() - INTERVAL '2 days'),
(107, 101, 101, 'UPLOADED', '메인화면',    'STORYBOARD', 3, 101, NOW() - INTERVAL '1 day'),
(108, 102, 103, 'UPLOADED', 'ProductPage', 'STORYBOARD', 1, 102, NOW() - INTERVAL '4 days'),
(109, 102, 103, 'UPDATED',  'ProductPage', 'STORYBOARD', 1, 102, NOW() - INTERVAL '3 days'),
(110, 102, 103, 'UPLOADED', 'ProductPage', 'STORYBOARD', 2, 102, NOW() - INTERVAL '2 days');

SELECT setval('activity_logs_id_seq', GREATEST((SELECT MAX(id) FROM activity_logs), 110));

-- ============================================
-- 11. Document Changes (change tracking for Doc 101 v2)
-- ============================================
INSERT INTO document_changes (id, document_version_id, change_type, page_number, screen_name, pin_number, item_description, before_value, after_value, modified_by, created_at) VALUES
(101, 102, 'REQUIREMENT_ADDED',    4, '알림', 1, '알림목록 요구사항 추가', NULL, '시간순 정렬, 읽음/안읽음 구분, 문서 수정 알림 포함', 101, NOW() - INTERVAL '3 days'),
(102, 102, 'REQUIREMENT_MODIFIED', 1, '홈',   1, '헤더 요구사항 내용 변경', '상단 네비게이션 바 배치', '상단 고정 네비게이션 바, 로고 + 메뉴 + 프로필 아이콘 배치', 101, NOW() - INTERVAL '3 days'),
(103, 102, 'SCREEN_MODIFIED',      2, '검색', NULL, '검색 화면 이름 변경', '서치', '검색', 101, NOW() - INTERVAL '3 days'),
(104, 102, 'REQUIREMENT_DELETED',  3, '마이페이지', 2, '사용하지 않는 요구사항 삭제', '임시 메모 항목', NULL, 101, NOW() - INTERVAL '3 days');

SELECT setval('document_changes_id_seq', GREATEST((SELECT MAX(id) FROM document_changes), 104));

-- Change Confirmations (Sarah confirmed 2 of 4 changes)
INSERT INTO change_confirmations (id, document_change_id, confirmed_by, confirmed_at) VALUES
(101, 101, 102, NOW() - INTERVAL '2 days'),
(102, 102, 102, NOW() - INTERVAL '2 days');

SELECT setval('change_confirmations_id_seq', GREATEST((SELECT MAX(id) FROM change_confirmations), 102));

-- ============================================
-- 12. Team Notifications
-- ============================================
INSERT INTO team_notifications (id, team_project_id, document_id, document_name, before_version, after_version, performed_by, created_at) VALUES
(101, 101, 101, '메인화면', 1, 2, 101, NOW() - INTERVAL '4 days'),
(102, 101, 101, '메인화면', NULL, 3, 101, NOW() - INTERVAL '1 day'),
(103, 101, 102, '로그인',   1, 2, 101, NOW() - INTERVAL '2 days'),
(104, 102, 103, 'ProductPage', 1, 2, 102, NOW() - INTERVAL '2 days');

SELECT setval('team_notifications_id_seq', GREATEST((SELECT MAX(id) FROM team_notifications), 104));

-- Notification Reads (Sarah read notification 101, Yuto read none → has unread notifications)
INSERT INTO team_notification_reads (id, notification_id, user_id, read_at) VALUES
(101, 101, 102, NOW() - INTERVAL '3 days'),
(102, 101, 103, NOW() - INTERVAL '3 days');

SELECT setval('team_notification_reads_id_seq', GREATEST((SELECT MAX(id) FROM team_notification_reads), 102));
