# 배포 후 점검 체크리스트

정적 웹 호스팅(S3 + CloudFront, NFR-004)으로 배포한 뒤 확인할 항목이다. 프론트(S3)와 백엔드(API)가 한 도메인 뒤에서 함께 동작해야 하는 부분을 다룬다.
로컬에서는 Vite 프록시(`front/vite.config.js`)가 같은 출처와 Host 전달을 맡았고, 운영에서는 이 일을 CloudFront와 백엔드 설정이 대신한다.

`{도메인}`은 서비스 도메인 `mongkiz.com`으로 바꿔 읽는다.

## 운영 리소스

2026-10-22 프론트 정적 배포 기준. 비밀값(액세스 키 등)은 여기에 적지 않는다.

| 구분 | 값 |
|---|---|
| 도메인 | `mongkiz.com` (외부 등록업체, 네임서버는 Route 53 호스팅 영역) |
| 인증서 | ACM `us-east-1`, `mongkiz.com` + `www.mongkiz.com`, DNS 검증 |
| 프론트 버킷 | S3 `mongkiz-front-prod` (`ap-northeast-2`, 퍼블릭 차단, OAC로 CloudFront만 읽기) |
| CloudFront | 배포 `E1AWEJNTIYBX5U` (`d14t42trjr34p1.cloudfront.net`), 기본 루트 객체 `index.html` |
| CloudFront Function | `mongkiz-spa-router` (기본 동작 Viewer Request): `www.mongkiz.com` → `https://mongkiz.com` 301, 확장자 없는 경로 → `/index.html` |
| DNS | `mongkiz.com`, `www.mongkiz.com` A/AAAA 별칭 → CloudFront |

2026-10-22 백엔드 배포 기준 (EC2 1대 + Caddy, RDS). 리전은 모두 `ap-northeast-2`.

| 구분 | 값 |
|---|---|
| 서버 | EC2 `i-01a62a936805035e1` (`mongkiz-back`, t4g.small, AL2023 arm64, gp3 20GB), Elastic IP `13.125.26.176` |
| 오리진 도메인 | `origin.mongkiz.com` A → EIP. Caddy가 Let's Encrypt 인증서로 443을 받고 `127.0.0.1:8080`(Spring Boot)으로 넘긴다 ([back/deploy/Caddyfile](../back/deploy/Caddyfile)) |
| 오리진 보호 | CloudFront가 붙이는 `X-Origin-Verify` 헤더가 없으면 Caddy가 403. 값은 Parameter Store `ORIGIN_VERIFY` |
| 접속 | SSH 없음(22 닫힘). SSM Session Manager로만 접속 |
| 보안 그룹 | `mongkiz-back-sg`(80/443 전체), `mongkiz-db-sg`(3306은 `mongkiz-back-sg`에서만) |
| DB | RDS `mongkiz-prod` (MariaDB 11.8.9, db.t4g.micro, 단일 AZ, gp3 20GB, 비공개, 암호화, 삭제 방지, 백업 7일), 파라미터 그룹 `mongkiz-mariadb118`(utf8mb4). TLS 필수(`require_secure_transport=ON`)라서 `DB_URL`에 `sslMode=verify-full` + RDS CA 번들(`/opt/mongkiz/rds-global-bundle.pem`) |
| 사진 버킷 | S3 `mongkiz-photos-prod` (퍼블릭 차단, CORS `PUT`) |
| 사진 CloudFront | 배포 `E21JTPP1GUA4UD` (`d3vc95fih5p9fq.cloudfront.net`, OAC). 기존 배포에 경로를 붙이지 않는다: 키 접두사 `reviews/`가 프론트 경로 `/reviews`와 겹친다 |
| 배포 jar | S3 `mongkiz-back-artifacts/releases/{sha}.jar` (30일 뒤 만료) |
| 환경 변수 | Parameter Store `/mongkiz/prod/*`. 서비스가 시작할 때마다 `load-env.sh`가 `/run/mongkiz/back.env`로 옮긴다 |
| IAM | 인스턴스 역할 `mongkiz-back-ec2`, GitHub OIDC 역할 `mongkiz-github-back-deploy` |

> 처음에는 계정이 AWS 무료 플랜이라 RDS 백업 보관을 1일로 만들었다. 2026-10-22 유료 플랜으로 바꾼 뒤 7일로 늘렸다.

### 운영 DB 조회

RDS는 비공개로 둔다. 로컬 DB 프로그램(DBeaver, HeidiSQL 등)으로 조회할 때는 SSM 포트 포워딩 터널을 쓴다. EC2를 거쳐 내 PC의 `127.0.0.1:13306`을 RDS 3306에 잇는 방식이라, 보안 그룹이나 퍼블릭 설정을 바꾸지 않고 IP가 바뀌어도 그대로 쓸 수 있다.

