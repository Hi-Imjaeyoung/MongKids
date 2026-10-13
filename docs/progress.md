# 프로젝트 진행 사항

프론트엔드와 백엔드의 진행 상황을 함께 기록한다. 항목에는 가능하면 요구사항 ID([requirements.md](requirements.md))를 붙인다.

## 현재 상태

| 영역 | 상태 |
|---|---|
| 공통 | 모노레포 구성 완료. 요구사항 문서는 아직 비어 있음 |
| 백엔드 | Spring Boot 초기 프로젝트 + 개발 규칙 문서. 도메인 코드 없음, DataSource 설정 검증 완료(`local`), 테스트는 H2 |
| 프론트엔드 | Vite + React 프로젝트. 홈 화면(SCR-001) 시안을 정적 데이터로 구현 |

## 마일스톤

## 진행 로그

| 날짜 | 영역 | 내용 |
|---|---|---|
| 2026-10-07 | 백엔드 | Spring Initializr로 프로젝트 생성 |
| 2026-10-07 | 백엔드 | `CLAUDE.md`, 개발 규칙 문서(`back/docs/`) 작성 |
| 2026-10-08 | 프론트엔드 | 홈 화면 시안 구현 (SCR-001, API 미연동) |
| 2026-10-08 | 공통 | 프론트·백 모노레포로 통합, 공유 문서(`docs/`) 구성 |
| 2026-10-13 | 백엔드 | yml profile 설정(`local`, `prod`)과 `.env` 구성 추가 (빌드 검증은 DB·Entity 생성 후) |
| 2026-10-13 | 백엔드 | 기본 패키지 `com.example.demo` → `com.mongkids`, 프로젝트명 `demo` → `mongkids` 변경 |
| 2026-10-13 | 백엔드 | 테스트 DB로 H2 의존성 추가, `back/docs/testing.md` 테스트 DB 전략을 H2 기준으로 변경. `application-test.yml` 작성, `contextLoads`가 H2로 통과 |
| 2026-10-13 | 백엔드 | 로컬 MariaDB(`mongkids`)로 `local` profile DataSource 기동 검증 (`bootRun`, health `UP`) |

## 이슈 / 블로커

- 프론트엔드: 실제 사진·로고·아이콘 리소스가 없어 플레이스홀더와 이모지로 대체되어 있다.

## 다음 할 일

### 공통
- [ ] 요구사항(사용자 역할, 기능 목록) 정리

### 백엔드
- [x] DataSource / profile 설정
- [x] 로컬 DB로 DataSource 설정 검증 (`gradlew bootRun`)
- [x] 패키지 `com.example.demo` → `com.mongkids` 변경
- [x] `src/test/resources/application-test.yml` 작성 + 테스트에 `@ActiveProfiles("test")` 적용 (H2로 `contextLoads` 통과 확인)
- [ ] `global/` 공통 클래스 구현

### 프론트엔드
- [ ] 실제 이미지·로고 리소스 반영
- [ ] 라우팅 도입 및 하위 페이지 구성
- [ ] API 연동 (`/api` 프록시 설정 포함)
