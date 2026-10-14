# 프론트엔드 아키텍처

## 1. 폴더 구조

```
front/
├─ index.html                 진입 HTML (lang, 메타, 웹폰트)
├─ public/                    정적 파일 (favicon 등, 경로 그대로 서빙)
└─ src/
   ├─ main.jsx                React 마운트
   ├─ App.jsx                 레이아웃(Header, FloatingMenu) + 페이지
   ├─ index.css               전역 리셋, 디자인 토큰, 공통 클래스(.container, .section-heading)
   ├─ pages/                  화면 단위. 섹션 컴포넌트를 조립만 한다
   │  └─ HomePage.jsx
   ├─ components/
   │  ├─ layout/              모든 화면에 공통으로 붙는 영역 (Header, FloatingMenu)
   │  ├─ home/                홈 화면 전용 섹션 (Hero, StatsBar, ServiceCards, WhyMongkids, Reviews)
   │  ├─ auth/                로그인·회원가입 모달 (LoginModal, SCR-013)
   │  └─ common/              화면과 무관한 재사용 UI (Button, Highlight, ImagePlaceholder)
   ├─ api/                    API 호출 (client.js 공통 fetch, auth.js 소셜 로그인)
   └─ data/                   화면 콘텐츠 (homeContent.js, authContent.js)
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
- `pages`는 섹션 순서만 정한다. 마크업·스타일을 직접 갖지 않는다.

## 3. 데이터와 API 연동 방침

- 홈 콘텐츠는 `src/data/homeContent.js`의 정적 데이터다. 로그인 상태(FR-018)만 API로 받는다.
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
