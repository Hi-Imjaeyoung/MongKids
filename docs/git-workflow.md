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
- 요약은 한글 또는 영어 중 하나로 통일 (기존 커밋은 영어 `chore: initialize spring project`).

## 3. Pull Request

- `main` 대상 PR로 병합. 병합 방식은 Squash merge 권장.
- PR 설명: 변경 내용, 변경 이유, 테스트 방법, 관련 요구사항 ID([requirements.md](requirements.md)) 또는 이슈.
- 병합 전 체크리스트 (변경한 영역만)
  - [ ] back: `./gradlew build` 통과 (`back/`에서 실행)
  - [ ] front: `npm run lint && npm run build` 통과 (`front/`에서 실행)
  - [ ] 새 기능/수정에 테스트 포함
  - [ ] 비밀값·개인정보 미포함
  - [ ] 규칙·구조 변경 시 해당 `CLAUDE.md`, `docs/` 갱신
  - [ ] 요구사항·진행 상황 변경 시 [requirements.md](requirements.md), [progress.md](progress.md) 갱신
  - [ ] 회의록(`docs/meetings/`)을 추가했다면 `/extract-requirements`로 요구사항에 반영

## 4. 커밋하면 안 되는 것

- `application-local.yml`, `.env` 등 비밀값 파일
- 빌드 산출물·의존성: `back/build/`, `back/.gradle/`, `front/node_modules/`, `front/dist/`
- IDE·OS 설정 파일

무시 규칙은 루트 `.gitignore`(공통)와 `back/.gitignore`, `front/.gitignore`(영역별)에 나눠 둔다.
