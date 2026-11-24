-- 수업 유형: 정기 수업 / 행사·특별활동 (EMP-FR-004, #110).
-- 운영 DB는 ddl-auto: validate라서 이 변경을 배포하기 전에 운영 RDS에 먼저 실행한다.
-- 기본값 'REGULAR'가 있어 지금 운영 중인 jar의 저장(class_type 없이 insert)도 그대로 동작하고,
-- 이미 등록된 수업은 모두 정기 수업이 된다. 행사 장소는 기관과 같이 등록하므로 다른 테이블은 바꾸지 않는다.

alter table class_session
    add column class_type varchar(20) not null default 'REGULAR';
