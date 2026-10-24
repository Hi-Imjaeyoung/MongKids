# 아키텍처

> 결정 사항(변경 가능): 초기 권장안. 변경 시 `CLAUDE.md`와 함께 갱신한다.

## 1. 패키지 구조 (도메인형)

기능(도메인) 단위로 묶고, 각 도메인 안에서 레이어를 나눈다. 여러 도메인이 공유하는 코드는 `global`에 둔다.

```
com.mongkids
├─ DemoApplication.java
├─ domain
│  ├─ member
│  │  ├─ controller   MemberController
│  │  ├─ service      MemberService
│  │  ├─ repository   MemberRepository
│  │  ├─ dto          MemberCreateRequest, MemberResponse ...
│  │  ├─ entity       Member, MemberRole(enum) ...
│  │  └─ exception    (선택) 도메인 전용 ErrorCode
│  └─ {other-domain}
│     └─ ...
└─ global
   ├─ config          JpaConfig, WebConfig ...
   ├─ exception       BusinessException, ErrorCode, GlobalExceptionHandler
   ├─ common          ApiResponse, BaseTimeEntity, 페이징 DTO
   ├─ security        JWT 발급·검증, 토큰 쿠키, 인증 필터, 401·403 응답
   ├─ storage         파일 저장소 (FileStorage, S3 구현)
   └─ util            (정말 범용인 것만)
```

## 2. 레이어별 책임

| 레이어 | 책임 | 하지 말 것 |
|---|---|---|
| Controller | HTTP 요청/응답 매핑, `@Valid` 입력 검증, Service 호출, `ApiResponse` 래핑 | 비즈니스 로직, Repository 직접 호출, Entity 반환 |
| Service | 비즈니스 로직, 트랜잭션 경계, Entity ↔ DTO 변환 조율 | HTTP 객체(`HttpServletRequest` 등) 의존 |
| Repository | 영속성 접근 (Spring Data JPA / JdbcClient) | 비즈니스 판단 |
| Entity | 도메인 상태와 그 상태를 바꾸는 행위(메서드) | 외부 서비스 호출, DTO 의존 |
| DTO | 계층 간/외부와의 데이터 전달 (`record`) | 로직 보유 (단순 변환 정적 메서드 `from()`은 허용) |

## 3. 의존 규칙

```
Controller ──▶ Service ──▶ Repository ──▶ Entity
     │             │
     └──▶ DTO ◀────┘
```

- 의존은 위 방향으로만 흐른다. Repository가 Service를, Service가 Controller를 알면 안 된다.
- Controller → Repository 건너뛰기 금지.
- **도메인 간 참조**: 다른 도메인의 데이터가 필요하면 그 도메인의 **Service**를 주입받는다. 다른 도메인의 Repository 직접 주입 금지.
  - Entity 연관관계(`@ManyToOne` 등)로 다른 도메인 Entity를 참조하는 것은 허용.
- 순환 의존이 생기면 공통 로직을 별도 Service로 분리하거나 이벤트(`ApplicationEventPublisher`)로 끊는다.
- `global`은 어떤 `domain`에도 의존하지 않는다.

## 4. 요청 처리 흐름

```
HTTP Request
  → Controller (@Valid XxxRequest)
  → Service (@Transactional, Request → Entity, 비즈니스 로직)
  → Repository (save / find)
  → Service (Entity → XxxResponse)
  → Controller (ApiResponse.success(response))
HTTP Response

예외 발생 시: throw BusinessException(ErrorCode) → GlobalExceptionHandler → ApiResponse.error(...)
```

## 5. global 공통 구성

