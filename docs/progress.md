# 프로젝트 진행 사항

프론트엔드와 백엔드의 진행 상황을 함께 기록한다. 항목에는 가능하면 요구사항 ID([requirements.md](requirements.md))를 붙인다.

## 현재 상태

| 영역 | 상태 |
|---|---|
| 공통 | 모노레포 구성 완료. MVP 요구사항 정리(SCR-001~014, FR-001~023, NFR-001~004). 미결은 SCR-002~003 범위만 남음 |
| 백엔드 | 도메인 Entity 10개, `global` 공통 클래스, 소셜 로그인(FR-018, OAuth2 + JWT 쿠키)·내 정보 API, 게시물 API(FR-008, S3 Presigned URL 업로드), 기관·개인 상담 신청 API(FR-001, FR-002) 구현. 테스트는 H2 |
| 프론트엔드 | Vite + React 프로젝트, react-router-dom 라우팅. 홈 화면(SCR-001) 시안을 정적 데이터로 구현. 로그인 모달(SCR-013)·헤더 로그인 상태, 몽키즈 소식(SCR-006) 목록·상세·작성·수정 API 연동, 기관 상담 신청 모달(SCR-009), 개인 상담 신청 2단계 모달(SCR-010) |

## 마일스톤

## 진행 로그

