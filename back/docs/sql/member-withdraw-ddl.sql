-- 회원 탈퇴 (#54, FR-028): 탈퇴하면 provider_id를 지우므로 NULL을 허용한다.
-- 운영 DB는 ddl-auto: validate라서 이 PR을 배포하기 전에 운영 RDS에 먼저 실행한다 (docs/deploy-checklist.md L).
-- uk_member_provider(provider, provider_id)는 그대로 둔다. UNIQUE는 NULL이 여러 행에 있어도 허용한다.

alter table member modify provider_id varchar(100) null;
