# 데이터베이스

> 테이블 정의(ERD·컬럼)는 [schema.md](schema.md)를 참고한다.

## 1. 현재 상태

- 드라이버: `org.mariadb.jdbc:mariadb-java-client` (runtimeOnly)
- `application.yml` 기반 profile 구성(`local`, `prod`)이 적용되어 있다. 기본 profile은 `local`.
- 로컬 DB 계정은 `back/.env`에서 읽는다. `.env`와 `application-local.yml`은 gitignore 대상이다.
- `local` profile의 DataSource 설정은 로컬 MariaDB(`mongkids` DB)로 기동 검증을 마쳤다 (`bootRun` 기동, `/actuator/health` `UP`). 로컬 MariaDB가 없으면 앱 기동은 접속 오류로 실패한다.
- 테스트는 `test` profile의 H2로 실행하므로 로컬 MariaDB가 없어도 통과한다. → [testing.md](testing.md)
- `.env`는 properties 형식으로 읽히므로 값 뒤의 공백이 값에 포함된다. 공백 없이 `KEY=value`로 적는다.

## 2. 설정 가이드

`application.yml` (공통, 커밋 O)
```yaml
spring:
  application:
    name: mongkids
  profiles:
    default: local
  config:
    import: optional:file:.env[.properties]
  jpa:
    open-in-view: false
    properties:
      hibernate:
        default_batch_fetch_size: 100
        jdbc.time_zone: Asia/Seoul
```

`application-local.yml` (로컬, **gitignore에 추가**)
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/mongkids
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    org.hibernate.SQL: debug
```

`application-prod.yml` (커밋 O, 값은 환경변수 — 운영은 Parameter Store `/mongkiz/prod/*`)
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
```

- 운영 DB는 RDS MariaDB 11.8 (`mongkiz-prod`, utf8mb4). TLS 연결만 받으므로 `DB_URL`에 `?sslMode=verify-full&serverSslCert=/opt/mongkiz/rds-global-bundle.pem`을 붙인다.
- RDS는 퍼블릭 접근이 꺼져 있다. 로컬 DB 프로그램으로 조회할 때는 SSM 포트 포워딩 터널(`127.0.0.1:13306`)과 개인 계정 `jaeyoung`을 쓴다 → [../../docs/deploy-checklist.md](../../docs/deploy-checklist.md#운영-db-조회)
- 설치 없이 바로 볼 때는 SSM 세션으로 EC2에 들어가 `mariadb --ssl --ssl-ca=/opt/mongkiz/rds-global-bundle.pem --ssl-verify-server-cert -h <엔드포인트> -u jaeyoung -p mongkids`로 한다.

### `.env`

- Spring Boot는 `.env`를 자동으로 읽지 않으므로 `spring.config.import`로 불러온다. 경로는 실행 디렉터리 기준이라 `back/`에서 실행해야 `back/.env`를 읽는다.
- `optional:`이므로 파일이 없으면 OS 환경변수만 사용한다. 같은 키가 있으면 OS 환경변수가 우선한다.
- 새로 받은 저장소에서는 `.env.example`을 `.env`로 복사해 값을 채우고, `application-local.yml`은 위 내용으로 직접 만든다.

```
DB_USERNAME=
DB_PASSWORD=
```

### `ddl-auto` 정책

| 환경 | 값 |
|---|---|
| local | `update` (또는 `create`) |
| test | `create-drop` |
| dev / prod | `validate` 또는 `none` — 스키마 변경은 마이그레이션 스크립트로 |

> 운영 스키마 관리가 필요해지면 Flyway 도입을 검토한다 (의존성 추가는 사용자 확인 후).

- 운영 초기 스키마(2026-10-22): 빈 DB라 `validate`로는 기동하지 못해서, 첫 기동만 환경 변수 `SPRING_JPA_HIBERNATE_DDL_AUTO=update`로 테이블 13개를 만들고 지운 뒤 `validate`로 재기동을 확인했다.
- 그 뒤로는 Entity를 바꾸면 배포 **전에** 같은 변경의 DDL을 운영 DB에 적용해야 한다. 적용하지 않으면 `validate`가 실패해 `deploy.sh`가 직전 jar로 되돌린다. 이 수작업이 잦아지면 Flyway를 도입한다.

## 3. 네이밍

- 테이블·컬럼: **snake_case** (Spring Boot 기본 `CamelCaseToUnderscoresNamingStrategy`가 자동 변환).
- 테이블명은 단수 (`member`, `class_schedule`).
- FK 컬럼: `{참조테이블}_id` (`member_id`).
- 문자셋: `utf8mb4` / `utf8mb4_unicode_ci`.

## 4. JPA 규칙

- 연관관계는 모두 `FetchType.LAZY`. (`@ManyToOne`, `@OneToOne`은 기본값이 EAGER이므로 반드시 명시)
- **N+1 방지**: 목록 조회 시 fetch join(`@Query ... join fetch`) 또는 `@EntityGraph` 사용. `default_batch_fetch_size`로 보조.
- `open-in-view: false` → 지연 로딩은 Service(트랜잭션) 안에서 끝내고 DTO로 변환해서 반환.
- 단순 조회는 Spring Data 메서드 이름 쿼리, 복잡해지면 `@Query`(JPQL).
- 벌크 수정 `@Modifying` 쿼리 후에는 영속성 컨텍스트 정합성 주의 (`clearAutomatically = true`).
- 삭제 정책(soft delete 여부)은 도메인별로 결정하고 Entity에 명시한다.

## 5. BaseTimeEntity (Auditing)

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;
}
```

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

> `@EnableJpaAuditing`을 메인 클래스에 붙이면 `@WebMvcTest`가 깨지므로 별도 `JpaConfig`에 둔다.

## 6. JdbcClient 사용 기준

`spring-boot-starter-jdbc`가 포함되어 있지만 기본은 JPA다. 다음 경우에만 `JdbcClient`(Spring 6.1+)를 사용한다.

- 수천 건 이상 대량 insert/update (배치)
- JPQL로 표현이 어려운 복잡한 통계·리포트 쿼리

규칙:
- 해당 도메인의 `repository` 패키지에 `XxxJdbcRepository`로 분리.
- SQL 파라미터는 반드시 named parameter 바인딩 (문자열 결합 금지 — SQL Injection 방지).
- JPA와 같은 트랜잭션 안에서 사용 시 flush 시점 주의.
