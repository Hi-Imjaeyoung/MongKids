# 배포 후 점검 체크리스트

정적 웹 호스팅(S3 + CloudFront, NFR-004)으로 배포한 뒤 확인할 항목이다. 프론트(S3)와 백엔드(API)가 한 도메인 뒤에서 함께 동작해야 하는 부분을 다룬다.
로컬에서는 Vite 프록시(`front/vite.config.js`)가 같은 출처와 Host 전달을 맡았고, 운영에서는 이 일을 CloudFront와 백엔드 설정이 대신한다.

`{도메인}`은 서비스 도메인(예: `mongkids.co.kr`)으로 바꿔 읽는다.

## A. CloudFront 경로 구성 (같은 출처 유지)

프론트 `src/api/client.js`는 상대 경로와 `credentials: 'same-origin'`으로 요청한다. 인증 쿠키도 같은 도메인에서 주고받아야 한다.

- [ ] 기본 동작(`*`) → S3 오리진 (프론트 `dist/`)
- [ ] `/api/*`, `/oauth2/*`, `/login/oauth2/*` → 백엔드 오리진
- [ ] 백엔드 동작의 허용 메서드: GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE (로그아웃이 `POST`)
- [ ] 백엔드 동작의 캐시 정책: `CachingDisabled`
- [ ] 백엔드 동작의 오리진 요청 정책: 쿠키·쿼리 문자열·헤더(Host 포함) 전체 전달 (`AllViewer` 계열)
  - 로그인 중 OAuth2 인가 요청(state)은 세션 쿠키(`JSESSIONID`)에 보관된다. 쿠키가 전달되지 않으면 `authorization_request_not_found`로 로그인이 실패한다.
  - 콜백의 `code`, `state` 쿼리 문자열도 전달되어야 한다.
- [ ] 사용자 지정 오류 응답(403/404 → `index.html`)은 넣지 않는다
  - 이 설정은 배포 전체에 적용된다. 넣으면 API가 보낸 403/404(`M002`, `C002`) JSON이 HTML로 바뀐다.
  - 라우팅을 도입했으므로(`/news`, `/news/{id}` 등) SPA 폴백은 CloudFront Function 등으로 S3 기본 동작에만 적용한다 (확장자 없는 경로 → `/index.html`).
- [ ] `/news/1`을 주소창에 직접 입력·새로고침해도 화면이 뜬다

## B. 백엔드 redirect_uri / 프로토콜

`redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"`의 `{baseUrl}`은 요청의 Host와 프로토콜로 만들어진다.

- [ ] `{baseUrl}`이 `https://{도메인}`으로 만들어지는지 확인
  - 확인 방법: `https://{도메인}/oauth2/authorization/kakao` 응답(302) Location의 `redirect_uri` 값
- [ ] prod에 `server.forward-headers-strategy: framework` 설정 (`X-Forwarded-*` 반영)
- [ ] CloudFront → 백엔드 구간이 HTTP라서 `redirect_uri`가 `http://`로 나오는 경우 둘 중 하나로 해결
  - ALB(HTTPS 리스너)를 거치게 해서 `X-Forwarded-Proto: https` 전달
  - `application-prod.yml`에서 redirect-uri를 `https://{도메인}/login/oauth2/code/{registrationId}`로 고정
- [ ] 로그인 성공/실패 후 `https://{도메인}/`로 돌아오는지 확인
  - `login-success-url: /`, `login-failure-url: /?loginError=true`는 상대 경로라서 콜백이 들어온 도메인 기준으로 이동한다. 콜백이 백엔드 도메인으로 바로 가면 백엔드 `/`에 핸들러가 없어 `C002`가 난다(로컬에서 겪은 문제와 같은 원인).

## C. 인증 쿠키

- [ ] `access_token` 쿠키가 `Secure; HttpOnly; SameSite=Lax; Path=/`로 발급된다 (`app.auth.cookie-secure: true`, prod profile)
- [ ] 로그인 후 `JSESSIONID`가 남아 있지 않다 (성공 시 세션 무효화)

## D. 개발자 콘솔

- [ ] 카카오: 플랫폼 > Web 사이트 도메인에 `https://{도메인}` 등록
- [ ] 카카오: Redirect URI `https://{도메인}/login/oauth2/code/kakao` 등록
- [ ] 카카오: 동의항목 `profile_nickname` 설정 확인
- [ ] 네이버: 서비스 URL `https://{도메인}`, Callback URL `https://{도메인}/login/oauth2/code/naver` 등록
- [ ] 네이버: 앱이 "개발 중"이면 등록된 테스트 계정만 로그인된다. 공개 전에 검수를 요청한다.
- [ ] 로컬 개발용 URI(`http://localhost:5173/login/oauth2/code/{kakao|naver}`)는 그대로 둔다

## E. 운영 환경 변수

저장소에 커밋하지 않는다. 배포 환경(GitHub Actions secrets, 서버 환경변수 등)에만 둔다.

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
- [ ] `JWT_SECRET` (로컬과 다른 값)
- [ ] `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- [ ] `AWS_S3_BUCKET`, `AWS_REGION`, `STORAGE_PUBLIC_BASE_URL` (자격 증명은 키 대신 서버 IAM 역할 권장)

## F. 정적 파일 캐시

- [ ] `index.html`은 `Cache-Control: no-cache`
- [ ] 해시가 붙은 `assets/*`는 장기 캐시 (`max-age=31536000, immutable`)
- [ ] 배포 후 `/index.html` 무효화(invalidation)

## G. 기능 점검 시나리오 (FR-018)

카카오·네이버 각각 확인한다.

- [ ] 로그인 모달 → 소셜 로그인 → `https://{도메인}/`로 복귀 → 헤더가 로그인 상태
- [ ] 새로고침 후에도 로그인 유지
- [ ] 로그아웃 → 헤더가 비로그인 상태, `GET /api/v1/members/me`가 401 JSON
- [ ] 동의 화면에서 취소 → `/?loginError=true`로 복귀
- [ ] 첫 로그인 시 회원 생성, 같은 계정으로 다시 로그인하면 같은 회원 (DB 확인)

## H. 게시물·연구소 글 사진 저장소 (FR-008)

사진은 브라우저가 Presigned URL로 S3에 직접 올리고(`PUT`), 백엔드는 URL 발급과 `HeadObject`로 확인만 한다.

- [ ] 사진용 S3 버킷 생성, 퍼블릭 액세스 차단 유지 (읽기는 CloudFront로만)
- [ ] 버킷 CORS: `PUT`을 `https://{도메인}`과 `http://localhost:5173`에서 허용, 허용 헤더 `Content-Type`
- [ ] 백엔드 IAM 권한: 버킷의 `posts/*`에 `s3:PutObject`(URL 서명용), `s3:GetObject`(`HeadObject`)
- [ ] CloudFront에 사진 버킷 오리진(OAC) 추가, `STORAGE_PUBLIC_BASE_URL`을 그 주소로 설정
- [ ] 수명 주기 규칙 검토: 업로드만 하고 저장하지 않았거나 수정·삭제로 빠진 사진이 남는다
- [ ] 작성 화면에서 사진 여러 장 업로드 → 목록 대표 사진·상세 갤러리 표시 → 수정으로 사진 교체 → 삭제
- [ ] 몽키즈 연구소(`/lab/write`)도 같은 흐름 확인. 업로드 URL과 `posts/*` 키를 수업 현장과 함께 쓴다 (`lab_post_image`)
- [ ] 21장, 10MB 초과, 이미지가 아닌 파일은 거부된다
