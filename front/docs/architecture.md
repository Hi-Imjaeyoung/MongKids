# 프론트엔드 아키텍처

## 1. 폴더 구조

```
front/
├─ index.html                 진입 HTML (lang, 메타, 웹폰트)
├─ public/                    정적 파일 (favicon 등, 경로 그대로 서빙)
└─ src/
   ├─ main.jsx                React 마운트
   ├─ App.jsx                 레이아웃(Header, Footer, FloatingMenu, MobileCtaBar) + 라우트(react-router-dom), 로그인 상태, 모달(로그인·기관 상담) 열림 상태
   ├─ index.css               전역 리셋, 디자인 토큰, 공통 클래스(.container, .section-heading)
   ├─ pages/                  화면 단위. 섹션 컴포넌트를 조립만 한다
   │  ├─ HomePage.jsx         /
   │  ├─ NewsListPage.jsx     /news (SCR-006, ?category=)
   │  ├─ NewsDetailPage.jsx   /news/:postId
   │  └─ NewsEditorPage.jsx   /news/write, /news/:postId/edit
   ├─ components/
   │  ├─ layout/              모든 화면에 공통으로 붙는 영역 (Header, Footer, FloatingMenu, MobileCtaBar: 640px 이하 하단 고정 상담 바)
   │  ├─ home/                홈 화면 전용 섹션 (Hero, StatsBar, ServiceCards, WhyMongkids, ClassGallery, Reviews, ProcessSteps, SpecialPrograms, FaqPreview, ContactCta)
   │  ├─ auth/                로그인·회원가입 모달 (LoginModal, SCR-013)
   │  ├─ news/                수업 현장 (목록·카드·상세·본문 출력·작성 폼·HTML 편집기·사진 묶음 노드, SCR-006)
   │  ├─ consultation/        상담 신청 모달 (InstitutionConsultationModal SCR-009, PersonalConsultationModal SCR-010, 공용 ConsultationFormParts)
   │  └─ common/              화면과 무관한 재사용 UI (Button: yellow·blue·ghost, to면 링크 / SectionHeading, Highlight, ImagePlaceholder)
   ├─ assets/                 번들에 포함되는 이미지 (logo.png: 원본 로고에서 여백을 잘라낸 버전)
   ├─ api/                    API 호출 (client.js 공통 fetch, auth.js 소셜 로그인, posts.js 게시물, consultations.js 상담 신청)
   └─ data/                   화면 콘텐츠 (homeContent.js, authContent.js, newsContent.js, consultationContent.js)
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
- 로그인 상태는 `App.jsx`가 `fetchMyInfo()`로 조회해 `Header`에 넘긴다. 로그인 실패 시 백엔드가 `/?loginError=true`로 돌려보내면 모달을 열고 오류 문구를 보여 준다.
- 데이터 구조는 백엔드 응답 형태를 염두에 두고 `id`를 가진 객체 배열로 유지한다.
- API 연동 시:
  - 호출 코드는 `src/api/`에 두고, 컴포넌트는 응답 데이터만 받는다. `request()`는 실패 시 `ApiError`(status, code)를 던진다.
  - 개발 서버는 `vite.config.js`의 `server.proxy`로 `/api`, `/oauth2`, `/login/oauth2`를 백엔드(8080)에 연결한다. 인증 쿠키가 같은 출처로 오가야 하므로 `changeOrigin`은 끈다.
  - 응답 형식은 백엔드 규칙([../../back/docs/api-guidelines.md](../../back/docs/api-guidelines.md))의 `ApiResponse<T>`를 따른다.
- 어떤 데이터를 API로 바꿀지는 [요구사항](../../docs/requirements.md)의 기능 ID 단위로 결정한다.

## 4. 반응형 기준

| 폭 | 기준 |
|---|---|
| ~1100px | 헤더가 햄버거 메뉴로 전환 |
| ~1024px | 다단 그리드 축소 (서비스 카드 1열, WHY 3열 등) |
| ~768px | 모바일 레이아웃 (세로 배치, 플로팅 메뉴 축소) |
