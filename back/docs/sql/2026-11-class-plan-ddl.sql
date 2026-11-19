-- 수업 계획안(SCR-027) 관리자 등록·열람 제한 (FR-032, #107, ADR-009).
-- 운영 DB는 ddl-auto: validate라서 이 변경을 배포하기 전에 운영 RDS에 먼저 실행한다.
-- 새 테이블만 만들므로 지금 운영 중인 jar에도 영향이 없다.

create table class_plan (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    file_key varchar(255) not null,
    kind varchar(10) not null,
    original_file_key varchar(255),
    period varchar(100) not null,
    title varchar(50) not null,
    primary key (id)
) engine=InnoDB;
