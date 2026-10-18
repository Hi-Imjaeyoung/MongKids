# 디자인 시안

화면 디자인 시안과 관리 규칙을 둔다. 화면 ID는 [requirements.md](../requirements.md)를 따른다.

## 관리 규칙

초안은 아티팩트에서 작업하고, 확정되면 PNG를 이 폴더에 커밋한다.

- **초안 단계**: 아티팩트에서 작업·수정하고 코멘트로 의견을 받는다. 요구사항 표의 `디자인` 칸에는 `[초안](아티팩트 링크)`를 적고, 상태는 "디자인 초안, 확정 필요"로 둔다.
- **확정 시점**: 화면을 PNG로 내보내 `docs/design/{화면명}.png`로 커밋한다. `디자인` 칸은 `[recruit.png](design/recruit.png) · [편집 원본](아티팩트 링크)` 형식으로 바꾼다.
- **기준**: 구현·리뷰의 기준은 이 폴더의 PNG다. 아티팩트는 편집 원본이며, 확정 후 아티팩트를 고치면 PNG를 다시 내보내 함께 갱신한다.
- **공유**: 아티팩트는 기본 비공개다. 팀원에게 보여줄 때는 아티팩트의 Share 메뉴에서 공유한다.
- **파일명**: 소문자 영문, 화면 단위로 짓는다. 모바일은 `{화면명}-mobile.png`.

## 시안 목록

| 화면 | PNG (확정본) | 편집 원본 | 상태 |
|---|---|---|---|
| SCR-001 홈 | - | [아티팩트](https://claude.ai/artifact/JDRpMgxehzpa1g9cRtztPy) | 기획 v4 반영해 수정 (2026-10-18, 데스크톱). 확정 PNG 다시 내보내기 필요 |
| SCR-006 수업 현장 | - | [아티팩트](https://claude.ai/artifact/JDRpMgxehzpa1g9cRtztPy) | 초안 (확정 필요) |
| SCR-009 기관 상담 신청 | - | [아티팩트](https://claude.ai/artifact/JDRpMgxehzpa1g9cRtztPy) | 초안 (확정 필요) |
| SCR-010 개인 상담 신청 | - | [아티팩트](https://claude.ai/artifact/JDRpMgxehzpa1g9cRtztPy) | 초안 (확정 필요) |
| SCR-007 강사 채용 | - | [아티팩트](https://claude.ai/artifact/JDRpMgxehzpa1g9cRtztPy) | 초안 (확정 필요) |
| SCR-015 게시물 작성·수정 | - | [아티팩트](https://claude.ai/artifact/JDRpMgxehzpa1g9cRtztPy) | 초안 (확정 필요) |

- 초안은 아티팩트 하나("MongKids 화면 초안")에 모으고, 화면마다 페이지(`SCR-006 수업 현장` 등)를 나눈다.