| 날짜 | 영역 | 내용 |
|---|---|---|
| 2026-10-07 | 백엔드 | Spring Initializr로 프로젝트 생성 |
| 2026-10-07 | 백엔드 | `CLAUDE.md`, 개발 규칙 문서(`back/docs/`) 작성 |
| 2026-10-08 | 프론트엔드 | 홈 화면 시안 구현 (SCR-001, API 미연동) |
| 2026-10-08 | 공통 | 프론트·백 모노레포로 통합, 공유 문서(`docs/`) 구성 |
| 2026-10-13 | 백엔드 | yml profile 설정(`local`, `prod`)과 `.env` 구성 추가 (빌드 검증은 DB·Entity 생성 후) |
| 2026-10-13 | 백엔드 | 기본 패키지 `com.example.demo` → `com.mongkids`, 프로젝트명 `demo` → `mongkids` 변경 |
| 2026-10-13 | 공통 | 홈 시안 기반 예상 화면 목록 작성 (SCR-002~011) |
| 2026-10-13 | 백엔드 | 테스트 DB로 H2 의존성 추가, `back/docs/testing.md` 테스트 DB 전략을 H2 기준으로 변경. `application-test.yml` 작성, `contextLoads`가 H2로 통과 |
| 2026-10-13 | 백엔드 | 로컬 MariaDB(`mongkids`)로 `local` profile DataSource 기동 검증 (`bootRun`, health `UP`) |
| 2026-10-13 | 공통 | 강사 채용 화면(SCR-007) 디자인 초안 작성 (데스크톱·모바일, 안내 + 지원 폼. 범위·문구 확정 필요) |
| 2026-10-13 | 공통 | 디자인 시안 관리 규칙 정리 (아티팩트 초안 → 확정 시 PNG 커밋, `docs/design/README.md`) |
| 2026-10-13 | 공통 | 수업 현장 화면(SCR-006) 디자인 초안 작성 (데스크톱·모바일, 사진 갤러리 + 영상. 구성 확정 필요). 초안 아티팩트를 "MongKids 화면 초안"으로 통합 |
| 2026-10-14 | 공통 | 회의록 2026-10-08, 2026-10-14 요구사항 반영 (SCR-012~014, FR-001~020, NFR-001~004) |
| 2026-10-14 | 공통 | 요구사항 미결 사항 결정 반영 (FR-002·007·008·010~013·016·019 변경, FR-020 폐기, FR-021~023 신규, SCR-006 "몽키즈 소식") |
| 2026-10-14 | 백엔드 | 요구사항 기반 DB 스키마 설계 (`back/docs/schema.md`, 테이블 10개) |
| 2026-10-14 | 백엔드 | 스키마 기반 Entity·`BaseTimeEntity`·`JpaConfig` 구현, 매핑 테스트(H2) 추가, 로컬 MariaDB에 테이블 10개 생성 |
| 2026-10-14 | 백엔드 | 소셜 로그인 구현 (FR-018, #10): OAuth2 Client + JWT HttpOnly 쿠키, `GET /api/v1/members/me`, `POST /api/v1/auth/logout`, `global` 공통 클래스(`ApiResponse`, `ErrorCode`, `BusinessException`, `GlobalExceptionHandler`) |
| 2026-10-14 | 프론트엔드 | 로그인 모달(SCR-013)·헤더 로그인 상태 구현 (FR-018, #10), `src/api/` 추가, Vite 프록시 설정 |
| 2026-10-15 | 공통 | 카카오·네이버 소셜 로그인 로컬 확인 완료 (FR-018). redirect-uri는 `{baseUrl}` 유지, 각 콘솔에 `http://localhost:5173/login/oauth2/code/{kakao\|naver}` 등록 |
| 2026-10-15 | 공통 | 배포 후 점검 체크리스트 작성 ([deploy-checklist.md](deploy-checklist.md)) |
| 2026-10-15 | 공통 | 요구사항 구조 변경: 화면(SCR)=프론트, 기능(FR)=백엔드. FR 설명을 서버 관점으로 재작성, `영역` 열 제거, FR-006 폐기(SCR-007로 이관) |
| 2026-10-15 | 공통 | 홈 확정 시안(SCR-001) PNG를 아티팩트 "MongKids 화면 초안"의 `SCR-001 홈` 페이지로 이동, `docs/design/home.png` 삭제 |
| 2026-10-15 | 공통 | 게시물 작성·수정 화면을 SCR-015로 분리해 요구사항에 추가 (구현 코드 기준, FR-008·009 관련 화면에 연결), 디자인 초안 작성 (데스크톱) |
| 2026-10-15 | 백엔드 | 게시물 API 구현 (FR-008): 사진 업로드 URL 발급(S3 Presigned PUT, 최대 20장·장당 10MB), 작성·목록·상세·수정·삭제. `global/storage`(AWS SDK v2), `PageResponse`, `spring-boot-starter-validation` 추가. 직원 권한(FR-009)은 미적용 |
| 2026-10-15 | 프론트엔드 | react-router-dom 도입, 몽키즈 소식(SCR-006) 목록·상세·작성·수정 화면 구현 (FR-008). GNB '수업 현장' → '몽키즈 소식'. 실제 S3 업로드는 버킷이 없어 미확인 |
| 2026-10-15 | 공통 | 게시물 본문을 HTML 편집기로 변경 (FR-008, 사용자 결정): 백엔드 jsoup 허용 목록 정화·사진 키 추출(P004 신설), 프론트 TipTap 편집기·사진 묶음 노드(1~3장 + 캡션)·DOMPurify 출력. 사진 갤러리·`imageKeys` 요청 필드 제거 |
| 2026-10-15 | 공통 | PR #12(FR-008) 병합. 실제 S3 업로드는 미확인이라 `feature/posts` 브랜치를 남기고 다음 할 일에 확인 항목 추가 |
| 2026-10-15 | 공통 | 게시물 작성 권한 구현 (FR-009, #14): 작성(사진 업로드 포함)은 STAFF·ADMIN, 수정·삭제는 직원인 작성자 본인 또는 ADMIN. 백엔드는 DB의 현재 역할로 검사, 프론트는 글쓰기·수정·삭제 버튼과 작성 화면 안내를 역할에 맞춤 |
| 2026-10-15 | 공통 | 기관 상담 신청 구현 (SCR-009, FR-001·003·023, #17): 디자인 초안(데스크톱 모달·모바일·접수 완료), `POST /api/v1/institution-consultations`(비회원, 동의 필수, 요일·연령 선택지를 문자열로 합쳐 저장), 헤더·히어로 버튼으로 여는 모달. 보관 기간 상담 완료 후 3년으로 결정 |
| 2026-10-15 | 공통 | 개인 상담 신청 구현 (SCR-010, FR-002·003·023, #19): 디자인 초안(데스크톱 1·2단계, 모바일 1단계), `POST /api/v1/personal-consultations`(비회원, 010 휴대전화만, 생년월일·개월 수 중 하나, 건강 메모 시 건강정보 별도 동의), 헤더·히어로 "1:1 방문체육 상담" 버튼으로 여는 2단계 모달. 번호 인증이 없어 전화번호는 `010` 고정 + 4자리 + 4자리 입력으로 결정 |

## 이슈 / 블로커

- 프론트엔드: 실제 사진·로고·아이콘 리소스가 없어 플레이스홀더와 이모지로 대체되어 있다.
- 백엔드: S3 버킷·IAM 키가 없어 `.env`에 `AWS_S3_BUCKET`, `STORAGE_PUBLIC_BASE_URL` 등이 비어 있으면 기동이 실패한다. 사진 업로드 E2E는 버킷 생성 후 확인 필요.

## 다음 할 일

### 공통
- [x] 요구사항(사용자 역할, 기능 목록) 정리
- [ ] SCR-002~003 범위·구성 확정

### 백엔드
- [x] DataSource / profile 설정
- [x] 로컬 DB로 DataSource 설정 검증 (`gradlew bootRun`)
- [x] 패키지 `com.example.demo` → `com.mongkids` 변경
- [x] `src/test/resources/application-test.yml` 작성 + 테스트에 `@ActiveProfiles("test")` 적용 (H2로 `contextLoads` 통과 확인)
- [x] `global/` 공통 클래스 구현
- [x] 소셜 로그인 (FR-018)
- [x] 카카오·네이버 실제 키로 로그인 확인 (로컬)
- [ ] 배포 후 소셜 로그인 점검 ([deploy-checklist.md](deploy-checklist.md))
- [ ] 가입 정보 입력 (FR-019)
- [x] 게시물 저장·조회·수정·삭제 (FR-008)
- [ ] 게시물 사진 S3 실제 업로드 확인 (FR-008, PR #12 병합 후 미확인). 확인이 끝날 때까지 `feature/posts` 브랜치 유지 ([deploy-checklist.md](deploy-checklist.md) H)
  - [ ] S3 버킷 CORS(`PUT`, `http://localhost:5173`), CloudFront(OAC) 구성 → `.env`의 `STORAGE_PUBLIC_BASE_URL` 설정
  - [ ] 로그인 후 `/news/write`에서 사진 묶음 업로드 → 저장 → 목록 대표 사진·상세 본문 사진 표시
  - [ ] 수정(사진 교체·순서 변경)·삭제, 21장·10MB 초과·이미지가 아닌 파일 거부
  - [ ] 확인이 끝나면 `feature/posts` 브랜치(로컬·원격) 삭제
- [x] 게시물 작성 직원 권한 (FR-009, #14): 작성은 STAFF·ADMIN, 수정·삭제는 직원인 작성자 본인 또는 ADMIN (DB 역할 기준)
- [ ] 직원 권한 부여·회수 (FR-022). 그 전까지 로컬은 `UPDATE member SET role = 'STAFF' WHERE id = ?`
- [ ] 수정·삭제로 빠지거나 업로드 후 버려진 S3 사진 정리 (수명 주기 규칙 등)
- [x] 요구사항 기반 DB 스키마 설계 (`back/docs/schema.md`)
- [x] 스키마 기반 Entity 구현, 로컬 MariaDB 테이블 생성 (`ddl-auto: update`)
- [x] 기관 상담 신청 저장 (FR-001, #17)
- [x] 개인 상담 신청 저장 (FR-002, #19)
- [ ] 상담 접수 메일 알림 (FR-005), 상담 신청 조회 (FR-004)
- [ ] 상담 신청 보관 기간(상담 완료 후 3년) 지난 데이터 삭제 배치 (FR-023)

### 프론트엔드
- [ ] 실제 이미지·로고 리소스 반영
- [x] 라우팅 도입 (react-router-dom)
- [x] 몽키즈 소식 화면 (SCR-006, FR-008)
- [ ] 나머지 하위 페이지 구성
- [x] `/api` 프록시 설정
- [x] 로그인 모달 (SCR-013, FR-018)
- [x] 기관 상담 신청 모달 (SCR-009, #17)
- [x] 개인 상담 신청 모달 (SCR-010, #19)
- [ ] 나머지 API 연동
