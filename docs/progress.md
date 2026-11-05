# 프로젝트 진행 사항

프론트엔드와 백엔드의 진행 상황을 함께 기록한다. 항목에는 가능하면 요구사항 ID([requirement_homepage.md](requirements/requirement_homepage.md), [requirement_employee.md](requirements/requirement_employee.md))를 붙인다.

## 현재 상태

| 영역 | 상태 |
|---|---|
| 공통 | 모노레포 구성 완료. 요구사항은 프로젝트별 문서: 홈페이지(SCR-001~026, SCR-004·005·021 결번, FR-001~031, FR-006·020 폐기, NFR-001~004), 근태 관리(EMP-SCR-001~006, EMP-SCR-003 폐기, EMP-FR-001~016, EMP-NFR-001~002). 디자인 초안은 아티팩트 "MongKids 화면 초안"(확정 PNG 내보내기 남음). 운영: 프론트 S3 + CloudFront(`https://mongkiz.com`, NFR-004), 백엔드 EC2 + Caddy + RDS MariaDB(CloudFront `/api`·OAuth 경로), GitHub Actions 경로 필터 자동 배포(2026-10-22~). 홈페이지 MVP 화면은 강사 채용(SCR-007) 등 일부를 빼고 운영 중, 근태 관리는 직원·관리자 화면까지 운영 배포(2026-11-02) |
| 백엔드 | 도메인 Entity와 `global` 공통 클래스, 소셜 로그인(FR-018, OAuth2 + JWT 쿠키)·내 정보·SNS 수신 동의·회원 탈퇴(FR-019·027·028, `{kakao,naver}-withdraw` 재인증), 게시물(FR-008, S3 Presigned URL)·댓글(FR-010)·공감(FR-011)·몽키즈 연구소 글(`/api/v1/lab-posts`, 제목 검색 FR-024), 기관·개인 상담 신청(FR-001·002), 후기·관리자 답글(FR-013~017, FR-025), 관리자 API(`/api/v1/admin/**`: 상담 조회·처리 상태, 후기 숨김, 직원 권한, FR-004·021·022·026), 신규 상담 건수 SSE(FR-005). 근태 관리 `attendance` 도메인: 수업 시작(위치 반경 100m)·수동 등록·승인·수업 종료·당일 정정·날짜별 내 수업 기록(EMP-FR-001~003·010·011·013~015), 기관 위치·수업 할당(EMP-FR-004·012). 테스트는 H2(181개) |
| 프론트엔드 | Vite + React, react-router-dom. 홈(SCR-001, 기획 v4 구성, 후기 API 연동), 로그인 모달(SCR-013)·헤더 로그인 상태, 수업 현장(SCR-006)·몽키즈 연구소(SCR-024)·작성·수정(SCR-015) API 연동(댓글·공감), 기관·개인 상담 신청 모달(SCR-009·010), 수업 후기(SCR-012, 사진 크게 보기), 관리자 화면(SCR-014, `/admin`: 상담·후기·회원, 신규 상담 배지, 근태 관리 메뉴 EMP-SCR-006·카카오맵), 내 정보(SCR-025, `/me`). 정적 화면: ABOUT 몽키즈(SCR-002), 체육/행사 프로그램(SCR-003·016~020), 자주 묻는 질문(SCR-022), 개인정보처리방침(SCR-023, `/privacy`). 근태 직원 화면 `/attendance`(EMP-SCR-001: 수업 시작·종료 체크, 날짜별 수업 기록) |

## 마일스톤

## 진행 로그

진행 로그는 주별 파일(`docs/progress/YYYY-Www.md`, ISO 주차, 월요일 시작)에 쓴다. 새 항목은 날짜가 속한 주 파일 표 끝에 추가하고, 그 주 파일이 없으면 만들고 아래 목록에 넣는다.

