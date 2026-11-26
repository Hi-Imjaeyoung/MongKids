-- 정기 수업(묶음)과 기관 사이 이동 시간 (EMP-FR-022~024, #125).
-- 운영 DB는 ddl-auto: validate라서 이 변경을 배포하기 전에 운영 RDS에 먼저 실행한다.
-- 새 테이블 두 개와 NULL 허용 컬럼 하나만 추가하므로 지금 운영 중인 jar에도 영향이 없다.
-- 기존 수업(class_session)은 series_id가 NULL로 남는다(옮기지 않음, 사용자 결정 2026-11-26).

create table class_series (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    class_type varchar(20) not null,
    days_of_week varchar(70) not null,
    end_time time(0) not null,
    first_date date not null,
    last_date date not null,
    start_time time(0) not null,
    target varchar(50),
    institution_id bigint not null,
    staff_id bigint,
    primary key (id)
) engine=InnoDB;

alter table class_series add constraint fk_class_series_institution foreign key (institution_id) references institution (id);
alter table class_series add constraint fk_class_series_staff foreign key (staff_id) references member (id);

alter table class_session add column series_id bigint null;
alter table class_session add constraint fk_class_session_series foreign key (series_id) references class_series (id);

create table travel_time (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    distance_meters integer not null,
    duration_seconds integer not null,
    from_institution_id bigint not null,
    to_institution_id bigint not null,
    primary key (id)
) engine=InnoDB;

alter table travel_time add constraint uk_travel_time_route unique (from_institution_id, to_institution_id);
alter table travel_time add constraint fk_travel_time_from foreign key (from_institution_id) references institution (id);
alter table travel_time add constraint fk_travel_time_to foreign key (to_institution_id) references institution (id);
