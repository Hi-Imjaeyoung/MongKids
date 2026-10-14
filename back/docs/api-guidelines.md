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

## 7. 기타

- 날짜/시간: ISO-8601 (`2026-10-07T14:30:00`), 서버 타임존 `Asia/Seoul`.
- JSON 필드는 camelCase.
- `null` 필드도 형식 일관성을 위해 포함한다 (위 예시처럼).
- API 버전은 호환되지 않는 변경 시에만 올린다 (`/api/v2`).
