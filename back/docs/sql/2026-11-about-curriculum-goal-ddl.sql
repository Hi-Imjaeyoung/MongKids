-- ABOUT 몽키즈(SCR-002) 월별 커리큘럼을 월 / 활동 주제 / 목표 세 칸으로 나눔 (FR-034).
-- 운영 DB는 ddl-auto: validate라서 이 변경을 배포하기 전에 운영 RDS에 먼저 실행한다.
-- 기본값 ''가 있어 지금 운영 중인 jar의 저장(goal 없이 insert)도 그대로 동작한다.
-- 이미 저장된 줄은 목표가 빈 값이 되고, 화면은 목표가 비어 있으면 그 칸을 그리지 않는다. 관리자가 다시 저장하면 채워진다.

alter table about_curriculum_month
    add column goal varchar(40) not null default '';
