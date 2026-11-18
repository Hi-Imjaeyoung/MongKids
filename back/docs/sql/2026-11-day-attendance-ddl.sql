-- 근태 관리 하루 단위 수업 시작·종료 (#94, EMP-FR-020) 테이블.
-- 운영 DB는 ddl-auto: validate라서 이 PR을 배포하기 전에 운영 RDS에 먼저 실행한다 (docs/deploy-checklist.md K).
-- 새 테이블만 만든다. 기존 수업 단위 기록(class_start, class_start_attempt, class_end, class_end_image)은 지우거나 옮기지 않고 보관한다.

create table day_start (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    staff_id bigint not null,
    work_date date not null,
    institution_id bigint not null,
    method varchar(20) not null,
    status varchar(20) not null,
    latitude float(53),
    longitude float(53),
    accuracy float(53),
    distance_meters integer,
    checked_at datetime(6) not null,
    reason varchar(200),
    reject_reason varchar(200),
    processed_by bigint,
    processed_at datetime(6),
    primary key (id)
) engine=InnoDB;

create table day_end (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    staff_id bigint not null,
    work_date date not null,
    institution_id bigint not null,
    method varchar(20) not null,
    status varchar(20) not null,
    latitude float(53),
    longitude float(53),
    accuracy float(53),
    distance_meters integer,
    checked_at datetime(6) not null,
    reason varchar(200),
    reject_reason varchar(200),
    processed_by bigint,
    processed_at datetime(6),
    corrected_at datetime(6),
    primary key (id)
) engine=InnoDB;

create table day_end_image (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    day_end_id bigint not null,
    image_key varchar(255) not null,
    sort_order integer not null,
    primary key (id)
) engine=InnoDB;

create table day_end_note (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    day_end_id bigint not null,
    institution_id bigint not null,
    note TEXT not null,
    primary key (id)
) engine=InnoDB;

create table day_check_attempt (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    staff_id bigint not null,
    work_date date not null,
    type varchar(20) not null,
    institution_id bigint not null,
    latitude float(53),
    longitude float(53),
    accuracy float(53),
    distance_meters integer,
    within_radius bit not null,
    attempted_at datetime(6) not null,
    primary key (id)
) engine=InnoDB;

alter table day_start add constraint uk_day_start_staff_date unique (staff_id, work_date);
alter table day_end add constraint uk_day_end_staff_date unique (staff_id, work_date);
alter table day_end_note add constraint uk_day_end_note_institution unique (day_end_id, institution_id);
create index idx_day_check_attempt_staff_date on day_check_attempt (staff_id, work_date);

alter table day_start add constraint fk_day_start_staff foreign key (staff_id) references member (id);
alter table day_start add constraint fk_day_start_institution foreign key (institution_id) references institution (id);
alter table day_start add constraint fk_day_start_processed_by foreign key (processed_by) references member (id);
alter table day_end add constraint fk_day_end_staff foreign key (staff_id) references member (id);
alter table day_end add constraint fk_day_end_institution foreign key (institution_id) references institution (id);
alter table day_end add constraint fk_day_end_processed_by foreign key (processed_by) references member (id);
alter table day_end_image add constraint fk_day_end_image_day_end foreign key (day_end_id) references day_end (id);
alter table day_end_note add constraint fk_day_end_note_day_end foreign key (day_end_id) references day_end (id);
alter table day_end_note add constraint fk_day_end_note_institution foreign key (institution_id) references institution (id);
alter table day_check_attempt add constraint fk_day_check_attempt_staff foreign key (staff_id) references member (id);
alter table day_check_attempt add constraint fk_day_check_attempt_institution foreign key (institution_id) references institution (id);