1. 준비(1회): AWS CLI v2와 [Session Manager 플러그인](https://docs.aws.amazon.com/systems-manager/latest/userguide/install-plugin-windows.html)을 설치한다. IAM 사용자에게 `ssm:StartSession` 권한이 있어야 한다.
2. 터널 열기: 터미널을 새로 열고 아래 명령을 실행한 뒤 창을 둔다. `Waiting for connections...`가 나오면 준비된 것이다.
   ```bash
   aws ssm start-session --target i-01a62a936805035e1 \
     --document-name AWS-StartPortForwardingSessionToRemoteHost \
     --parameters "host=mongkiz-prod.cta02ii4kjp5.ap-northeast-2.rds.amazonaws.com,portNumber=3306,localPortNumber=13306"
   ```
3. DB 프로그램 연결 설정

   | 항목 | 값 |
   |---|---|
   | 호스트 / 포트 | `127.0.0.1` / `13306` |
   | 사용자 / DB | `jaeyoung` / `mongkids` |
   | 비밀번호 | Parameter Store `/mongkiz/db-users/jaeyoung` (SecureString) |
   | SSL | 켜기. 인증서(호스트명) 검증은 끈다. 접속 주소가 `127.0.0.1`이라 인증서의 호스트명과 다르다. 연결은 그대로 암호화된다 |

4. 다 쓰면 터널 창에서 `Ctrl+C`로 닫는다.

- `jaeyoung`은 `mongkids` 전체 권한 계정이다. 테이블·컬럼을 바꾸면 다음 배포에서 `validate`가 실패하므로 스키마 변경은 배포 절차에 맞춰 적용한다. 상담 신청 등 개인정보(FR-023)가 든 조회 결과는 파일로 내보내거나 공유하지 않는다.
- 설치 없이 쿼리 몇 개만 볼 때는 EC2 Session Manager 세션에서 `mariadb` 클라이언트를 쓴다 ([back/docs/database.md](../back/docs/database.md)).
- 터널을 쓸 수 없을 때만 RDS를 잠깐 퍼블릭으로 연다. `mongkiz-db-sg`에 내 IP `/32` 규칙을 추가하고 `--publicly-accessible`을 켠 뒤, 작업이 끝나면 둘 다 바로 되돌린다.

### 재배포

프론트 배포는 `main`에 `front/**` 변경이 반영되면 GitHub Actions(`.github/workflows/front.yml`)가 아래와 같은 순서로 실행한다. 직접 배포해야 할 때(`front/`에서 `npm run build` 후):

```bash
aws s3 sync dist/ s3://mongkiz-front-prod --delete --exclude index.html --cache-control "public,max-age=31536000,immutable"
aws s3 cp dist/index.html s3://mongkiz-front-prod/index.html --cache-control "no-cache"
aws cloudfront create-invalidation --distribution-id E1AWEJNTIYBX5U --paths "/index.html"
```

백엔드 배포는 `main`에 `back/**` 변경이 반영되면 GitHub Actions(`.github/workflows/back.yml`)가 jar를 S3에 올리고, SSM으로 서버의 `/opt/mongkiz/deploy.sh {sha}`를 실행한다. `deploy.sh`는 jar 교체 → 재시작 → `/actuator/health` 확인 순서로 진행하고, 실패하면 직전 jar로 되돌린다. 직접 배포해야 할 때(`back/`에서 `./gradlew build` 후):

```bash
aws s3 cp build/libs/mongkids-0.0.1-SNAPSHOT.jar s3://mongkiz-back-artifacts/releases/{sha}.jar
aws ssm send-command --instance-ids i-01a62a936805035e1 --document-name AWS-RunShellScript \
  --parameters 'commands=["/opt/mongkiz/deploy.sh {sha}"]'
# 롤백: 이전 sha로 같은 명령 (서버 /opt/mongkiz/releases/에 최근 5개 보관)
# 로그: SSM 세션에서 journalctl -u mongkiz-back -f
```

- 운영 DB는 `ddl-auto: validate`다. Entity에 컬럼·테이블을 추가하면 배포 **전에** RDS에 `ALTER`/`CREATE`를 먼저 적용해야 기동한다 ([back/docs/database.md](../back/docs/database.md#ddl-auto-정책)).
- 서버 설정 원본은 [back/deploy/](../back/deploy/)에 있다. 바꾸면 서버의 `/etc/caddy/Caddyfile`, `/etc/systemd/system/*.service`, `/opt/mongkiz/*.sh`에 직접 반영해야 한다 (자동 배포는 jar만 바꾼다).

## A. CloudFront 경로 구성 (같은 출처 유지)

프론트 `src/api/client.js`는 상대 경로와 `credentials: 'same-origin'`으로 요청한다. 인증 쿠키도 같은 도메인에서 주고받아야 한다.

- [x] 기본 동작(`*`) → S3 오리진 (프론트 `dist/`)
- [x] `/api/*`, `/oauth2/*`, `/login/oauth2/*` → 백엔드 오리진 `mongkiz-back-origin`(`origin.mongkiz.com`, HTTPS only, 커스텀 헤더 `X-Origin-Verify`) (2026-10-22)
- [x] 백엔드 동작의 허용 메서드: GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE (로그아웃이 `POST`)
- [x] 백엔드 동작의 캐시 정책: `CachingDisabled`
- [x] 백엔드 동작의 오리진 요청 정책: 쿠키·쿼리 문자열·헤더 전체 전달. Host는 빼고 넘긴다(`AllViewerExceptHostHeader`). 서비스 도메인은 Caddy가 `X-Forwarded-Host`로 넘긴다 (B 참고)
  - 로그인 중 OAuth2 인가 요청(state)은 세션 쿠키(`JSESSIONID`)에 보관된다. 쿠키가 전달되지 않으면 `authorization_request_not_found`로 로그인이 실패한다.
  - 콜백의 `code`, `state` 쿼리 문자열도 전달되어야 한다.
- [x] 사용자 지정 오류 응답(403/404 → `index.html`)은 넣지 않는다
  - 이 설정은 배포 전체에 적용된다. 넣으면 API가 보낸 403/404(`M002`, `C002`) JSON이 HTML로 바뀐다.
  - 라우팅을 도입했으므로(`/news`, `/news/{id}` 등) SPA 폴백은 CloudFront Function 등으로 S3 기본 동작에만 적용한다 (확장자 없는 경로 → `/index.html`). → `mongkiz-spa-router`로 적용
  - 백엔드 경로 동작을 추가할 때 이 Function을 연결하지 않는다 (`/api/...`도 확장자가 없어 `index.html`로 바뀐다).
- [x] `/news/1`을 주소창에 직접 입력·새로고침해도 화면이 뜬다 (2026-10-22, 없는 정적 파일은 403 유지)

## B. 백엔드 redirect_uri / 프로토콜

`redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"`의 `{baseUrl}`은 요청의 Host와 프로토콜로 만들어진다.

- [x] `{baseUrl}`이 `https://{도메인}`으로 만들어지는지 확인 (2026-10-22, 카카오·네이버 모두 `redirect_uri=https://mongkiz.com/login/oauth2/code/{kakao|naver}`, `JSESSIONID`는 `Secure; HttpOnly; SameSite=Lax`)
  - 확인 방법: `https://{도메인}/oauth2/authorization/kakao` 응답(302) Location의 `redirect_uri` 값
- [x] prod에 `server.forward-headers-strategy: framework` 설정 (`X-Forwarded-*` 반영)
- [x] 적용한 방식: CloudFront는 Host를 넘기지 않고(`AllViewerExceptHostHeader`, 오리진 인증서가 `origin.mongkiz.com`이라서), Caddy가 `X-Forwarded-Proto: https`, `X-Forwarded-Host: mongkiz.com`, `X-Forwarded-Port: 443`을 덮어써서 넘긴다. 앱은 `127.0.0.1`에만 열려 있어 이 헤더를 위조할 수 있는 경로가 없다.
  - 안 될 때 대안: `application-prod.yml`에서 redirect-uri를 `https://{도메인}/login/oauth2/code/{registrationId}`로 고정
- [ ] 로그인 성공/실패 후 `https://{도메인}/`로 돌아오는지 확인
  - `login-success-url: /`, `login-failure-url: /?loginError=true`는 상대 경로라서 콜백이 들어온 도메인 기준으로 이동한다. 콜백이 백엔드 도메인으로 바로 가면 백엔드 `/`에 핸들러가 없어 `C002`가 난다(로컬에서 겪은 문제와 같은 원인).

## C. 인증 쿠키

- [ ] `access_token` 쿠키가 `Secure; HttpOnly; SameSite=Lax; Path=/`로 발급된다 (`app.auth.cookie-secure: true`, prod profile)
- [ ] 로그인 후 `JSESSIONID`가 남아 있지 않다 (성공 시 세션 무효화)

## D. 개발자 콘솔

운영은 사업자 계정의 카카오·네이버 앱 키를 쓴다 (2026-10-22 교체, Parameter Store `KAKAO_*`, `NAVER_*`). 로컬 `.env`의 키와 다를 수 있다.

- [x] 카카오: 플랫폼 > Web 사이트 도메인에 `https://{도메인}` 등록 (2026-10-22, 사용자)
- [x] 카카오: Redirect URI `https://{도메인}/login/oauth2/code/kakao` 등록 (2026-10-22, 사용자)
- [ ] 카카오: 동의항목 `profile_nickname` 설정 확인
- [x] 네이버: 서비스 URL `https://{도메인}`, Callback URL `https://{도메인}/login/oauth2/code/naver` 등록 (2026-10-22, 사용자)
- [ ] 네이버: 앱이 "개발 중"이면 등록된 테스트 계정만 로그인된다. 공개 전에 검수를 요청한다.
- [ ] 로컬 개발용 URI(`http://localhost:5173/login/oauth2/code/{kakao|naver}`)는 그대로 둔다

## E. 운영 환경 변수

저장소에 커밋하지 않는다. Parameter Store `/mongkiz/prod/{이름}`에만 둔다 (비밀값은 SecureString).

- [x] `SPRING_PROFILES_ACTIVE=prod`
- [x] `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET` (사용자가 직접 입력)
- [x] `JWT_SECRET` (로컬과 다른 값, 생성 후 바로 저장)
- [x] `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- [x] `AWS_S3_BUCKET`, `AWS_REGION`, `STORAGE_PUBLIC_BASE_URL` (자격 증명은 인스턴스 역할)
- [x] `ORIGIN_VERIFY` (CloudFront 오리진 커스텀 헤더와 같은 값)
- 운영 DB 스키마를 처음 만들 때만 `SPRING_JPA_HIBERNATE_DDL_AUTO=update`를 넣고 기동한 뒤 지웠다 (2026-10-22)

## F. 정적 파일 캐시

- [x] `index.html`은 `Cache-Control: no-cache`
- [x] 해시가 붙은 `assets/*`는 장기 캐시 (`max-age=31536000, immutable`)
- [ ] 배포 후 `/index.html` 무효화(invalidation) (첫 배포는 캐시가 없어 생략, 재배포부터 매번)

## G. 기능 점검 시나리오 (FR-018)

카카오·네이버 각각 확인한다.

> 2026-10-22 카카오 점검 완료(사용자, 운영 DB에 카카오 회원 1명). 네이버는 운영 DB에 회원이 없어 아직 점검 전이다.

- [ ] 로그인 모달 → 소셜 로그인 → `https://{도메인}/`로 복귀 → 헤더가 로그인 상태
- [ ] 새로고침 후에도 로그인 유지
- [ ] 로그아웃 → 헤더가 비로그인 상태, `GET /api/v1/members/me`가 401 JSON
- [ ] 동의 화면에서 취소 → `/?loginError=true`로 복귀
- [ ] 첫 로그인 시 회원 생성, 같은 계정으로 다시 로그인하면 같은 회원 (DB 확인)

## H. 게시물·연구소 글 사진 저장소 (FR-008)

사진은 브라우저가 Presigned URL로 S3에 직접 올리고(`PUT`), 백엔드는 URL 발급과 `HeadObject`로 확인만 한다.

- [x] 사진용 S3 버킷 생성, 퍼블릭 액세스 차단 유지 (읽기는 CloudFront로만) — `mongkiz-photos-prod`
- [x] 버킷 CORS: `PUT`을 `https://{도메인}`과 `http://localhost:5173`에서 허용, 허용 헤더 `Content-Type`
- [x] 백엔드 IAM 권한: 버킷의 `posts/*`, `reviews/*`에 `s3:PutObject`(URL 서명용), `s3:GetObject`(`HeadObject`)
- [ ] 백엔드 IAM 권한에 `lab/*` 추가 (연구소 사진 경로 분리, 2026-10-24). 빠지면 연구소 사진 업로드가 S3에서 403, 저장은 `P003`
- [x] 사진 전용 CloudFront(OAC) `E21JTPP1GUA4UD`, `STORAGE_PUBLIC_BASE_URL=https://d3vc95fih5p9fq.cloudfront.net`
- [ ] 수명 주기 규칙 검토: 업로드만 하고 저장하지 않았거나 수정·삭제로 빠진 사진이 남는다
- [ ] 작성 화면에서 사진 여러 장 업로드 → 목록 대표 사진·상세 갤러리 표시 → 수정으로 사진 교체 → 삭제
- [ ] 몽키즈 연구소(`/lab/write`)도 같은 흐름 확인. 업로드 URL은 `/api/v1/lab-posts/image-upload-urls`, 키는 `lab/*` (`lab_post_image`)
- [ ] 21장, 10MB 초과, 이미지가 아닌 파일은 거부된다
- [ ] 후기(`/reviews`) 사진도 같은 흐름 확인 (`reviews/*` 키, 최대 5장)