| 클래스 | 위치 | 역할 |
|---|---|---|
| `ApiResponse<T>` | `global/common` | 공통 응답 래퍼 ([api-guidelines.md](api-guidelines.md)) |
| `ErrorCode` | `global/exception` | HTTP 상태 + 에러 코드 + 메시지 enum |
| `BusinessException` | `global/exception` | `ErrorCode`를 담는 런타임 예외 |
| `GlobalExceptionHandler` | `global/exception` | `@RestControllerAdvice` 예외 → 응답 변환 |
| `BaseTimeEntity` | `global/common` | `createdAt`, `updatedAt` Auditing ([database.md](database.md)) |
| `JpaConfig` | `global/config` | `@EnableJpaAuditing` |
| `SecurityConfig` | `global/config` | 시큐리티 필터 체인 (아래 7절) |
| `JwtProvider`, `TokenCookieManager`, `JwtAuthenticationFilter`, `SecurityResponseWriter`, `AuthProperties` | `global/security` | JWT·쿠키 인증 (아래 7절) |
| `PageResponse<T>` | `global/common` | 페이지 응답 ([api-guidelines.md](api-guidelines.md)) |
| `FileStorage`, `S3FileStorage`, `StorageProperties`, `StorageConfig` | `global/storage`, `global/config` | 파일 저장소 (아래 8절) |

## 6. 설정 파일

- `application.yml`(공통) + `application-{local,dev,prod}.yml`(환경별)로 관리하는 것을 권장한다. (현재는 `application.properties` 하나뿐)
- 비밀값은 `${ENV_VAR}` 플레이스홀더로 주입한다.
- Actuator는 운영에서 `health`, `info`만 노출한다.

## 7. 인증 (FR-018)

카카오·네이버 소셜 로그인은 Spring Security OAuth2 Client로 처리하고, 로그인 상태는 HttpOnly 쿠키의 JWT로 유지한다.

```
[로그인 모달] → GET /oauth2/authorization/{kakao|naver}
  → 카카오·네이버 인가 페이지 → GET /login/oauth2/code/{kakao|naver}
  → CustomOAuth2UserService: 사용자 정보 → MemberService.loginOrRegister (최초 로그인 시 가입)
  → OAuth2LoginSuccessHandler: JWT 발급 → access_token 쿠키 → app.auth.login-success-url 로 리다이렉트
  (실패: OAuth2LoginFailureHandler → app.auth.login-failure-url)

이후 요청: JwtAuthenticationFilter가 쿠키의 JWT를 검증 → principal = 회원 ID(Long)
  → Controller에서 @AuthenticationPrincipal Long memberId
```

| 항목 | 내용 |
|---|---|
| 위치 | 도메인 무관 부분은 `global/security`, 회원 연동은 `domain/auth` (`CustomOAuth2UserService`, 성공·실패 핸들러). `SecurityConfig`는 이들을 Spring 인터페이스 타입 빈으로 주입받아 `global`이 `domain`에 의존하지 않는다 |
| 토큰 | HS256, `sub`=회원 ID, `role` 클레임. 유효 기간 `app.auth.token-expiration`(기본 1일). 리프레시 토큰 없음 |
| 쿠키 | `access_token`, HttpOnly, SameSite=Lax, Path=/, Secure는 `app.auth.cookie-secure`(prod true) |
| CSRF | Spring CSRF는 끄고 SameSite=Lax 쿠키로 교차 사이트 POST를 막는다 |
| 세션 | `STATELESS`. OAuth2 인가 요청(state, PKCE)만 로그인 과정 동안 세션에 보관하고 성공 시 무효화한다 |
| 로그아웃 | `POST /api/v1/auth/logout` → 쿠키 삭제. 이미 발급된 토큰은 만료 전까지 서버에서 무효화하지 않는다 |
| 탈퇴 회원 | 소셜 로그인 실패 처리. 토큰이 남아 있어도 `/members/me`는 `M002`(403) |
| 인가 규칙 | `SecurityConfig.authorizeHttpRequests`에 경로별로 추가한다 (현재 `/api/v1/members/me`, 게시물·연구소 글 `POST·PUT·DELETE`가 인증 필요). 역할 검사는 토큰의 `role` 대신 Service에서 DB의 현재 역할로 한다 (게시물·연구소 글 FR-009: `ArticleWriteSupport`, `MemberRole.canWritePost()`). 권한 회수가 다시 로그인하지 않아도 바로 반영된다 |

