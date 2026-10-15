# 프론트엔드 코딩 컨벤션

## 1. 컴포넌트

- 함수 컴포넌트만 사용하고 파일 끝에서 `export default`.
- 파일명·컴포넌트명은 PascalCase, 한 파일에 컴포넌트 하나.
- props 기본값은 구조 분해에서 지정한다 (`function Button({ variant = 'yellow' })`).
- 목록 렌더링의 `key`는 데이터의 `id`(없으면 고유한 값)를 쓴다. 인덱스 사용 지양.
- 클릭 동작만 있는 요소는 `<button type="button">`, 페이지 이동은 `<a>`.
- 장식용 아이콘·이모지에는 `aria-hidden="true"`, 의미 있는 이미지에는 `alt`/`aria-label`.

## 2. 스타일

- 컴포넌트와 같은 폴더에 같은 이름의 CSS 파일을 두고 컴포넌트에서 import한다.
- 클래스명은 BEM: `block`, `block__element`, `block--modifier`.
  - 예: `service-card`, `service-card__title`, `service-card--green`
- 색상·둥글기·그림자·레이아웃 값은 `src/index.css`의 CSS 변수를 쓴다.

  | 분류 | 변수 |
  |---|---|
  | 브랜드 | `--yellow`, `--blue`, `--green` (+ `-soft`), `--cream` |
  | 분류 칩 글자 | `--yellow-text`, `--blue-text`, `--green-text` (`-soft` 배경 위) |
  | 상태 | `--danger` (오류 문구) |
  | 텍스트 | `--text`, `--text-sub`, `--text-muted` |
  | 배경·선 | `--bg`, `--bg-soft`, `--border` |
  | 형태 | `--radius-md`, `--radius-lg`, `--shadow-sm`, `--shadow-md` |
  | 레이아웃 | `--header-height`, `--container` |

- 새 색이 반복해서 필요하면 토큰을 추가한 뒤 사용한다.
- 미디어 쿼리 기준은 [architecture.md](architecture.md#4-반응형-기준)를 따른다.
- 한글 문단에는 `word-break: keep-all`을 적용한다.

## 3. 데이터

- 화면 문구·수치·목록은 `src/data/`에 둔다. 컴포넌트에 문자열을 하드코딩하지 않는다.
  - 예외: 접근성 레이블, "자세히 보기"처럼 UI 자체에 속한 짧은 문구
- 강조 단어는 데이터에 `highlight` 필드로 두고 `common/Highlight`로 렌더링한다.

## 4. 임시 리소스

- 실제 이미지가 없으면 `common/ImagePlaceholder`에 무엇이 들어갈지 `label`로 적는다.
- 아이콘은 실제 리소스가 준비될 때까지 이모지로 대체한다. 교체 시 `src/assets/`에 두고 import한다.

## 5. 포맷

- 들여쓰기 공백 2칸, 세미콜론 없음, 작은따옴표 (Vite 템플릿 기본 스타일).
- 커밋 전 `npm run lint` 통과.
