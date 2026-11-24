-- 후기 작성 코드(SCR-014 관리자 화면) 발급·목록·폐기 (FR-029, #117, ADR-005).
-- 운영 DB는 ddl-auto: validate라서 이 변경을 배포하기 전에 운영 RDS에 먼저 실행한다.
-- 새 테이블만 만들므로 지금 운영 중인 jar에도 영향이 없다.

create table review_code (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    code varchar(20) not null,
    expires_on date not null,
    kind varchar(10) not null,
    max_uses integer not null,
    memo varchar(50),
    revoked_at datetime(6),
    used_count integer not null,
    primary key (id)
) engine=InnoDB;

create unique index uk_review_code_code on review_code (code);
