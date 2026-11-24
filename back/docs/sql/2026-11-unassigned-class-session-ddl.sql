-- 직원 미정 수업 (EMP-FR-021, #114): 수업 등록 때 담당 직원을 비워 두고 나중에 배정한다.
-- 운영 DB는 ddl-auto: validate라서 이 변경을 배포하기 전에 운영 RDS에 먼저 실행한다.
-- NULL을 허용하기만 하므로 지금 운영 중인 jar(항상 직원을 넣음)도 그대로 동작한다.
-- 외래 키(member.id)와 인덱스(staff_id, class_date)는 그대로 둔다.

alter table class_session
    modify staff_id bigint null;