| 주차 | 기간 | 주요 내용 |
|---|---|---|
| [2026-W45](progress/2026-W45.md) | 11-02 ~ 11-08 | 근태 날짜별 수업 기록(#71), 근태 관리자 운영 배포(#70), 개인정보처리방침(SCR-023, #74), 회의록 2026-11-05 요구사항 반영(견적 신청·특수 체육 등) |
| [2026-W44](progress/2026-W44.md) | 10-26 ~ 11-01 | 상담 접수 알림 SSE(FR-005), 후기 카드 개편, 내 정보·회원 탈퇴(#54), 근태 요구사항·ADR-006, 수업 시작·종료(#61), 근태 관리자 API·화면(#63) |
| [2026-W43](progress/2026-W43.md) | 10-19 ~ 10-25 | 디자인 초안(SCR-002·003·012·016~024), 연구소·댓글·공감·후기, 정적 화면, 운영 배포·CI/CD, 관리자 화면(SCR-014) |
| [2026-W42](progress/2026-W42.md) | 10-12 ~ 10-18 | 요구사항 정리, DB 스키마·Entity, 소셜 로그인, 게시물 API·화면, 상담 신청, 홈 기획 v4 반영 |
| [2026-W41](progress/2026-W41.md) | 10-05 ~ 10-11 | 프로젝트 생성, 모노레포 통합, 홈 시안 |

## 이슈 / 블로커

- 프론트엔드: 소개 대표 사진(SCR-002)과 운동회·공공기관 상세 사진(SCR-019·020)은 리소스가 없어 자리 표시(`ImagePlaceholder`)로 남아 있다.
- 백엔드: 로컬 `.env`에 `AWS_S3_BUCKET`, `STORAGE_PUBLIC_BASE_URL` 등이 비어 있으면 기동이 실패한다.

## 다음 할 일

### 공통
- [x] 프론트 정적 배포·도메인 연결 (NFR-004, 2026-10-22, `https://mongkiz.com`)
- [x] 백엔드 서버 배포 (2026-10-22, EC2 + Caddy, RDS, [deploy-checklist.md](deploy-checklist.md) 운영 리소스)
- [x] CloudFront `E1AWEJNTIYBX5U`에 `/api/*`, `/oauth2/*`, `/login/oauth2/*` 동작 추가 (2026-10-22, [deploy-checklist.md](deploy-checklist.md) A)
- [x] 카카오·네이버 개발자 콘솔에 운영 Redirect URI 등록, 운영 키를 사업자 계정 앱으로 교체 (2026-10-22, 사용자, [deploy-checklist.md](deploy-checklist.md) D)
- [ ] 운영 `KAKAO_*`, `NAVER_*` 파라미터를 SecureString으로 다시 만들기 (지금은 String)
- [x] GitHub Actions 프론트 자동 배포·백엔드 CI (2026-10-22, [git-workflow.md](git-workflow.md) 4)
- [x] GitHub Actions 백엔드 배포 단계 (2026-10-22, 병합 후 첫 실행 확인 필요)
- [x] 사진용 S3 버킷·CORS·CloudFront(OAC) (2026-10-22, [deploy-checklist.md](deploy-checklist.md) H)
- [x] AWS 유료 플랜 전환 후 RDS 백업 보관 1일 → 7일 (2026-10-22)
- [ ] AWS Activate Founders 크레딧 신청 (사용자, 업무용 이메일 사용)
- [ ] 운영 DB 조회 전용 계정 검토 (`jaeyoung`은 전체 권한)
- [ ] 운영 스키마 마이그레이션 방식 결정 (Flyway 도입 검토, [back/docs/database.md](../back/docs/database.md#ddl-auto-정책))
- [x] 요구사항(사용자 역할, 기능 목록) 정리
- [x] SCR-002 구성 확정 (디자인 초안 승인, 2026-10-20)
- [x] SCR-003 범위·구성 확정 (2026-10-20, 기획 v4 기준 디자인 초안)
- [x] SCR-024 몽키즈 연구소 범위 결정 (2026-10-20, FR-008·009·012 변경, FR-024 신설)
- [ ] 동시 접속 30명 부하 확인 (NFR-001)
- [x] 근태 관리 관리자 화면(EMP-SCR-006: 기관 위치 등록·수업 할당·수업 시작 승인·기록 조회) 구현 (#63, 2026-10-30)
- [ ] #63 확인·배포: 카카오맵 활성화·도메인 등록, 운영 빌드 키(`front.yml` + 저장소 Variables)는 완료(2026-11-02). 남은 것: 로그인한 운영 화면 점검(기관명 검색·지도·수업 할당·승인), EMP-SCR-006 시안 PNG 내보내기 ([deploy-checklist.md](deploy-checklist.md) M)
- [ ] 근태 관리자 개선 (EMP-FR-012, EMP-SCR-006, 2026-11-02 추가): 기관 삭제 시 수업·기록 함께 삭제 + 삭제 확인 창(동의), 기관 등록·수정 모달 바깥 클릭 닫힘 제거
- [ ] 수업 기록 60일 보관 후 자동 삭제 (EMP-FR-016, 예정): 미결 사항(대상 범위·사진 S3·기준일·주기) 정한 뒤 착수
- [x] #61 병합 전: 운영 DDL 적용, IAM `attendance/*` 권한 (2026-10-29, [deploy-checklist.md](deploy-checklist.md) K)
- [ ] #61 배포 후 실제 기관·수업 시드 입력 ([deploy-checklist.md](deploy-checklist.md) K)
- [ ] #61 배포 후 사용자 직접 테스트: 테스트 계정에 테스트 기관·수업을 할당해 위치 판정 정확성과 기능 확인, 실제 휴대폰(아이폰 Safari, 안드로이드 Chrome·삼성 인터넷) 점검 ([deploy-checklist.md](deploy-checklist.md) K, K-1)
- [ ] EMP-SCR-001 시안 확정 PNG 내보내기 → `docs/design/` (사용자)
- [ ] SCR-024 디자인 초안 확정 (PNG 내보내기)
- [ ] 회의록 2026-11-05 반영 작업 (11/11 MVP 전)
  - [x] 프론트 (2026-11-05): 운영 지역 성남 추가(SCR-001·002·022), 연락처 010-3923-0124(푸터·플로팅·SCR-023), 헤더 로고 확대 시험(SCR-001), SCR-003 소개 버튼 제거·특수 체육 카드 2×2, 특수 체육 상세(SCR-026)·다른 프로그램 보기·FAQ 탭, 개인 상담 건강 예시 문구(SCR-010), 강사 카드 전담 분야(SCR-002), 현장 사진 탭 삭제·게시물 상세 사진 크게 보기(SCR-006·015), 후기 유형 삭제(SCR-012·014), 견적 신청 모달(SCR-011)·SCR-019 버튼
    - [ ] 남은 항목: 견적 요청 실제 저장 확인(백엔드 FR-031 후), 후기 저장 요청의 `CLASS` 고정 제거(백엔드 유형 삭제 후), 헤더 로고 크기 확정, 특수 체육 문구·FAQ 초안 대표 확인, 홈 요약 띠 지역 표기("용인·수원·경기광주") 정리 여부
  - [x] 백엔드 (2026-11-05): 견적 신청 저장(FR-031)·관리자 조회 유형 추가(FR-004·005·026), 후기 유형 삭제와 기존 무료체험 후기 변환(FR-013·021·025), 현장 사진 분류 삭제와 기존 게시물 게시글로 이동(FR-008)
    - [ ] 남은 항목: 운영 DDL 1단계 적용(2026-11-05 완료, 사용자) 후 #77 배포, 2단계(`review.type` 삭제) 적용 완료(2026-11-05, 사용자), 프론트 `type: 'CLASS'` 제거 완료(2026-11-05) ([deploy-checklist.md](deploy-checklist.md) N)
  - [x] 받을 자료: 홈 히어로 새 사진, 만족도 새 아이콘 (2026-11-05 적용)
- [ ] 화면 구성 신규 기획 바탕으로 수정
  - [x] 홈(SCR-001) 공통 요소·섹션 (2026-10-18)
  - [ ] 기획 v4의 하위 페이지(프로그램 상세 6종, 연구소, FAQ, 수업 현장) 범위 결정 → 요구사항 반영
    - [x] 예상 추가 화면을 기존 화면과 병합·제거 정리 (사용자). SCR-016~020·022 정리, SCR-023(개인정보처리방침) 구현·배포 (#74, 2026-11-03)
  - [ ] 대표 확인 필요 항목: 공식 연락처·사업자 정보·카카오톡 채널, 실제 사진(게시 동의), 실제 후기, 출강기관 수(기준일), SCR-002 빈칸 문구(강사 교육 과정, 커리큘럼 예시, 대체강사·안전 기준, 강사 정보·사진), SCR-022·016~020 FAQ 문구(기획 v4 기준) 사실 확인

### 백엔드
- [x] DataSource / profile 설정
- [x] 로컬 DB로 DataSource 설정 검증 (`gradlew bootRun`)
- [x] 패키지 `com.example.demo` → `com.mongkids` 변경
- [x] `src/test/resources/application-test.yml` 작성 + 테스트에 `@ActiveProfiles("test")` 적용 (H2로 `contextLoads` 통과 확인)
- [x] `global/` 공통 클래스 구현
- [x] 소셜 로그인 (FR-018)
- [x] 카카오·네이버 실제 키로 로그인 확인 (로컬)
- [ ] 배포 후 소셜 로그인 점검 ([deploy-checklist.md](deploy-checklist.md) G): 카카오 완료(2026-10-22, 사용자), 네이버 남음
- [ ] 가입 정보 입력 (FR-019). SNS 수신 동의 변경은 내 정보에서 가능(#54), 전화번호·이메일은 네이버 동의항목으로 저장(#54)
- [ ] 카카오 이메일·휴대폰 번호 (FR-019): 비즈 앱 전환·동의항목 `account_email`·`phone_number`(필수) 켜기(로컬·운영, 사용자, 2026-11-03), 비즈 앱 승인 완료(2026-11-05), `application.yml` 카카오 scope 추가(#75). 남은 것: 로컬·운영 카카오 로그인 확인 ([deploy-checklist.md](deploy-checklist.md) L)
- [ ] 회원 탈퇴 (FR-028, #54) 실제 확인: 카카오·네이버 콘솔에 `-withdraw` Redirect URI 등록(사용자) → 로컬 DB에도 `member-withdraw-ddl.sql` 적용 → 로컬·운영 탈퇴 점검 ([deploy-checklist.md](deploy-checklist.md) L)
- [x] 게시물 저장·조회·수정·삭제 (FR-008)
- [ ] 게시물 사진 S3 실제 업로드 확인 (FR-008, 운영 업로드·표시 확인 2026-10-27, 수정·거부 케이스 남음) ([deploy-checklist.md](deploy-checklist.md) H)
  - [x] 운영 S3 버킷 CORS(`PUT`, `https://mongkiz.com`, `http://localhost:5173`), CloudFront(OAC) 구성 (2026-10-22). 로컬 `.env`는 개발 버킷 기준으로 따로 설정
  - [x] 로그인 후 `/news/write`에서 사진 묶음 업로드 → 저장 → 목록 대표 사진·상세 본문 사진 표시 (운영, 2026-10-27, 사용자)
  - [x] 몽키즈 연구소 `/lab/write`도 연구소 업로드 URL(`lab/` 키)로 사진 업로드 → 저장 → `/lab` 카드 대표 이미지·상세 본문 사진 표시, 수정 시 `lab_post_image` 교체 (FR-008, #22)
  - [ ] 수정(사진 교체·순서 변경)·삭제, 21장·10MB 초과·이미지가 아닌 파일 거부
  - [x] `feature/posts` 브랜치(로컬·원격) 삭제 (2026-10-27, #12 squash 병합본과 내용 같음 확인)
- [x] 게시물 작성 직원 권한 (FR-009, #14): 작성은 STAFF·ADMIN, 수정·삭제는 직원인 작성자 본인 또는 ADMIN (DB 역할 기준)
- [x] 직원 권한 부여·회수 (FR-022, #43): 관리자 화면에서 지정·해제. 관리자(ADMIN) 지정은 여전히 DB에서 `UPDATE member SET role = 'ADMIN' WHERE id = ?`
- [x] 운영 DB에 `ALTER TABLE consultation ADD COLUMN completed_at DATETIME(6) NULL;` 적용 (2026-10-24, 사용자, #44 병합 전)
- [x] 연구소 글 사진을 `lab/` 경로로 분리 (FR-008, 사용자 결정 2026-10-24): 전용 업로드 URL, 접두사 검사, 프론트 업로드 호출
  - [x] 운영 IAM에 `lab/*` 권한 추가 (운영 연구소 사진 업로드 확인, 2026-10-27) ([deploy-checklist.md](deploy-checklist.md) H)
  - [ ] 분리 전 `posts/`에 올라간 연구소 사진을 옮길지 정하기. 지금은 그 글을 수정할 때만 유지 허용 ([ADR-002](adr/ADR-002-s3-orphan-photo-cleanup.md))
- [ ] 수정·삭제로 빠지거나 업로드 후 버려진 S3 사진 정리. 방식 검토 중 ([ADR-002](adr/ADR-002-s3-orphan-photo-cleanup.md))
- [x] 요구사항 기반 DB 스키마 설계 (`back/docs/schema.md`)
- [x] 스키마 기반 Entity 구현, 로컬 MariaDB 테이블 생성 (`ddl-auto: update`)
- [x] 기관 상담 신청 저장 (FR-001, #17)
- [x] 개인 상담 신청 저장 (FR-002, #19)
- [x] 몽키즈 연구소 글 저장·목록·상세·수정·삭제 (FR-008, #22): `lab_post` 별도 테이블, `/api/v1/lab-posts`
- [x] 연구소 글 제목 검색 (FR-024, #22): `GET /api/v1/lab-posts?keyword=`
- [x] 게시물 댓글 작성·목록·삭제 (FR-010, #26)
- [x] 게시물·연구소 글 공감 API (FR-011, #27)
- [ ] 비로그인 열람 제한 (FR-012). 후기는 완료(#31), 수업 현장 게시물만 남음. 연구소 글에는 적용하지 않음
- [x] 후기 작성·목록(무한 스크롤)·사진·수정·삭제 (FR-013~016, FR-025, #31)
- [x] 후기 관리자 답글 (FR-017, #32): `PUT /api/v1/reviews/{reviewId}/reply`
- [x] 후기 사진 S3 실제 업로드 확인 (`reviews/` 키, 2026-10-22, 개발용 버킷, 사용자)
- [x] 상담 신청 조회 (FR-004)·처리 상태 (FR-026), 후기 숨김 (FR-021) (#43)
- [x] 상담 접수 알림 (FR-005, #46): 헤더·상담 신청 탭 신규 건수 배지, SSE 즉시 반영 ([ADR-003](adr/ADR-003-fast-consultation-check.md), [ADR-004](adr/ADR-004-admin-web-notification-transport.md))
  - [x] 운영 배포 뒤 CloudFront → Caddy 경로에서 SSE 유지 확인 (2026-10-26, #47)
  - [ ] 운영 재배포 뒤 자동 재연결 확인, 초기 몇 주 끊김·재연결 빈도 관찰 ([deploy-checklist.md](deploy-checklist.md) I)
- [x] 권한 검사가 요청마다 DB에서 회원을 조회하는 방식 검토 (2026-10-27, 사용자 결정). 관리자 API(`MemberService.validateAdmin`)와 게시물 작성·수정·삭제(FR-009)는 매 요청 `member`를 읽어 직원 해제(FR-022)·탈퇴를 바로 반영한다. SSE(FR-005)는 연결·재연결(30분마다) 때만 조회하므로, 연결 중 권한 변경은 재연결 전까지 반영되지 않는다. 결론: 토큰 만료가 1일로 길어 DB 역할 검사 유지. JWT에는 `role` 클레임이 이미 들어 있고, 관리자 API URL 단계 `hasRole("ADMIN")` 이중 검사는 보류
- [ ] 상담 신청 보관 기간(상담 완료 후 3년, `completed_at` 기준) 지난 데이터 삭제 배치 (FR-023)

### 프론트엔드
- [ ] 실제 이미지·로고 리소스 반영
- [x] 라우팅 도입 (react-router-dom)
- [x] 수업 현장 화면 (SCR-006, FR-008)
- [x] 수업 현장 상세 댓글 (SCR-006, FR-010, #26), 본인 댓글 "내가 쓴 댓글" 뱃지. 로컬 QA 완료 (2026-10-21)
- [x] 수업 현장·연구소 상세 공감 버튼 (SCR-006, SCR-024, FR-011, #27), 목록 카드 우측 상단 공감 수. 로컬 QA 완료 (2026-10-21)
- [x] ABOUT 몽키즈 화면 (SCR-002, 디자인 초안 기준, 빈칸 문구는 대표 확인 후 `aboutContent.js` 갱신)
- [ ] 나머지 하위 페이지 구성
- [x] 몽키즈 연구소 화면 (SCR-024, #22): `/lab` 목록·검색·페이지, 상세·작성·수정은 수업 현장 화면 재사용. 로컬 QA 완료 (2026-10-20)
- [x] 게시물 작성·수정(SCR-015)에 연구소 분류와 작성자 안내 추가 (#22)
- [x] 자주 묻는 질문 화면 (SCR-022, 정적 데이터. 홈 FAQ 미리보기·프로그램 상세 FAQ와 데이터 공유)
- [x] 수업 후기 화면 (SCR-012, #31): 목록 무한 스크롤, 작성·수정 모달, 삭제 확인, 비로그인 4개 안내
- [x] 수업 후기 로컬 화면 QA (사용자, #31, 2026-10-21). 예시 후기는 로컬 DB에 남아 있음, 필요 없으면 삭제: `DELETE FROM review_image WHERE review_id IN (2,3,4,5,6); DELETE FROM review WHERE id IN (2,3,4,5,6);`
- [x] 홈 후기 섹션(SCR-001)에 후기 API 연동 (2개씩 옆으로 넘기기, #31)
- [x] 체육/행사 프로그램 화면 (SCR-003 전체 안내, SCR-016~020 상세 공통 템플릿, 정적 데이터)
- [x] `/api` 프록시 설정
- [x] 로그인 모달 (SCR-013, FR-018)
- [x] 기관 상담 신청 모달 (SCR-009, #17)
- [x] 개인 상담 신청 모달 (SCR-010, #19)
- [ ] 나머지 API 연동
- [x] 플로팅 메뉴 카카오톡 상담 → 후기 보러가기 (SCR-001, #50)
  - [ ] 플로팅 메뉴 추가 변경: 2026-10-29 회의 결과 반영
