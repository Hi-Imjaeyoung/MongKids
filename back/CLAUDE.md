# CLAUDE.md

이 파일은 Mongkids 백엔드에서 작업할 때 Claude Code(및 개발자)가 따라야 할 핵심 규칙을 담는다.
저장소 공통 규칙은 루트 [CLAUDE.md](../CLAUDE.md), 백엔드 상세 규칙은 `back/docs/` 아래 문서를 참고한다.

## 프로젝트 개요

- Mongkids 백엔드 API 서버 (Spring Boot 기반 REST API)
- 현재 상태: Spring Initializr로 생성한 직후의 초기 프로젝트. 도메인 코드 없음.
- 요구사항 (프론트와 공유) → [../docs/requirements.md](../docs/requirements.md)
- 진행 사항 (프론트와 공유) → [../docs/progress.md](../docs/progress.md)

## 기술 스택

| 구분 | 내용 |
|---|---|
| Language | Java 17 (Gradle toolchain) |
| Framework | Spring Boot 4.1.1 (Spring Framework 7) |
| Build | Gradle 9.7.1 (wrapper), Groovy DSL |
| Web | `spring-boot-starter-webmvc` (서블릿 기반 MVC) |
| Data | `spring-boot-starter-data-jpa` (Hibernate), `spring-boot-starter-jdbc` |
| DB | MariaDB (`mariadb-java-client`) |
| 기타 | Actuator, Lombok |
| Test | JUnit 5, 각 starter의 `*-test` 모듈 |

> Spring Boot 4는 auto-configuration·테스트 어노테이션이 모듈별로 분리되었다.
> 3.x 시절 예제 코드의 import 경로를 그대로 쓰지 말고 실제 의존성 기준으로 확인한다.

## 자주 쓰는 명령

```bash
./gradlew build                 # 컴파일 + 테스트 + 패키징 (Windows: gradlew.bat build)
./gradlew test                  # 전체 테스트
./gradlew test --tests "com.mongkids.SomeTest"            # 단일 테스트 클래스
./gradlew test --tests "com.mongkids.SomeTest.methodName" # 단일 테스트 메서드
./gradlew bootRun --args='--spring.profiles.active=local'     # 로컬 실행
```

## 아키텍처 요약 → [docs/architecture.md](docs/architecture.md)

- **도메인형 패키지 구조**: `domain/{기능}/{controller,service,repository,dto,entity}` + `global/{config,exception,common}`
- 의존 방향: `Controller → Service → Repository` (역방향·건너뛰기 금지)
- Controller는 Entity를 직접 받거나 반환하지 않는다. 항상 DTO를 사용한다.
- 다른 도메인의 Repository를 직접 호출하지 않는다. 해당 도메인의 Service를 통한다.

## 핵심 코딩 규칙 → [docs/coding-conventions.md](docs/coding-conventions.md)

- 의존성 주입은 **생성자 주입만** 사용 (`private final` + `@RequiredArgsConstructor`). `@Autowired` 필드 주입 금지.
- Service 클래스에 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional`.
- Entity
  - `@Setter`, `@Data` 금지. 상태 변경은 의미 있는 이름의 메서드로 (`changeName()`, `cancel()`).
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, 생성은 `@Builder` 또는 정적 팩토리.
  - 연관관계는 모두 `FetchType.LAZY`.
- DTO는 Java `record`, 요청은 `XxxRequest`, 응답은 `XxxResponse`. 입력 검증은 Bean Validation + `@Valid`.
- 비즈니스 예외는 `BusinessException(ErrorCode)`로 던지고 `@RestControllerAdvice`에서 일괄 처리.

## API 규칙 → [docs/api-guidelines.md](docs/api-guidelines.md)

- URL: `/api/v1/{복수명사}` , kebab-case. 동사 금지.
- 응답은 공통 래퍼 `ApiResponse<T>` 사용, 에러는 `ErrorCode` 기반 형식.

## DB 규칙 → [docs/database.md](docs/database.md)

- 기본은 Spring Data JPA. 대량 배치·복잡한 통계 쿼리만 `JdbcClient` 허용.
- 테이블·컬럼은 snake_case. 모든 Entity는 `BaseTimeEntity`(생성/수정 시각) 상속.
- `spring.jpa.open-in-view=false`, 운영 환경 `ddl-auto`는 `validate` 또는 `none`.
- DB 접속 정보 등 **비밀값은 절대 커밋하지 않는다** (환경변수 / `application-local.yml`은 gitignore).

## 테스트 → [docs/testing.md](docs/testing.md)

- 새 기능·버그 수정에는 테스트를 함께 작성한다.
- Controller는 `@WebMvcTest`, Repository는 `@DataJpaTest`, 통합은 `@SpringBootTest`.
- given / when / then 구조, `@DisplayName`은 한글로 의도를 설명.

## Git → [../docs/git-workflow.md](../docs/git-workflow.md)

- 백엔드 커밋은 scope `back` 사용 (`feat(back): ...`, `fix(back): ...`)

## Claude 작업 원칙 (백엔드)

공통 원칙은 루트 [CLAUDE.md](../CLAUDE.md)를 따른다.

- 코드를 바꾼 뒤에는 `back/`에서 `./gradlew build`(최소 `test`)로 검증하고 결과를 그대로 보고한다.
- 기존 코드의 스타일(들여쓰기: 탭, 네이밍, 주석 밀도)을 따른다.

## 알려진 이슈 / TODO

- [ ] DataSource 설정 검증 필요: profile별 yml과 `.env` 구성은 추가했으나 빌드·기동 검증은 하지 않았다. 로컬 MariaDB와 `mongkids` DB, Entity 생성 후 `DemoApplicationTests.contextLoads`로 확인한다. → [docs/database.md](docs/database.md)
- [ ] `global/` 공통 클래스(`ApiResponse`, `ErrorCode`, `BusinessException`, `GlobalExceptionHandler`, `BaseTimeEntity`) 미구현.
- 문서의 아키텍처 결정은 초기 권장안이며, 변경 시 이 파일과 `back/docs/`를 함께 갱신한다.
