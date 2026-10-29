# 프론트엔드 아키텍처

## 1. 폴더 구조

```
front/
├─ index.html                 진입 HTML (lang, 메타, 웹폰트)
├─ public/                    정적 파일 (favicon 등, 경로 그대로 서빙)
└─ src/
   ├─ main.jsx                React 마운트
   ├─ App.jsx                 레이아웃(Header, Footer, FloatingMenu, MobileCtaBar) + 라우트(react-router-dom), 로그인 상태, 모달(로그인·기관 상담) 열림 상태, 화면 이동 시 맨 위 또는 #앵커로 스크롤
   ├─ index.css               전역 리셋, 디자인 토큰, 공통 클래스(.container, .section-heading)
   ├─ pages/                  화면 단위. 섹션 컴포넌트를 조립만 한다
   │  ├─ HomePage.jsx         /
   │  ├─ AboutPage.jsx        /about (SCR-002)
   │  ├─ ProgramsPage.jsx     /programs (SCR-003, #special)
   │  ├─ ProgramDetailPage.jsx /programs/:programId (SCR-016~020, 없는 id는 /programs)
   │  ├─ FaqPage.jsx          /faq (SCR-022)
   │  ├─ NewsListPage.jsx     /news (SCR-006, ?category=)
   │  ├─ NewsDetailPage.jsx   /news/:postId, /lab/:postId (board 설정으로 재사용)
   │  ├─ NewsEditorPage.jsx   /news/write, /news/:postId/edit, /lab/write, /lab/:postId/edit
   │  ├─ LabListPage.jsx      /lab (SCR-024, ?category=&keyword=&page=)
   │  └─ boards.js            상세·작성 화면이 쓰는 게시판 설정 (newsBoard, labBoard: 목록 주소·분류·API·안내 문구)
   ├─ components/
   │  ├─ layout/              모든 화면에 공통으로 붙는 영역 (Header, Footer, FloatingMenu, MobileCtaBar: 640px 이하 하단 고정 상담 바)
   │  ├─ home/                홈 화면 전용 섹션 (Hero, StatsBar, ServiceCards, WhyMongkids, ClassGallery, Reviews, ProcessSteps, SpecialPrograms, FaqPreview, ContactCta)
   │  ├─ about/               ABOUT 몽키즈 (AboutHero, AboutStandards, AboutPrinciple, AboutWhy, TeacherList, SCR-002)
   │  ├─ program/             체육/행사 프로그램 전체 안내·상세 (ProgramsHero, ProgramGroup, ProgramCard, ProgramIcon, ProgramProcess, StepList / ProgramBreadcrumb, ProgramHero, ProgramGuide, ProgramFaq, OtherPrograms, SCR-003·016~020)
   │  ├─ faq/                 자주 묻는 질문 (FaqList, FaqTabs, SCR-022)
   │  ├─ auth/                로그인·회원가입 모달 (LoginModal, SCR-013)
   │  ├─ news/                수업 현장 (목록·카드·상세·본문 출력·작성 폼·HTML 편집기·사진 묶음 노드, SCR-006). 상세·작성 폼·분류 탭·칩은 categories prop으로 연구소도 쓴다
   │  ├─ lab/                 몽키즈 연구소 목록 (LabHero 검색, LabList, LabCard, Pagination, SCR-024)
   │  ├─ consultation/        상담 신청 모달 (InstitutionConsultationModal SCR-009, PersonalConsultationModal SCR-010, 공용 ConsultationFormParts)
   │  └─ common/              화면과 무관한 재사용 UI (Button: yellow·blue·ghost, to면 링크 / SectionHeading: align left·center, Highlight, ImagePlaceholder, PageHero: 하위 화면 크림 소개 영역, ConsultCta: 하단 상담 띠, FaqAccordion: 여러 개 펼침 질문 목록, ChevronIcon)
   ├─ assets/                 번들에 포함되는 이미지 (logo.png: 원본 로고에서 여백을 잘라낸 버전)
   ├─ api/                    API 호출 (client.js 공통 fetch, auth.js 소셜 로그인, posts.js 게시물, labPosts.js 연구소 글, consultations.js 상담 신청)
   └─ data/                   화면 콘텐츠 (homeContent.js, aboutContent.js, programContent.js, faqContent.js, authContent.js, newsContent.js, labContent.js, consultationContent.js)
```

- 새 화면이 생기면 `pages/{Name}Page.jsx`와 `components/{화면}/`을 추가한다.
- 두 화면 이상에서 쓰이면 `components/common/`으로 옮긴다.

## 2. 의존 방향

```
pages → components/{화면} → components/common
              ↓
            data
```

- `common` 컴포넌트는 `data`를 직접 import하지 않는다. 필요한 값은 props로 받는다.
- `pages`는 섹션 순서와 API 호출·화면 상태만 맡는다. 마크업·스타일을 직접 갖지 않는다.
- 라우팅은 `App.jsx`의 `<Routes>`에 둔다. GNB 항목은 `homeContent.js`의 `navItems`에서 `to`(라우트)나 `href`(아직 화면 없는 앵커)로 구분한다.

## 3. 데이터와 API 연동 방침

