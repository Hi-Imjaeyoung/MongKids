-- 근태 관리 수업 시작·종료 체크 (#61, EMP-FR-001·002·010·011·013·014) 테이블.
-- 운영 DB는 ddl-auto: validate라서 이 PR을 배포하기 전에 운영 RDS에 먼저 실행한다 (docs/deploy-checklist.md K).
-- 로컬(local 프로필, ddl-auto: update)에서 Hibernate가 만든 DDL을 그대로 옮겼다. 제약 이름은 Hibernate가 붙인 이름이다.

create table institution (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    address varchar(255) not null,
    latitude float(53) not null,
    longitude float(53) not null,
    name varchar(100) not null,
    primary key (id)
) engine=InnoDB;

create table class_session (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    class_date date not null,
    end_time time(0) not null,
    start_time time(0) not null,
    target varchar(50),
    institution_id bigint not null,
    staff_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table class_start (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    accuracy float(53),
    distance_meters integer,
    latitude float(53),
    longitude float(53),
    method varchar(20) not null,
    note varchar(1000),
    processed_at datetime(6),
    reason varchar(200),
    reject_reason varchar(200),
    started_at datetime(6) not null,
    status varchar(20) not null,
    class_session_id bigint not null,
    processed_by bigint,
    primary key (id)
) engine=InnoDB;

create table class_start_attempt (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    attempted_at datetime(6) not null,
    accuracy float(53),
    distance_meters integer,
    latitude float(53),
    longitude float(53),
    within_radius bit not null,
    class_session_id bigint not null,
    member_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table class_end (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    corrected_at datetime(6),
    ended_at datetime(6) not null,
    note TEXT,
    class_session_id bigint not null,
    writer_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table class_end_image (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    image_key varchar(255) not null,
    sort_order integer not null,
    class_end_id bigint not null,
    primary key (id)
) engine=InnoDB;

alter table class_start add constraint UKsilt2qqphhcubinb6371gm933 unique (class_session_id);
alter table class_end add constraint UK790mi0j5qfmdvhq3ocolhidd4 unique (class_session_id);
create index idx_class_session_staff_date on class_session (staff_id, class_date);
create index idx_class_start_attempt_session on class_start_attempt (class_session_id);

alter table class_session add constraint FKqwlxuv0piuls34959o7y0vqln foreign key (institution_id) references institution (id);
alter table class_session add constraint FKjuyfmmkwcqrd4lq4ynu5ik1ri foreign key (staff_id) references member (id);
alter table class_start add constraint FKdimrkjf39kisbig0u9scr2e03 foreign key (class_session_id) references class_session (id);
alter table class_start add constraint FK3buu97455125scmf4qj7o4vxr foreign key (processed_by) references member (id);
alter table class_start_attempt add constraint FK7wrkdfvgb7w71oyxyr8vldq3s foreign key (class_session_id) references class_session (id);
alter table class_start_attempt add constraint FK69xlwdhdbfromhlaol3gk6lgh foreign key (member_id) references member (id);
alter table class_end add constraint FK344f5656serw5poutepihtwfv foreign key (class_session_id) references class_session (id);
alter table class_end add constraint FKr00rprqybxylv7y17j9r1w5wf foreign key (writer_id) references member (id);
alter table class_end_image add constraint FKi5pinp192wf4dg968yo3bcka9 foreign key (class_end_id) references class_end (id);
