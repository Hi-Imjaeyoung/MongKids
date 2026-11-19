-- ABOUT 몽키즈(SCR-002) 월별 커리큘럼 관리자 편집 (FR-034, #101, ADR-008).
-- 운영 DB는 ddl-auto: validate라서 이 변경을 배포하기 전에 운영 RDS에 먼저 실행한다.
-- 새 테이블만 만들므로 지금 운영 중인 jar에도 영향이 없다. 행이 없으면 화면은 코드의 기본 문구를 쓴다.

create table about_curriculum_month (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    month_label varchar(10) not null,
    sort_order integer not null,
    topic varchar(40) not null,
    primary key (id),
    constraint uk_about_curriculum_month_sort_order unique (sort_order)
) engine=InnoDB;
