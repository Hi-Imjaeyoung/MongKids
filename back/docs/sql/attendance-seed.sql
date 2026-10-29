-- 근태 관리 시드 데이터: 기관 위치와 날짜별 수업 할당.
-- 관리자 화면(EMP-SCR-006, #63)이 생기기 전까지 이 SQL을 직접 실행해 넣는다. 앱은 시드를 넣지 않는다.
--
-- 쓰는 법
-- 1. 직원 회원 id를 확인한다 (직원 권한은 관리자 화면 SCR-014 "회원·직원 권한"에서 지정).
--      select id, name, role from member where role in ('STAFF', 'ADMIN');
-- 2. 아래 @staff_id, 기관 이름·주소·좌표, 수업 날짜·시간을 실제 값으로 바꾼다.
--    좌표는 지도에서 기관 출입구 쪽을 찍어 소수점 6자리까지 넣는다. 좌표가 틀리면 그 기관의 수업 시작이 모두 반경 밖으로 거절된다.
-- 3. 운영 DB에 넣을 때는 docs/deploy-checklist.md "운영 DB 조회"의 터널로 접속해 실행한다.
--
-- 아래 값은 로컬 확인용 예시다.

set @staff_id = 1;

insert into institution (name, address, latitude, longitude, created_at, updated_at)
values ('예시 해님어린이집', '서울특별시 중구 세종대로 110', 37.566535, 126.977969, now(6), now(6));
set @institution_id = last_insert_id();

-- 오늘부터 사흘 동안 하루 두 번
insert into class_session (institution_id, staff_id, class_date, start_time, end_time, target, created_at, updated_at)
values
    (@institution_id, @staff_id, curdate(), '10:00', '10:50', '햇살반', now(6), now(6)),
    (@institution_id, @staff_id, curdate(), '14:00', '14:50', '달님반', now(6), now(6)),
    (@institution_id, @staff_id, curdate() + interval 1 day, '10:00', '10:50', '햇살반', now(6), now(6)),
    (@institution_id, @staff_id, curdate() + interval 2 day, '10:00', '10:50', '햇살반', now(6), now(6));