- 홈 콘텐츠는 `src/data/homeContent.js`의 정적 데이터다. 로그인 상태(FR-018)와 수업 현장(FR-008)은 API로 받는다.
- 기관 상담 신청(FR-001)은 로그인 없이 `createInstitutionConsultation()`으로 보낸다. 모달은 `App.jsx`가 열고, 헤더·히어로의 "기관 프로그램 문의" 버튼이 `onInstitutionConsultClick`으로 연다. 요일·연령 선택지의 `value`는 백엔드 enum 이름과 같다(`consultationContent.js`).
- 개인 상담 신청(FR-002)은 `createPersonalConsultation()`으로 보낸다. 헤더·히어로의 "1:1 방문체육 상담" 버튼이 `onPersonalConsultClick`으로 `PersonalConsultationModal`을 연다. 2단계(기본 정보 → 아이 이야기·동의)이며, 전화번호는 `010` 고정 + 4자리 두 칸을 합쳐 보낸다. 서버 필드 오류가 1단계 항목이면 1단계로 돌아간다. 모양은 `InstitutionConsultationModal.css`의 `consult-modal__*`를 같이 쓰고 `PersonalConsultationModal.css`는 파란 강조·단계 표시만 더한다.
- 두 모달이 같이 쓰는 칩 묶음(`ChipGroup`, 배열이면 여러 개·값이면 하나 선택)과 필드 오류(`FieldError`)는 `ConsultationFormParts.jsx`에 둔다.
- 로그인 상태 `member`는 `undefined`(확인 중) / `null`(비로그인) / 객체로 구분한다. 작성 화면은 확인이 끝난 뒤 로그인 여부를 판단한다.
- 게시물 권한(FR-009)은 `components/news/postPermission.js`의 `canWritePost(member)`(STAFF·ADMIN), `canManagePost(member, post)`(직원인 작성자 본인 또는 ADMIN)로 판단한다. 화면 표시용이며 실제 검사는 서버가 한다.
- 게시물 본문은 TipTap 편집기(`PostContentEditor`)가 만든 HTML이다. 허용 서식은 굵게·기울임·밑줄·링크·목록·인용·구분선이고, 사진은 커스텀 노드 `photoGroup`(`<figure class="post-photos">`, 1~3장 + 캡션)으로 넣는다.
  - 사진은 고르는 즉시 `uploadPostImages()`가 업로드 URL을 받아 S3에 직접 `PUT`하고, 노드에는 객체 키(`data-image-key`)만 남는다 (흐름은 [백엔드 아키텍처](../../back/docs/architecture.md#8-파일-저장소-fr-008)).
  - 상세 화면(`PostContent`)은 서버가 정화한 HTML을 DOMPurify로 한 번 더 거른 뒤 출력한다. 본문 스타일은 편집기와 같은 `PostBody.css`(`.post-body`)를 쓴다.
  - 작성·수정 화면은 편집기 번들이 커서 `React.lazy`로 불러온다.
- 몽키즈 연구소(SCR-024)는 수업 현장과 API만 다르다. 상세·작성·수정 페이지는 `board` prop(`pages/boards.js`의 `labBoard`)으로 목록 주소(`/lab`), 분류(`labCategories`), API(`api/labPosts.js`), 본문 아래 참고용 안내·작성자 안내를 바꿔 쓴다. 사진 업로드는 `board.uploadImages`로 바꿔 쓴다 (연구소는 `uploadLabPostImages()`, `lab/` 경로).
  - 목록은 분류·검색어·페이지를 주소에 둔다(`?category=&keyword=&page=`, 기본값은 뺌). 한 페이지 9개(3열 × 3줄), 768px 이하는 썸네일 + 제목 목록.
  - 모바일 하단 상담 바를 두지 않는 화면은 `App.jsx`의 `NO_MOBILE_CTA_PATHS`에 둔다. 바를 숨기면서 `<html>`에 `no-mobile-cta`를 붙여 바 높이 여백(`--mobile-cta-height`)도 0으로 만든다.
- 로그인 상태는 `App.jsx`가 `fetchMyInfo()`로 조회해 `Header`에 넘긴다. 로그인 실패 시 백엔드가 `/?loginError=true`로 돌려보내면 모달을 열고 오류 문구를 보여 준다.
- 데이터 구조는 백엔드 응답 형태를 염두에 두고 `id`를 가진 객체 배열로 유지한다.
- API 연동 시:
  - 호출 코드는 `src/api/`에 두고, 컴포넌트는 응답 데이터만 받는다. `request()`는 실패 시 `ApiError`(status, code)를 던진다.
  - 개발 서버는 `vite.config.js`의 `server.proxy`로 `/api`, `/oauth2`, `/login/oauth2`를 백엔드(8080)에 연결한다. 인증 쿠키가 같은 출처로 오가야 하므로 `changeOrigin`은 끈다.
  - 응답 형식은 백엔드 규칙([../../back/docs/api-guidelines.md](../../back/docs/api-guidelines.md))의 `ApiResponse<T>`를 따른다.
- 어떤 데이터를 API로 바꿀지는 [요구사항](../../docs/requirements/requirement_homepage.md)의 기능 ID 단위로 결정한다.

## 4. 반응형 기준

| 폭 | 기준 |
|---|---|
| ~1100px | 헤더가 햄버거 메뉴로 전환 |
| ~1024px | 다단 그리드 축소 (서비스 카드 1열, WHY 3열 등) |
| ~768px | 모바일 레이아웃 (세로 배치, 플로팅 메뉴 축소) |
