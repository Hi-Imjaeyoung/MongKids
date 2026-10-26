# CLAUDE.md

이 파일은 Mongkids 프론트엔드에서 작업할 때 Claude Code(및 개발자)가 따라야 할 핵심 규칙을 담는다.
저장소 공통 규칙은 루트 [CLAUDE.md](../CLAUDE.md), 프론트엔드 상세 규칙은 `front/docs/` 아래 문서를 참고한다.

## 프로젝트 개요

- Mongkids 웹 프론트엔드 (Vite + React SPA)
- 현재 상태: 홈 화면(SCR-001)을 기획 v4 구성으로 정적 데이터 구현 (공통 푸터·모바일 하단 상담 바 포함). 소셜 로그인 모달(SCR-013, FR-018), 수업 현장(SCR-006, FR-008) 목록·상세·작성·수정·댓글(FR-010)·공감(FR-011) API 연동, 몽키즈 연구소(SCR-024) 목록·검색·상세·작성·수정·공감 API 연동(상세·작성은 수업 현장 화면 재사용), 기관 상담 신청 모달(SCR-009, FR-001), 개인 상담 신청 2단계 모달(SCR-010, FR-002), ABOUT 몽키즈(SCR-002)·체육/행사 프로그램 전체 안내·상세(SCR-003, SCR-016~020)·자주 묻는 질문(SCR-022) 정적 화면, 수업 후기(SCR-012) `/reviews` 목록(무한 스크롤)·작성·수정·삭제·관리자 답글(FR-017) API 연동, 관리자 화면(SCR-014) `/admin` 상담 신청·후기 숨김·직원 권한 API 연동, 신규 상담 건수 배지(FR-005, `EventSource` SSE 구독). react-router-dom 라우팅.
- 요구사항 (백엔드와 공유) → [../docs/requirements.md](../docs/requirements.md)
- 진행 사항 (백엔드와 공유) → [../docs/progress.md](../docs/progress.md)
- 디자인 시안 → [../docs/design/](../docs/design/)

## 기술 스택

| 구분 | 내용 |
|---|---|
| Language | JavaScript (JSX). TypeScript 미사용 |
| Framework | React 19, react-router-dom 7 |
| Editor | TipTap 3 (게시물 본문 HTML 편집기, 작성 화면만 지연 로딩), DOMPurify (본문 출력 전 정화) |
| Build | Vite 8 (`@vitejs/plugin-react`) |
| Lint | ESLint 10 (react-hooks, react-refresh) |
| Style | 일반 CSS (컴포넌트별 파일) + `src/index.css` 디자인 토큰 |
| Font | Pretendard (CDN, `index.html`) |

## 자주 쓰는 명령 (`front/`에서)

```bash
npm install
npm run dev        # 개발 서버
npm run lint       # ESLint
npm run build      # 프로덕션 빌드 (dist/)
npm run preview    # 빌드 결과 미리보기
```

## 아키텍처 요약 → [docs/architecture.md](docs/architecture.md)

- `pages/`(화면 조립·API 호출) → `components/{layout,home,about,program,faq,auth,news,lab,review,consultation,admin,common}/`(UI) ← `data/`(콘텐츠). 라우트는 `App.jsx`
- API 호출은 `src/api/`(`client.js`의 `request`가 `ApiResponse`를 풀어 `data`만 반환). 인증은 HttpOnly 쿠키라 토큰을 JS에서 다루지 않는다.
- 화면 문구·수치·목록은 컴포넌트에 하드코딩하지 않고 `src/data/`에 둔다. API 연동 시 이 데이터만 응답값으로 교체한다.

## 핵심 코딩 규칙 → [docs/coding-conventions.md](docs/coding-conventions.md)

- 함수 컴포넌트 + `export default`. 파일명은 PascalCase (`ServiceCards.jsx`).
- 컴포넌트 CSS는 같은 폴더에 같은 이름으로 두고 컴포넌트에서 import (`ServiceCards.css`).
- 클래스명은 BEM 형식 (`service-card__title`, `service-card--blue`).
- 색상·둥글기·그림자는 `src/index.css`의 CSS 변수를 사용한다. 값 직접 입력 지양.
- 버튼은 `common/Button`, 이미지 자리는 `common/ImagePlaceholder`를 재사용한다.

## Git → [../docs/git-workflow.md](../docs/git-workflow.md)

- 프론트엔드 커밋은 scope `front` 사용 (`feat(front): ...`, `fix(front): ...`)

## Claude 작업 원칙 (프론트엔드)

공통 원칙은 루트 [CLAUDE.md](../CLAUDE.md)를 따른다.

- 코드를 바꾼 뒤에는 `front/`에서 `npm run lint && npm run build`로 검증하고 결과를 그대로 보고한다.
- 화면 변경은 가능하면 실제로 띄워 데스크톱·모바일 폭에서 확인한다.
- 기존 코드의 스타일(들여쓰기: 공백 2칸, 세미콜론 없음, 작은따옴표)을 따른다.

## 알려진 이슈 / TODO

- [ ] 실제 사진·아이콘 리소스 없음 → `ImagePlaceholder`, 이모지로 대체 중 (로고는 `src/assets/logo.png` 적용)
- [x] 라우팅 도입. 화면이 없는 메뉴는 아직 `#` 앵커
- [x] 운영 배포 시 SPA 딥링크 폴백: CloudFront Function `mongkiz-spa-router`로 적용 (2026-10-22) → [../docs/deploy-checklist.md](../docs/deploy-checklist.md) A
- [x] `vite.config.js`에 `/api`, `/oauth2`, `/login/oauth2` 프록시 설정 (`changeOrigin: false` 유지: OAuth redirect_uri가 프론트 주소로 만들어져야 함)
- [ ] 로그인·게시물·상담 신청 외 API 미연동
