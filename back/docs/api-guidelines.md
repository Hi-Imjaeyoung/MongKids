# API 가이드라인

## 1. URL 규칙

- 접두사: `/api/v1`
- 리소스는 **복수 명사**, kebab-case: `/api/v1/members`, `/api/v1/class-schedules`
- 동사 금지 (`/getMember` ✗). 행위는 HTTP 메서드로 표현.
- 계층 관계: `/api/v1/members/{memberId}/orders`
- 리소스 상태 변경처럼 CRUD로 표현하기 어려운 경우만 하위 동작 허용: `POST /api/v1/orders/{orderId}/cancel`
- PathVariable 이름은 `{리소스}Id` (`{memberId}`).
- 쿼리 파라미터는 camelCase (`?sortBy=createdAt`).

## 2. HTTP 메서드 & 상태 코드

| 메서드 | 용도 | 성공 상태 |
|---|---|---|
| GET | 조회 | 200 OK |
| POST | 생성 | 201 Created |
| PUT | 전체 수정 | 200 OK |
| PATCH | 부분 수정 | 200 OK |
| DELETE | 삭제 | 200 OK (또는 204 No Content) |

| 에러 상태 | 상황 |
|---|---|
| 400 | 입력 검증 실패, 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 중복·상태 충돌 |
| 500 | 서버 내부 오류 |

## 3. 응답 형식 (`ApiResponse<T>`)

성공:
```json
{
  "success": true,
  "data": { "id": 1, "name": "홍길동" },
  "error": null
}
```

실패:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "M001",
    "message": "회원을 찾을 수 없습니다.",
    "fieldErrors": []
  }
}
```

입력 검증 실패(400) 시 `fieldErrors`:
```json
"fieldErrors": [
  { "field": "name", "reason": "공백일 수 없습니다" }
]
```

구현 스케치:
```java
public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {
	public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(true, data, null); }
	public static ApiResponse<Void> error(ErrorResponse error) { return new ApiResponse<>(false, null, error); }
}
```

## 4. 예외 처리 (`GlobalExceptionHandler`)

| 예외 | 응답 |
|---|---|
| `BusinessException` | `ErrorCode`의 status / code / message |
| `MethodArgumentNotValidException` | 400, `C001`, `fieldErrors` 포함 |
| `HttpMessageNotReadableException`, 타입 불일치 | 400, `C001` |
| 그 외 `Exception` | 500, `C999` (상세 내용은 로그에만, 응답에는 노출 금지) |

## 5. 페이징

- 요청: `?page=0&size=20&sort=createdAt,desc` (Spring `Pageable`, page는 0부터)
- `size` 최대값 제한 (예: 100).
- 응답 `data`:
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 135,
  "totalPages": 7,
  "hasNext": true
}
```
- Spring `Page` 객체를 그대로 직렬화하지 말고 `PageResponse<T>` record로 변환한다.

## 6. 인증·내 정보 API (FR-018, FR-019, FR-027, FR-028)

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| GET | `/oauth2/authorization/{kakao\|naver}` | 소셜 로그인 시작 (브라우저 이동, API 아님) | - |
| GET | `/api/v1/members/me` | 내 정보 `MemberResponse` (`id`, `provider`, `name`, `email`, `phone`, `snsAgreed`, `snsAgreedAt`, `role`, `createdAt`). 헤더 로그인 상태·권한 판단에도 쓴다 | 필요 |
| PUT | `/api/v1/members/me/sns-agreement` | SNS 수신 동의 변경 `{agreed}` → `MemberResponse`. 동의하면 `snsAgreedAt` 저장, 철회하면 `null` | 필요 |
| GET | `/oauth2/authorization/{kakao\|naver}-withdraw` | 회원 탈퇴 시작 (브라우저 이동). 강제 재인증으로 소셜 로그인을 다시 받는다 | 로그인 쿠키 |
| POST | `/api/v1/auth/logout` | 로그아웃 (토큰 쿠키 삭제) | - |

