# 검색 노출 (NFR-006)

몽키즈 홈페이지가 브랜드·지역 검색어로 검색되도록 프론트에 둔 설정과, 검색 서비스에 직접 등록하는 절차를 정리한다.
대상 검색어와 요구사항은 [requirement_homepage.md](../../docs/requirements/requirement_homepage.md) NFR-006을 따른다.

## 1. 프론트에 둔 것

| 위치 | 내용 |
|---|---|
| `index.html` | 기본 `<title>`·description·canonical, OpenGraph, 네이버 사이트 소유 확인, 사이트·업체 정보 JSON-LD(`WebSite`, `Organization`, 서비스 지역) |
| `src/data/seoContent.js` | 기본 제목·설명(`siteMeta`), 화면별 탭 제목·설명(`pageMeta`), 프로그램 상세 설명 |
| `src/pages/usePageMeta.js` | 화면이 열릴 때 탭 제목·description·canonical을 바꾸는 훅. 화면마다 맨 위에서 호출 |
| `public/sitemap.xml` | 공개 화면 목록. 게시글 상세는 넣지 않는다 |
| `public/robots.txt` | 공개 화면 허용, 로그인·직원 도구·API 차단, `Sitemap:` 주소 |

- 이 사이트는 SPA라 자바스크립트를 실행하지 않는 수집기는 `index.html`의 기본값만 읽는다. 기본 제목·설명에 대상 검색어를 넣어 두고, `siteMeta`와 같은 값으로 유지한다.
- 탭 제목 형식: `{화면 이름} | 몽키즈유아체육`. 홈은 기본 제목, 게시글 상세는 글 제목.
- `meta keywords` 태그나 별도 키워드 목록은 쓰지 않는다 (검색 순위에 반영되지 않음). 검색어는 제목·설명 문장과 JSON-LD에만 넣는다.
- 화면을 추가·삭제하면 `pageMeta`, `sitemap.xml`, (비공개 화면이면) `robots.txt`를 함께 고친다.
- 연락처·주소가 확정되면 JSON-LD에 `telephone`·`address`를 추가할 수 있다.

## 2. 검색 서비스 등록 (사용자가 직접)

배포 후 `https://mongkiz.com/sitemap.xml`, `https://mongkiz.com/robots.txt`가 열리는지 먼저 확인한다.

### 네이버 서치어드바이저 (https://searchadvisor.naver.com)

1. 웹마스터 도구 → 사이트 관리에서 `https://mongkiz.com` 선택 (소유 확인은 `index.html` 메타 태그로 완료됨)
2. 요청 → 사이트맵 제출: `https://mongkiz.com/sitemap.xml`
3. 요청 → 웹 페이지 수집: 홈과 주요 화면 주소 요청
4. 검증 → robots.txt에서 수집 허용 여부 확인

### Google Search Console (https://search.google.com/search-console)

1. 속성 추가 → 도메인(`mongkiz.com`, Route 53에 TXT 레코드 추가) 또는 URL 접두어(`https://mongkiz.com/`, HTML 태그 방식이면 받은 메타 태그를 `index.html`에 추가 요청)
2. Sitemaps → `sitemap.xml` 제출
3. URL 검사 → 홈 주소 색인 생성 요청

- 검색 결과 반영에는 며칠~몇 주가 걸린다.
- `robots.txt`는 배포 때 CloudFront 무효화 대상(`/index.html`)이 아니라 바뀐 내용이 캐시 만료 후 보일 수 있다. 바로 확인하려면 `/robots.txt`도 무효화한다.