설정·환경변수: `spring.security.oauth2.client.*`(application.yml), `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `JWT_SECRET` (`.env.example` 참고).
각 개발자 콘솔에는 Redirect URI `{서비스 주소}/login/oauth2/code/kakao`, `.../naver`를 등록한다. 로컬은 Vite 프록시를 거치므로 `http://localhost:5173/login/oauth2/code/{kakao|naver}`.

## 8. 파일 저장소 (FR-008)

사진은 S3에 저장하고 DB에는 객체 키만 둔다 ([schema.md](schema.md)). 업로드는 서버를 거치지 않는다.

```
[작성 화면] → POST /api/v1/posts/image-upload-urls 또는 /api/v1/lab-posts/image-upload-urls (contentType, size)
  → ArticleWriteSupport: 형식·크기·개수 검증, 묶음별 키 생성(posts/ 또는 lab/{yyyy}/{MM}/{uuid}.{ext}) → FileStorage.createUploadUrl
  → 브라우저가 uploadUrl로 S3에 직접 PUT
  → 편집기가 본문 HTML에 <img data-image-key> 삽입 → POST /api/v1/posts (content)
  → PostContentSanitizer.sanitize: jsoup 허용 목록 정화 + 구조 검사 + 사진 키 추출
  → PostService·LabPostService(ArticleWriteSupport): 묶음 경로 확인, FileStorage.findSize로 업로드·크기 확인 후 저장 (post·lab_post 본문, post_image·lab_post_image)
응답: PostContentSanitizer.render → img에 src = FileStorage.getUrl(key) (app.storage.public-base-url + "/" + key), 링크에 target·rel
```

| 항목 | 내용 |
|---|---|
| 인터페이스 | `global/storage/FileStorage`. 도메인 규칙(키 prefix, 허용 형식, 개수)은 각 도메인 Service에 둔다 |
| 키 경로 | 사진 테이블마다 하나: `posts/` → `post_image`, `lab/` → `lab_post_image`, `reviews/` → `review_image`. 연구소 글은 2026-10-24에 `posts/`에서 분리했다 (ADR-002). 분리 전 연구소 사진은 `posts/`에 남아 있을 수 있다 |
| 구현 | `S3FileStorage` (AWS SDK v2 `S3Client`, `S3Presigner`). Presigned URL에 `Content-Type`·`Content-Length`를 서명해 다른 타입·크기의 업로드는 S3가 거부한다 |
| 자격 증명 | SDK 기본 체인. 로컬은 `AWS_ACCESS_KEY_ID`·`AWS_SECRET_ACCESS_KEY`, 운영은 IAM 역할 |
| 설정 | `app.storage.bucket`(`AWS_S3_BUCKET`), `region`(`AWS_REGION`, 기본 ap-northeast-2), `public-base-url`(`STORAGE_PUBLIC_BASE_URL`), `upload-url-duration`(10분) |
| 본문 정화 | `domain/post/service/PostContentSanitizer` (jsoup). 허용 목록·구조 규칙은 [api-guidelines.md](api-guidelines.md#7-게시물-api-fr-008). 프론트는 출력 전에 DOMPurify로 한 번 더 거른다 |
| 테스트 | S3에 접속하지 않는다. 통합 테스트는 `@MockitoBean FileStorage` |
| 정리 | 업로드 후 저장하지 않았거나 수정·삭제로 빠진 객체는 지우지 않는다 ([ADR-002](../../docs/adr/ADR-002-s3-orphan-photo-cleanup.md) 현상 유지). 정리 방식과 같은 키 공유 방지는 후속 작업 |