- 인증은 `access_token` HttpOnly 쿠키로 한다. 흐름은 [architecture.md](architecture.md#7-인증-fr-018) 참고.
- 미인증 401 `A001`, 권한 없음 403 `A002`.
- 회원 탈퇴 결과는 프론트로 리다이렉트해 알린다. 성공 `app.auth.withdraw-success-url`(홈 `/?withdrawn=true`, 쿠키 삭제. 프론트가 완료 모달을 띄운다). 실패 `app.auth.withdraw-failure-url`에 `withdrawError` 코드가 붙는다:
  - `cancelled`: 재로그인 취소
  - `failed`: 그 밖의 소셜 로그인 실패
  - `A001`: 로그인 쿠키 없음·만료
  - `M004`: 관리자·직원
  - `M005`: 가입한 계정과 다른 소셜 계정
  - `M006`: 연결 끊기 실패. 개인정보는 지우지 않는다

## 7. 게시물 API (FR-008)

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/v1/posts/image-upload-urls` | 사진 업로드 URL 발급. 요청 `{files: [{contentType, size}]}` → `[{imageKey, uploadUrl}]`. 키는 `posts/` 아래 | 직원 |
| POST | `/api/v1/posts` | 작성 `{category, title, content}` → 201 `{id}`. `content`는 본문 HTML | 직원 |
| GET | `/api/v1/posts?category=&page=&size=` | 최신순 목록 (`PageResponse<PostSummaryResponse>`). `category` 없으면 전체, 기본 `size` 12, 최대 100 | - |
| GET | `/api/v1/posts/{postId}` | 상세 (`content`는 사진 `src`가 채워진 HTML, `authorId` 포함) | - |
| PUT | `/api/v1/posts/{postId}` | 전체 수정. 사진 목록은 새 본문에 나온 사진으로 통째로 바뀐다 | 작성자·관리자 |
| DELETE | `/api/v1/posts/{postId}` | soft delete | 작성자·관리자 |

- `category`: `GENERAL`(게시글), `NEWS`(소식), `EVENT`(현장 사진).
- 본문 HTML 허용 목록: `p br strong em u a ul ol li blockquote hr figure figcaption img`. 그 밖의 태그·속성(`script`, `style`, `on*` 등)은 제거한다.
  - `a[href]`는 http/https만. 응답에 `target="_blank" rel="noopener noreferrer"`를 붙인다.
  - 사진 묶음: `<figure class="post-photos">` 안에 `<img data-image-key="…">` 1~3장 + `<figcaption>`(선택, 200자). `img`는 사진 묶음 안에만 둘 수 있다.
  - 본문 최대 100,000자, 글자나 사진이 하나 이상. 위반은 `P004`.
- 사진 흐름: URL 발급 → 브라우저가 `uploadUrl`에 `PUT`(발급 때 보낸 `Content-Type`·크기 그대로) → 본문 HTML에 `data-image-key`로 넣어 작성·수정. 본문에 처음 나온 사진이 대표 사진. 수정 시 상세 응답 HTML을 그대로 편집해 보내면 된다(`src`는 서버가 지운다).
- 사진 제한: 게시물당 최대 20장(`P002`), jpeg·png·webp, 장당 10MB. 형식·크기 위반, 업로드되지 않은 키, 묶음 경로 밖의 키(게시물 `posts/`, 연구소 글 `lab/`), 중복 키는 `P003`.
- 권한 (FR-009): **직원** = `STAFF`·`ADMIN`. **작성자·관리자** = 직원인 작성자 본인 또는 `ADMIN`. 직원 권한이 회수된 작성자는 자기 글도 수정·삭제할 수 없다.
  - 역할은 토큰이 아니라 DB의 현재 역할로 `ArticleWriteSupport`가 검사한다 (연구소 글도 같음). 비로그인은 401 `A001`, 권한 없음은 403 `A002`.
- 없거나 삭제된 게시물 `P001`(404).

### 7-1. 몽키즈 연구소 글 API (FR-008, SCR-024)

수업 현장 게시물과 테이블(`lab_post`)·경로를 분리했다. 본문 형식·사진 흐름·사진 제한·권한(FR-009)은 §7과 같다.

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/v1/lab-posts/image-upload-urls` | 사진 업로드 URL 발급. 요청·응답은 §7과 같고 키는 `lab/` 아래 | 직원 |
| POST | `/api/v1/lab-posts` | 작성 `{category, title, content}` → 201 `{id}` | 직원 |
| GET | `/api/v1/lab-posts?category=&keyword=&page=&size=` | 최신순 목록 (`PageResponse<LabPostSummaryResponse>`). `category` 없으면 전체, `keyword`는 제목 부분 일치(앞뒤 공백 무시, 본문은 검색 안 함, FR-024), 기본 `size` 12, 최대 100 | - |
| GET | `/api/v1/lab-posts/{labPostId}` | 상세 (`content`는 사진 `src`가 채워진 HTML, `authorId` 포함) | - |
| PUT | `/api/v1/lab-posts/{labPostId}` | 전체 수정. 사진 목록은 새 본문에 나온 사진으로 통째로 바뀐다 | 작성자·관리자 |
| DELETE | `/api/v1/lab-posts/{labPostId}` | soft delete | 작성자·관리자 |

- `category`: `PARENT_QA`(부모님 궁금증), `GROWTH`(움직임과 성장), `EQUIPMENT`(교구 이야기). 수업 현장 분류는 받지 않는다(400).
- 응답에 `likeCount`는 있고 `commentCount`는 없다 (연구소 글은 댓글 없음).
- 사진은 `/api/v1/lab-posts/image-upload-urls`로 받은 `lab/` 키를 쓴다 (2026-10-24 분리, ADR-002). 경로를 나누기 전에 `posts/`로 올린 사진은 그 글을 수정할 때 그대로 둘 수 있지만, 새로 넣을 수는 없다.
- 없거나 삭제된 연구소 글 `P005`(404).

### 7-2. 게시물 댓글 API (FR-010)

수업 현장 게시물에만 있다 (연구소 글은 댓글 없음).

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/v1/posts/{postId}/comments` | 작성 `{content}` → 201 `PostCommentResponse` | 회원 |
| GET | `/api/v1/posts/{postId}/comments?page=&size=` | 오래된 순 목록 (`PageResponse<PostCommentResponse>`). 기본 `size` 20, 최대 100 | - |
| DELETE | `/api/v1/posts/{postId}/comments/{commentId}` | soft delete | 작성자·관리자 |

- `PostCommentResponse`: `{id, content, authorId, authorName, createdAt}`. 탈퇴한 작성자는 `authorName`이 "탈퇴 회원".
- `content`: 일반 텍스트, 공백만은 안 됨, 1,000자 이내(400 `C001`).
- **회원** = 로그인한 회원 누구나 (직원 권한 불필요, 별도 확인 절차 없음). **작성자·관리자** = 댓글 작성자 본인 또는 `ADMIN` (직원이어도 남의 댓글은 403 `A002`).
- 작성·삭제 시 `post.comment_count`를 DB에서 바로 더하고 뺀다 (동시 작성 시 값 유실 방지).
- 없거나 삭제된 게시물 `P001`, 없거나 삭제된 댓글 `P006`(404).

### 7-3. 공감 API (FR-011)

수업 현장 게시물과 연구소 글 모두 받는다. 응답은 모두 `LikeResponse` `{liked, likeCount}`.

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/v1/posts/{postId}/likes` | 공감 상태. 비로그인은 `liked: false` | - |
| POST | `/api/v1/posts/{postId}/likes` | 공감 (이미 공감했으면 변화 없음) | 회원 |
| DELETE | `/api/v1/posts/{postId}/likes` | 공감 취소 (공감하지 않았으면 변화 없음) | 회원 |
| GET·POST·DELETE | `/api/v1/lab-posts/{labPostId}/likes` | 연구소 글. 위와 같음 | 위와 같음 |

- **회원** = 로그인한 회원 누구나 (직원 권한 불필요, 별도 확인 절차 없음). 회원당 글마다 1회 (`post_like`·`lab_post_like` UNIQUE).
- 공감·취소는 글 행을 `PESSIMISTIC_WRITE`로 잠그고 처리해 같은 회원이 동시에 눌러도 한 번만 반영되고 `like_count`가 어긋나지 않는다.
- 없거나 삭제된 게시물 `P001`, 연구소 글 `P005`(404).

## 8. 상담 신청 API (FR-001, FR-002, FR-003, FR-023)

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/v1/institution-consultations` | 기관 상담 신청 → 201 `{id}` | - |
| POST | `/api/v1/personal-consultations` | 개인 상담 신청 → 201 `{id}` | - |

### 기관 상담 신청

- 요청: `{institutionName, preferredDays, preferredTime, childAgeGroups, childCount, phone, note, privacyAgreed}`
  - `preferredDays`: `MONDAY`~`SATURDAY` 1개 이상 (`SUNDAY`는 `C001`). `preferredTime`: 자유 입력 100자.
  - `childAgeGroups`: `UNDER_2`, `AGE_3`, `AGE_4`, `AGE_5`, `OVER_6` 1개 이상. `childCount`: 1~999.
  - `phone`: 휴대전화·유선번호(`^0\d{1,2}-?\d{3,4}-?\d{4}$`). 숫자만 남겨 저장한다.
  - `note`: 선택, 1000자. `privacyAgreed`: 반드시 `true` (FR-023). 동의 시각을 `privacy_agreed_at`에 저장한다.
- 저장 형식: 요일은 월~토 순서로 `·`로 잇고 시간대를 붙여 `preferred_schedule`(`"월·수 오전 10시"`), 연령은 라벨을 `, `로 이어 `child_age_range`(`"만 3세, 만 4세"`).
- 검증 실패는 400 `C001` + `fieldErrors`.

### 개인 상담 신청

- 요청: `{guardianName, phone, childName, childGender, childBirthDate, childAgeMonths, address, attendsDaycare, temperament, favoritePlay, favoriteCharacter, healthNote, motivation, privacyAgreed, healthAgreed}`
  - 필수: `guardianName`·`childName` 50자, `childGender`(`MALE`, `FEMALE`), `address` 255자, `attendsDaycare`(boolean), `privacyAgreed` 반드시 `true`.
  - `phone`: 번호 인증이 없어 휴대전화(`^010-?\d{4}-?\d{4}$`)만 받는다. 숫자만 남겨 저장한다.
  - `childBirthDate`(`YYYY-MM-DD`, 오늘 이전)와 `childAgeMonths`(0~120) 중 **정확히 하나**. 둘 다 있거나 둘 다 없으면 `C001`(`fieldErrors` 없음).
  - 선택: `temperament`, `favoritePlay`, `favoriteCharacter`, `healthNote`, `motivation` 각 1000자. 공백뿐이면 null로 저장한다.
  - `healthAgreed`: `healthNote`가 있으면 반드시 `true`, 아니면 `C001`(`fieldErrors` 없음). 생략하면 미동의.
- 동의 시각: `privacy_agreed_at`은 항상, `health_agreed_at`은 `healthNote`가 있을 때만 저장한다 (FR-023).
- Bean Validation 실패는 400 `C001` + `fieldErrors`.

## 9. 후기 API (FR-012~017, FR-025)

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/v1/reviews/image-upload-urls` | 사진 업로드 URL 발급. 요청 `{files: [{contentType, size}]}` → `[{imageKey, uploadUrl}]` | 회원 |
| POST | `/api/v1/reviews` | 작성 `{type, satisfaction, content, imageKeys}` → 201 `ReviewResponse` | 회원 |
| GET | `/api/v1/reviews?type=&page=&size=` | 최신순 목록 (`PageResponse<ReviewResponse>`). `type` 없으면 전체, 기본 `size` 10, 최대 100. 비로그인은 아래 제한 | - |
| PUT | `/api/v1/reviews/{reviewId}` | 전체 수정 → `ReviewResponse`. 사진 목록은 요청의 `imageKeys`로 통째로 바뀐다 | 작성자 |
| DELETE | `/api/v1/reviews/{reviewId}` | soft delete | 작성자 |
| PUT | `/api/v1/reviews/{reviewId}/reply` | 관리자 답글 저장 `{content}` → `ReviewResponse`. 이미 있으면 고친다(`repliedAt` 갱신) | 관리자 |

- `type`: `FREE_TRIAL`(무료체험 후기), `CLASS`(수업 후기). `satisfaction`: 1~5 정수(FR-016). `content`: 일반 텍스트, 공백만은 안 됨, 1,000자 이내. 위반은 400 `C001` + `fieldErrors`.
- `ReviewResponse`: `{id, type, satisfaction, content, authorId, authorName, images: [{imageKey, url}], reply: {content, repliedAt} | null, createdAt, updatedAt}`.
  - `authorName`은 첫 글자만 남기고 가린다(FR-014): 홍길동 → `홍**`, 김철 → `김*`, 한 글자 → `*`. 탈퇴한 작성자는 "탈퇴 회원".
  - `reply`는 관리자 답글(FR-017). 후기당 하나, 없으면 `null`.
- 사진(FR-015): 흐름은 §7과 같다(URL 발급 → `PUT` → 저장 요청에 `imageKeys`). 후기당 최대 5장(`R002`), jpeg·png·webp, 장당 10MB. 형식·크기 위반, 업로드되지 않은 키, `reviews/` 밖의 키, 중복 키는 `R003`. 보낸 순서대로 저장하고 첫 사진이 대표 사진. 수정할 때 남길 사진은 응답의 `imageKey`를 다시 보낸다.
- 비로그인 목록(FR-012): `page`·`size`와 상관없이 최신 4개만 주고 `hasNext=false`. `totalElements`는 실제 건수라 화면은 이 값이 4보다 크면 로그인 안내를 띄운다.
- 숨김(FR-021)·삭제한 후기는 목록에 나오지 않는다.
- **회원** = 로그인한 회원 누구나 (별도 확인 절차 없음, 저장 즉시 공개). **작성자** = 후기 작성자 본인만. 관리자도 남의 후기는 403 `A002`(FR-025, 관리자는 숨김으로 처리). **관리자** = DB의 현재 역할이 `ADMIN` (직원 `STAFF`도 403 `A002`).
- 답글 `content`: 일반 텍스트, 공백만은 안 됨, 1,000자 이내(400 `C001`).
- 없거나 삭제된 후기 `R001`(404).

## 10. 관리자 API (SCR-014: FR-004, FR-005, FR-021, FR-022, FR-026)

관리자 화면 전용 API는 `/api/v1/admin/**`에 모은다. 모두 인증이 필요하고(비로그인 401 `A001`), 각 Service가 `MemberService.validateAdmin`으로 DB의 현재 역할이 `ADMIN`인지 확인한다(직원 `STAFF`도 403 `A002`). 후기 답글(FR-017)은 SCR-012 카드에서 입력하므로 §9의 `/api/v1/reviews/{reviewId}/reply`를 쓴다.

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/admin/consultations?type=&status=&page=&size=` | 상담 신청 최근 접수 순 목록 (`PageResponse<ConsultationSummaryResponse>`). 기본 `size` 20, 최대 100 |
| GET | `/api/v1/admin/consultations/{consultationId}` | 상세 (`ConsultationDetailResponse`) |
| PUT | `/api/v1/admin/consultations/{consultationId}/status` | 처리 상태 변경 `{status}` → `ConsultationDetailResponse` |
| GET | `/api/v1/admin/consultations/new-count/stream` | 신규(`RECEIVED`) 상담 건수 실시간 구독 (SSE, `text/event-stream`). `ApiResponse` 래퍼를 쓰지 않는다 |
| GET | `/api/v1/admin/reviews?type=&hiddenOnly=&page=&size=` | 숨긴 후기를 포함한 최신순 목록 (`PageResponse<AdminReviewResponse>`). `hiddenOnly=true`면 숨긴 것만 |
| PUT | `/api/v1/admin/reviews/{reviewId}/hidden` | 숨김 `{hidden: true}`·다시 공개 `{hidden: false}` → `AdminReviewResponse` |
| GET | `/api/v1/admin/members?role=&keyword=&page=&size=` | 탈퇴하지 않은 회원 최근 가입 순 목록 (`PageResponse<AdminMemberResponse>`) |
| PUT | `/api/v1/admin/members/{memberId}/role` | 직원 지정 `{role: "STAFF"}`·해제 `{role: "MEMBER"}` → `AdminMemberResponse` |

- 신규 건수 구독(FR-005, [ADR-004](../../docs/adr/ADR-004-admin-web-notification-transport.md)):
  - 권한은 연결할 때 한 번 확인한다. 권한 오류는 다른 API처럼 JSON(401 `A001`, 403 `A002`)으로 내려간다.
  - 이벤트 `new-count`의 `data`는 건수 숫자다. 연결하자마자 현재 건수를 보내고(`retry: 3000`), 신청 저장·처리 상태 변경이 커밋되면 다시 센 건수를 보낸다. 신청자 정보는 보내지 않는다.
  - 커밋이 몰리면 1초에 한 번 이하로 묶어 보낸다. 15초마다 주석(`: ping`)을 보내 연결을 유지한다(CloudFront 오리진 응답 제한 시간 30초).
  - 연결은 30분 뒤 서버가 닫는다. 브라우저 `EventSource`가 다시 연결하면서 권한을 다시 확인한다. 연결은 서버 메모리에만 있어 여러 대로 늘리면 인스턴스 간 전달이 필요하다.
- `ConsultationSummaryResponse`: `{id, type, title, phone, status, createdAt}`. `title`은 기관명 또는 보호자 이름. `type`: `INSTITUTION`·`PERSONAL`, `status`: `RECEIVED`(신규)·`CONTACTED`(연락 완료)·`COMPLETED`(상담 완료).
- `ConsultationDetailResponse`: `{id, type, phone, status, privacyAgreedAt, completedAt, createdAt, institution, personal}`. 유형에 맞는 쪽만 채우고 나머지는 `null`. `institution`은 기관 신청 항목(§8), `personal`은 개인 신청 항목과 `healthAgreedAt`.
- 상태는 어느 단계로든 바꿀 수 있다. `COMPLETED`가 되면 `completedAt`을 남기고(이미 완료면 처음 시각 유지), 다른 상태로 되돌리면 `null`. 이 시각이 보관 기간(FR-023)의 기준이다. 없는 신청은 404 `S001`.
- `AdminReviewResponse`: `{review: ReviewResponse, hidden, hiddenAt}`. 작성자 이름은 공개 응답처럼 가린다(FR-014). 숨긴 후기는 §9 공개 목록에서 빠진다. 없거나 삭제된 후기 404 `R001`.
- `AdminMemberResponse`: `{id, name, phone, provider, role, createdAt}`. `keyword`는 이름 부분 일치, 숫자가 있으면 전화번호(하이픈 무시) 부분 일치로도 찾는다.
- 권한 변경: 대상이 `ADMIN`이거나 `role`이 `ADMIN`이면 400 `M003`. 없는 회원 404 `M001`, 탈퇴 회원 403 `M002`. 회수해도 작성한 게시물은 남고 새 글 작성·자기 글 수정·삭제만 막힌다(FR-009).

## 11. 근태 수업 시작·종료 API (EMP-SCR-001, EMP-SCR-006, EMP-FR-001·002·003·004·010·011·012·013·014)

근태 관리(직원용) 수업 체크. 판정 규칙은 [ADR-006](../../docs/adr/ADR-006-attendance-location-verification.md)을 따른다. 직원 API는 DB의 현재 역할이 `STAFF`·`ADMIN`인 회원만(그 외 403 `A002`), 자기에게 할당된 수업만 쓴다. 남의 수업은 없는 수업처럼 404 `E001`.

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/v1/attendance/classes?from=&to=` | 내 수업 할당, 기간(최대 31일) 안 시간순 → `ClassSessionResponse[]` (EMP-FR-004) | 직원 |
| GET | `/api/v1/attendance/classes/today` | 오늘 수업 + 수업 시작이 반려된 채 남은 지난 수업, 시간순 → `ClassSessionResponse[]` (EMP-FR-013) | 직원 |
| POST | `/api/v1/attendance/classes/{id}/start` | 위치로 수업 시작 확인 `{latitude, longitude, accuracy}` → `{confirmed, distanceMeters, radiusMeters, classSession}` (EMP-FR-001) | 직원 |
| POST | `/api/v1/attendance/classes/{id}/start/manual` | 수동 등록 `{reason, latitude?, longitude?, accuracy?}` → 승인 대기 `ClassSessionResponse` (EMP-FR-011) | 직원 |
| PATCH | `/api/v1/attendance/classes/{id}/start/note` | 수업 시작 특이사항 `{note}` (1,000자 이내, 비우면 지움) (EMP-FR-002) | 직원 |
| POST | `/api/v1/attendance/image-upload-urls` | 수업 사진 업로드 URL 발급 `{files: [{contentType, size}]}` → `[{imageKey, uploadUrl}]` | 직원 |
| POST | `/api/v1/attendance/classes/{id}/end` | 수업 종료 `{note, imageKeys}` → 201 `ClassSessionResponse` (EMP-FR-010) | 직원 |
| PUT | `/api/v1/attendance/classes/{id}/end` | 수업 기록 정정 `{note, imageKeys}` → `ClassSessionResponse`. 사진·특이사항을 통째로 바꾼다 (EMP-FR-014) | 작성자 |

관리자 API (`/api/v1/admin/attendance`, `/api/v1/admin/institutions`, §10과 같은 권한 규칙). 화면은 EMP-SCR-006.

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/admin/attendance/pending-starts` | 승인 대기 수업 시작, 먼저 등록한 순 → `AdminClassSessionResponse[]` |
| POST | `/api/v1/admin/attendance/classes/{id}/start/approve` | 승인 → `AdminClassSessionResponse` |
| POST | `/api/v1/admin/attendance/classes/{id}/start/reject` | 반려 `{reason}` (200자 이내) |
| POST | `/api/v1/admin/attendance/classes/{id}/start` | 관리자 직접 등록 `{reason}`. 기록이 없거나 반려된 수업만 |
| GET | `/api/v1/admin/attendance/classes?from=&to=&staffId=&institutionId=` | 기간(최대 31일)·직원·기관별 수업과 시작·종료 기록. 조건이 없으면 전체. 수업 할당 목록도 이것으로 본다 (EMP-FR-003, 004, 007) |
| POST | `/api/v1/admin/attendance/classes` | 수업 반복 등록 `{institutionId, staffId, firstDate, lastDate, daysOfWeek: ["MONDAY", …], startTime, endTime, target?}` → 201 만든 `AdminClassSessionResponse[]` (EMP-FR-004) |
| PUT | `/api/v1/admin/attendance/classes/{id}` | 수업 한 회 수정 `{institutionId, staffId, classDate, startTime, endTime, target?}` → `AdminClassSessionResponse` |
| DELETE | `/api/v1/admin/attendance/classes/{id}` | 수업 한 회 삭제 |
| GET | `/api/v1/admin/institutions` | 기관 목록, 이름순 → `InstitutionResponse[]` (EMP-FR-012) |
| POST | `/api/v1/admin/institutions` | 기관 등록 `{name, address, latitude, longitude}` → 201 `InstitutionResponse` |
| PUT | `/api/v1/admin/institutions/{id}` | 기관 수정 (같은 본문) → `InstitutionResponse` |
| DELETE | `/api/v1/admin/institutions/{id}` | 기관 삭제. 할당된 수업이 있으면 409 `E012` |

- `ClassSessionResponse`: `{id, classDate, startTime, endTime, target, institutionName, institutionAddress, status, start, end}`.
  - `status`: `NOT_STARTED`(시작 전)·`STARTED`(시작 확정: 자동 확정·승인·관리자 등록)·`PENDING`(승인 대기)·`REJECTED`(반려)·`ENDED`(종료 완료). 저장하지 않고 기록에서 계산한다. 수업 종료 기록이 있어도 수업 시작이 반려되면 `REJECTED`.
  - `start`: `{method, status, startedAt, distanceMeters, accuracy, reason, rejectReason, note, processedAt}` | `null`. `method`: `LOCATION`·`MANUAL`·`ADMIN`. 좌표 원본은 응답에 넣지 않는다.
  - `end`: `{endedAt, correctedAt, note, images: [{imageKey, url}], correctable}` | `null`. `correctable`은 요청한 직원이 작성자이고 수업 당일(서버 시각 기준)인지.
- `AdminClassSessionResponse`: `{staffId, staffName, institutionId, classSession}`.
- `InstitutionResponse`: `{id, name, address, latitude, longitude, classSessionCount}`. 위도 -90~90·경도 -180~180(그 외 400 `C001`). 없는 기관은 404 `E011`. 좌표를 바꾸면 이후 수업 시작부터 새 좌표로 판정하고, 이미 저장된 수업 시작 기록의 거리는 그대로 둔다.
- 수업 반복 등록: `firstDate`~`lastDate`(최대 6개월) 안에서 `daysOfWeek`에 든 날짜마다 수업을 하나씩 만든다. 기간·시간이 잘못되면(끝 ≤ 시작) 400 `C001`, 만들 날짜가 없으면 400 `E016`. 담당은 `STAFF`·`ADMIN`만(그 외 400 `E015`). 같은 직원의 같은 날 수업과 시간이 겹치면(끝 시각 = 다음 시작 시각은 허용) 409 `E014`이고 하나도 만들지 않는다. 수정도 같은 규칙.
- 수업 수정·삭제: 수업 시작·종료 기록이나 수업 시작 시도 로그가 있으면 409 `E013`.
- 수업 시작 판정: 서버가 하버사인 거리(m, 반올림)로 기관 좌표 반경 100m 안이면 `confirmed=true`로 확정, 밖이면 저장하지 않고 `confirmed=false`(200)와 거리·기준 반경을 준다. 정확도로는 거절하지 않는다. 결과와 상관없이 시도마다 `class_start_attempt`에 남긴다. 시작 시각은 서버 시각이고 시간 제한은 없다.
- 수업 시작 기록은 수업당 1건. 기록이 없거나 반려된 수업만 다시 확인할 수 있고(반려 기록을 덮어씀), 확정·승인 대기면 409 `E002`. 승인·반려는 승인 대기만(그 외 409 `E004`), 기록이 없으면 404 `E003`.
- 수업 종료: 수업 시작이 확정·승인 대기인 수업만(그 외 409 `E005`), 수업당 1건(409 `E006`). 위치는 받지 않는다. 승인 대기 중 종료한 뒤 반려돼도 종료 기록은 지우지 않는다.
- 사진: 흐름은 §7과 같다. 1장 이상 20장 이하(400 `E009`), jpeg·png·webp, 장당 10MB. 형식·크기 위반, 업로드되지 않은 키, `attendance/` 밖의 키, 중복 키는 400 `E010`. 정정할 때 남길 사진은 응답의 `imageKey`를 다시 보낸다.
- 정정: 작성자 본인만(남의 수업은 404 `E001`), 수업 날짜 당일까지(지나면 409 `E008`). 수업 시작·종료 시각은 바뀌지 않고 `correctedAt`만 남긴다. 이력은 보관하지 않는다. 종료 기록이 없으면 404 `E007`.
- 수업 시작 특이사항은 확정·승인 대기 기록에만(없거나 반려면 404 `E003`).

## 12. 기타

- 날짜/시간: ISO-8601 (`2026-10-07T14:30:00`), 서버 타임존 `Asia/Seoul`.
- JSON 필드는 camelCase.
- `null` 필드도 형식 일관성을 위해 포함한다 (위 예시처럼).
- API 버전은 호환되지 않는 변경 시에만 올린다 (`/api/v2`).
