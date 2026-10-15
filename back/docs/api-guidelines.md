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

## 6. 인증 API (FR-018)

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| GET | `/oauth2/authorization/{kakao\|naver}` | 소셜 로그인 시작 (브라우저 이동, API 아님) | - |
| GET | `/api/v1/members/me` | 내 정보 (`id`, `provider`, `name`, `email`, `role`) | 필요 |
| POST | `/api/v1/auth/logout` | 로그아웃 (토큰 쿠키 삭제) | - |

- 인증은 `access_token` HttpOnly 쿠키로 한다. 흐름은 [architecture.md](architecture.md#7-인증-fr-018) 참고.
- 미인증 401 `A001`, 권한 없음 403 `A002`.

## 7. 게시물 API (FR-008)

| 메서드 | URL | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/v1/posts/image-upload-urls` | 사진 업로드 URL 발급. 요청 `{files: [{contentType, size}]}` → `[{imageKey, uploadUrl}]` | 직원 |
| POST | `/api/v1/posts` | 작성 `{category, title, content}` → 201 `{id}`. `content`는 본문 HTML | 직원 |
| GET | `/api/v1/posts?category=&page=&size=` | 최신순 목록 (`PageResponse<PostSummaryResponse>`). `category` 없으면 전체, 기본 `size` 12, 최대 100 | - |
| GET | `/api/v1/posts/{postId}` | 상세 (`content`는 사진 `src`가 채워진 HTML, `authorId` 포함) | - |
| PUT | `/api/v1/posts/{postId}` | 전체 수정. 사진 목록은 새 본문에 나온 사진으로 통째로 바뀐다 | 작성자·관리자 |
| DELETE | `/api/v1/posts/{postId}` | soft delete | 작성자·관리자 |

- `category`: `GENERAL`(게시글), `NEWS`(소식), `EVENT`(행사 프로그램).
- 본문 HTML 허용 목록: `p br strong em u a ul ol li blockquote hr figure figcaption img`. 그 밖의 태그·속성(`script`, `style`, `on*` 등)은 제거한다.
  - `a[href]`는 http/https만. 응답에 `target="_blank" rel="noopener noreferrer"`를 붙인다.
  - 사진 묶음: `<figure class="post-photos">` 안에 `<img data-image-key="…">` 1~3장 + `<figcaption>`(선택, 200자). `img`는 사진 묶음 안에만 둘 수 있다.
  - 본문 최대 100,000자, 글자나 사진이 하나 이상. 위반은 `P004`.
- 사진 흐름: URL 발급 → 브라우저가 `uploadUrl`에 `PUT`(발급 때 보낸 `Content-Type`·크기 그대로) → 본문 HTML에 `data-image-key`로 넣어 작성·수정. 본문에 처음 나온 사진이 대표 사진. 수정 시 상세 응답 HTML을 그대로 편집해 보내면 된다(`src`는 서버가 지운다).
- 사진 제한: 게시물당 최대 20장(`P002`), jpeg·png·webp, 장당 10MB. 형식·크기 위반, 업로드되지 않은 키, `posts/` 밖의 키, 중복 키는 `P003`.
- 권한 (FR-009): **직원** = `STAFF`·`ADMIN`. **작성자·관리자** = 직원인 작성자 본인 또는 `ADMIN`. 직원 권한이 회수된 작성자는 자기 글도 수정·삭제할 수 없다.
  - 역할은 토큰이 아니라 DB의 현재 역할로 `PostService`가 검사한다. 비로그인은 401 `A001`, 권한 없음은 403 `A002`.
- 없거나 삭제된 게시물 `P001`(404).

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

## 9. 기타

- 날짜/시간: ISO-8601 (`2026-10-07T14:30:00`), 서버 타임존 `Asia/Seoul`.
- JSON 필드는 camelCase.
- `null` 필드도 형식 일관성을 위해 포함한다 (위 예시처럼).
- API 버전은 호환되지 않는 변경 시에만 올린다 (`/api/v2`).
