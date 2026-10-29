# Git 워크플로

MongKids는 프론트엔드(`front/`)와 백엔드(`back/`)를 하나의 저장소에서 관리하는 모노레포다.
이 문서는 두 영역에 공통으로 적용된다.

## 1. 브랜치

| 브랜치 | 용도 |
|---|---|
| `main` | 배포 가능한 상태 유지. 직접 커밋 지양 |
| `feature/{설명}` | 기능 개발 (`feature/member-signup`) |
| `fix/{설명}` | 버그 수정 |
| `refactor/{설명}` | 동작 변경 없는 구조 개선 |
| `docs/{설명}` | 문서 |
| `chore/{설명}` | 빌드·설정·의존성 |

- 브랜치명은 소문자 kebab-case. 이슈 번호가 있으면 포함 (`feature/12-member-signup`).
- 한쪽 영역만 바꾸는 브랜치는 이름에 영역을 드러내도 좋다 (`feature/front-home-page`).

## 2. 커밋 메시지 (Conventional Commits + scope)

```
<type>(<scope>): <요약 (50자 이내, 명령형)>

<본문: 무엇을, 왜 (선택)>
```

| type | 의미 |
|---|---|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 리팩터링 |
| `test` | 테스트 추가/수정 |
| `docs` | 문서 |
| `chore` | 빌드, 설정, 의존성 |
| `style` | 포맷팅 (로직 변경 없음) |

| scope | 대상 |
|---|---|
| `back` | `back/` 아래 변경 |
| `front` | `front/` 아래 변경 |
| (생략) | 루트 문서·설정 등 공통 변경, 또는 양쪽을 함께 바꾸는 변경 |

예시:
```
feat(back): 회원 가입 API 추가
fix(back): 회원 목록 조회 시 N+1 문제 해결
feat(front): 홈 화면 시안 구현
docs: 요구사항에 문의 기능 추가
```

- 한 커밋에는 하나의 논리적 변경만. 프론트와 백 변경은 가능하면 커밋을 나눈다.
- 요약과 본문은 한글로 작성한다. `type`, `scope`와 코드 식별자·파일명·명령어는 원문 그대로 둔다 (`feat(back): MemberService 가입 검증 추가`).
- 이 규칙 이전의 영어 커밋(`chore: initialize spring project` 등)은 고치지 않는다.

## 3. Pull Request

- `main` 대상 PR로 병합. 병합 방식은 Squash merge 권장.
- PR 설명: 변경 내용, 변경 이유, 테스트 방법, 관련 요구사항 ID([requirement_homepage.md](requirements/requirement_homepage.md)) 또는 이슈.
- PR 병합 후 브랜치(로컬·원격)를 제거할지는 사용자에게 승인받는다. 승인 없이 삭제하지 않는다.
- 병합 전 체크리스트 (변경한 영역만)
  - [ ] back: `./gradlew build` 통과 (`back/`에서 실행)
  - [ ] front: `npm run lint && npm run build` 통과 (`front/`에서 실행)
  - [ ] 새 기능/수정에 테스트 포함
  - [ ] 비밀값·개인정보 미포함
  - [ ] 규칙·구조 변경 시 해당 `CLAUDE.md`, `docs/` 갱신
  - [ ] 요구사항·진행 상황 변경 시 [requirement_homepage.md](requirements/requirement_homepage.md), [progress.md](progress.md) 갱신
  - [ ] 회의록(`docs/meetings/`)을 추가했다면 `/extract-requirements`로 요구사항에 반영

## 4. CI/CD (GitHub Actions)

영역별로 워크플로를 나누고 경로 필터로 바뀐 영역만 실행한다. 문서만 바뀌면 아무것도 실행되지 않는다.

| 워크플로 | 실행 조건 | PR (→ `main`) | `main` 반영 |
|---|---|---|---|
| `.github/workflows/front.yml` | `front/**` 변경 | `npm ci` → lint → build | 같은 검증 후 S3 업로드·CloudFront `/index.html` 무효화 |
| `.github/workflows/back.yml` | `back/**` 변경 | `gradlew build` (H2 테스트) | 같은 검증 후 jar S3 업로드 → SSM으로 EC2 `deploy.sh` 실행 (헬스 체크 실패 시 직전 jar로 롤백, 워크플로도 실패) |

- 두 워크플로 모두 Actions 탭에서 수동 실행(`workflow_dispatch`)할 수 있다.
- AWS 인증은 OIDC 역할로 받는다. 액세스 키를 Secrets에 넣지 않는다.
  - 역할 ARN은 저장소 변수 `AWS_FRONT_DEPLOY_ROLE_ARN`(`mongkiz-github-front-deploy`), `AWS_BACK_DEPLOY_ROLE_ARN`(`mongkiz-github-back-deploy`)에 둔다.
  - 역할은 이 저장소의 `main` 브랜치에서만 쓸 수 있다. 프론트는 프론트 버킷 쓰기·삭제와 배포 무효화, 백엔드는 jar 업로드와 운영 인스턴스 하나에 대한 `AWS-RunShellScript` 실행·결과 조회만 허용한다.
  - 두 역할의 신뢰 정책은 `token.actions.githubusercontent.com:sub`가 `repo:Hi-Imjaeyoung@106050747/MongKids@1408177560:ref:refs/heads/main`일 때만 허용한다 (저장소 소유자·이름·ID가 함께 들어가는 형식). 저장소 이름이나 소유자를 바꾸면 이 값도 두 역할 모두 함께 고쳐야 한다. 안 고치면 배포가 `Not authorized to perform sts:AssumeRoleWithWebIdentity`로 실패한다 (2026-10-29 `MongKidsTest` → `MongKids` 변경 때 겪음).
- 운영 DB는 `ddl-auto: validate`라서 Entity 스키마를 바꾼 PR은 병합 전에 RDS에 DDL을 먼저 적용한다 ([deploy-checklist.md](deploy-checklist.md)).
- PR의 CI가 실패하면 병합하지 않는다.

## 5. 커밋하면 안 되는 것

- `application-local.yml`, `.env` 등 비밀값 파일
- 빌드 산출물·의존성: `back/build/`, `back/.gradle/`, `front/node_modules/`, `front/dist/`
- IDE·OS 설정 파일

무시 규칙은 루트 `.gitignore`(공통)와 `back/.gitignore`, `front/.gitignore`(영역별)에 나눠 둔다.
