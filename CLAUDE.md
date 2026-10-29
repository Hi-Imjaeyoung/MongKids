# CLAUDE.md

이 파일은 MongKids 저장소 전체에 공통으로 적용되는 규칙을 담는다.
영역별 규칙은 각 영역의 `CLAUDE.md`를 따른다. Claude Code는 하위 폴더에서 작업할 때 해당 폴더의 `CLAUDE.md`를 함께 읽는다.

## 프로젝트 개요

- 방문 유아체육 전문 브랜드 **몽키즈**의 웹 서비스
- 프론트엔드와 백엔드를 하나의 저장소에서 관리하는 모노레포

## 저장소 구조

```
MongKids/
├─ CLAUDE.md          저장소 공통 규칙 (이 파일)
├─ docs/              프론트·백 공유 문서
│  ├─ requirements/     요구사항 (프로젝트별 단일 출처)
│  │  ├─ requirement_homepage.md  홈페이지
│  │  └─ requirement_employee.md  근태 관리 (직원용)
│  ├─ progress.md       진행 사항
│  ├─ next-projects.md  홈페이지 외 신규 프로젝트 후보
│  ├─ git-workflow.md   브랜치·커밋·PR 규칙
│  ├─ deploy-checklist.md  배포 후 점검 체크리스트
│  ├─ design/           디자인 시안 (확정본 PNG, 관리 규칙)
│  └─ meetings/         회의록 (요구사항 추출 전용, 아래 "회의록 규칙" 참고)
├─ back/              백엔드: Spring Boot REST API  → back/CLAUDE.md
└─ front/             프론트엔드: Vite + React      → front/CLAUDE.md
```

## 문서 지도

| 문서 | 범위 | 내용 |
|---|---|---|
| [docs/requirements/requirement_homepage.md](docs/requirements/requirement_homepage.md) | 공유 | 홈페이지 화면(프론트)·기능(백엔드)·비기능 요구사항과 ID |
| [docs/requirements/requirement_employee.md](docs/requirements/requirement_employee.md) | 공유 | 근태 관리(직원용) 요구사항 (기획 단계) |
| [docs/progress.md](docs/progress.md) | 공유 | 영역별 현재 상태, 진행 로그, 다음 할 일 |
| [docs/next-projects.md](docs/next-projects.md) | 공유 | 홈페이지 외 신규 프로젝트 후보 (기획 단계, ID 없음) |
| [docs/git-workflow.md](docs/git-workflow.md) | 공유 | 브랜치, 커밋 scope, PR 체크리스트 |
| [docs/deploy-checklist.md](docs/deploy-checklist.md) | 공유 | 배포(S3 + CloudFront) 후 점검 항목 |
| [docs/design/](docs/design/) | 공유 | 디자인 시안 이미지(확정본)와 관리 규칙 |
| `docs/meetings/` | 공유 | 회의록. `/extract-requirements`에서만 사용 |
| [back/CLAUDE.md](back/CLAUDE.md), `back/docs/` | 백엔드 | 아키텍처, 코딩 컨벤션, API, DB, 테스트 |
| [front/CLAUDE.md](front/CLAUDE.md), `front/docs/` | 프론트엔드 | 폴더 구조, 컴포넌트·스타일 규칙 |

- 요구사항은 영역별로 따로 두지 않는다. 항상 `docs/requirements/`의 해당 프로젝트 문서를 갱신하고 영역 문서에서는 ID로 참조한다.
- 요구사항 ID: 홈페이지는 접두사 없이 `SCR-`·`FR-`·`NFR-`, 다른 프로젝트는 프로젝트 접두사를 붙인다(근태 관리: `EMP-SCR-001`). 규칙은 각 요구사항 문서의 "ID 규칙"을 따른다.
- 영역별 구현 규칙은 공유 문서에 쓰지 않는다. 각 영역의 `docs/`에 둔다.

## 회의록 규칙

요구사항은 `회의록(docs/meetings/) → /extract-requirements → docs/requirements/requirement_{프로젝트}.md` 순서로만 만든다.

- 회의록에 접근하는 작업은 `/extract-requirements` 하나뿐이다. 사용자가 이 명령을 실행했을 때만 회의록을 읽는다.
- 그 밖의 작업(코드, 설계, 문서, 리뷰 등)에서는 회의록을 읽지도, 검색하지도, 인용하지도 않는다. 기준은 `docs/requirements/`의 요구사항 문서다.
  - Bash(`cat`, `grep` 등)로 우회해서 읽는 것도 금지한다.
- 요구사항이 부족하거나 모호하면 회의록을 찾아보지 말고 사용자에게 묻거나 `/extract-requirements` 실행을 제안한다.
- 회의록은 수정하지 않는다.
- 설정으로도 제한되어 있다: 읽기는 매번 승인 요청(`.claude/settings.json`), 수정은 차단, Grep·Glob 검색에서 제외(`docs/meetings/.ignore`).

## 자주 쓰는 명령

```bash
# 백엔드 (back/ 에서)
./gradlew build            # Windows: gradlew.bat build
./gradlew bootRun

# 프론트엔드 (front/ 에서)
npm install
npm run dev
npm run lint && npm run build
```

## Git → [docs/git-workflow.md](docs/git-workflow.md)

- 커밋: Conventional Commits + 영역 scope (`feat(front):`, `fix(back):`, 공통 변경은 scope 생략)
- 프론트와 백 변경은 가능하면 커밋을 나눈다.

## Claude 작업 원칙 (공통)

- 요청 범위 밖의 코드는 수정하지 않는다. 필요해 보이면 먼저 제안한다.
- 새 의존성 추가, 폴더·패키지 구조 변경, 설정 파일 변경은 사용자 확인 후 진행한다.
- 코드를 바꾼 뒤에는 해당 영역의 빌드·검증 명령을 실행하고 결과를 그대로 보고한다.
- 검증하려고 띄운 서버(`bootRun`, `npm run dev` 등)는 검증이 끝나면 바로 모두 종료한다.
  - 백그라운드 작업만 멈추면 자식 프로세스(java, node)가 남을 수 있다. 포트(8080, 5173)를 점유한 프로세스까지 종료하고, 포트가 비었는지 확인한 뒤 보고한다.
- 기존 코드의 스타일(들여쓰기, 네이밍, 주석 밀도)을 따른다.
- 요구사항·진행 상황이 바뀌면 `docs/requirements/`의 해당 문서와 `docs/progress.md`를 함께 갱신한다.
- 비밀값(DB 접속 정보, API 키 등)은 절대 커밋하지 않는다.
