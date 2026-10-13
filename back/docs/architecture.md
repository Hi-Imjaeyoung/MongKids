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

## 5. global 공통 구성 (구현 예정)

| 클래스 | 위치 | 역할 |
|---|---|---|
| `ApiResponse<T>` | `global/common` | 공통 응답 래퍼 ([api-guidelines.md](api-guidelines.md)) |
| `ErrorCode` | `global/exception` | HTTP 상태 + 에러 코드 + 메시지 enum |
| `BusinessException` | `global/exception` | `ErrorCode`를 담는 런타임 예외 |
| `GlobalExceptionHandler` | `global/exception` | `@RestControllerAdvice` 예외 → 응답 변환 |
| `BaseTimeEntity` | `global/common` | `createdAt`, `updatedAt` Auditing ([database.md](database.md)) |
| `JpaConfig` | `global/config` | `@EnableJpaAuditing` |

## 6. 설정 파일

- `application.yml`(공통) + `application-{local,dev,prod}.yml`(환경별)로 관리하는 것을 권장한다. (현재는 `application.properties` 하나뿐)
- 비밀값은 `${ENV_VAR}` 플레이스홀더로 주입한다.
- Actuator는 운영에서 `health`, `info`만 노출한다.
