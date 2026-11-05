-- 회의록 2026-11-05 반영 백엔드 변경: 견적 신청(FR-031), 후기 유형 삭제(FR-013·021·025), 현장 사진 분류 삭제(FR-008).
-- 운영 DB는 ddl-auto: validate라서 1단계는 이 변경을 배포하기 전에 운영 RDS에 먼저 실행한다 (docs/deploy-checklist.md N).
-- 1단계는 지금 운영 중인 jar와 새 jar 모두에서 문제가 없다. 2단계는 새 jar 배포를 확인한 뒤 실행한다.

-- ===== 1단계: 배포 전 =====

-- 견적 신청 (FR-031). 상담 신청(consultation)의 세 번째 유형(type = 'QUOTE')이다.
-- 참여 대상·희망 프로그램은 enum 이름을 쉼표로 이은 문자열 (예: 'PRESCHOOL,ADULT'), 고르지 않은 희망 프로그램은 NULL.
create table quote_consultation (
    id bigint not null,
    institution_name varchar(100) not null,
    schedule_undecided bit not null,
    event_date date,
    event_time time(0),
    venue_type varchar(20) not null,
    address varchar(255),
    participant_groups varchar(100) not null,
    child_count integer,
    guardian_count integer,
    programs varchar(100),
    note text,
    primary key (id),
    constraint fk_quote_consultation_consultation foreign key (id) references consultation (id)
) engine=InnoDB;

-- 현장 사진(EVENT) 분류 삭제 (FR-008): 기존 게시물은 게시글로 옮긴다.
-- 새 jar는 EVENT 값을 읽지 못하므로 반드시 배포 전에 실행한다.
update post set category = 'GENERAL' where category = 'EVENT';

-- 후기 유형 삭제 (FR-013): 기존 무료체험 후기는 수업 후기로 바꾸고,
-- 새 jar가 type 없이 저장해도 실패하지 않도록 기본값을 둔다.
update review set type = 'CLASS' where type = 'FREE_TRIAL';
alter table review alter column type set default 'CLASS';

-- ===== 2단계: 새 jar 배포 확인 후 =====

-- 쓰지 않는 type 컬럼과 그 인덱스를 지우고, 최신순 목록용 인덱스를 둔다.
-- alter table review drop index idx_review_type_created_at;
-- alter table review drop column type;
-- create index idx_review_created_at on review (created_at);
