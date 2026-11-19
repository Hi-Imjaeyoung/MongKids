// 수업 계획안(SCR-027) 정적 데이터. 계획안 등록 API(FR-032) 전까지 목록과 이미지는 이 파일에서 관리한다.
// 연간 계획안은 누구나 보고, 월간 계획안은 인증된 기관 회원만 원본을 본다(사용자 결정 2026-11-19).
// - image: 연간 계획안 이미지(공개)
// - preview: 월간 계획안의 블러 처리한 이미지(비인증 방문자에게 보여 줌)
// - 월간 원본 PDF는 프론트에 넣지 않는다. 서버가 인증을 확인한 뒤에만 내려준다(FR-032)
// 파일이 준비되기 전에는 null로 두면 이미지 자리 표시가 나온다. 파일은 src/assets/plans/에 두고 import해서 넣는다

export const plansHero = {
  eyebrow: 'CLASS PLAN',
  title: '한 해 동안 아이들이 할 체육 수업을 미리 확인하세요.',
  description: '연간 계획안은 누구나 볼 수 있고, 주차별 수업 내용이 담긴 월간 계획안은 인증된 기관 회원만 볼 수 있어요.',
}

export const plansText = {
  listLabel: '계획안 목록',
  groups: { annual: '연간 계획안', monthly: '월간 계획안' },
  publicBadge: '공개',
  lockedBadge: '잠김',
  listNote: '계획안은 관리자가 올리며, 새 달 계획안은 올라오는 대로 목록에 추가돼요.',
  toolbarLabel: '보기 도구',
  zoomIn: '확대',
  zoomOut: '축소',
  viewNote: '계획안은 이 화면에서만 볼 수 있어요. 내려받기와 인쇄는 지원하지 않아요.',
  imagePlaceholder: (title) => `${title} 이미지 준비 중`,
  previewAlt: (title) => `${title} 미리보기 (흐리게 처리됨)`,
  lock: {
    title: '월간 계획안은 인증된 기관 회원만 볼 수 있어요',
    guest: '로그인한 뒤 기관 인증을 받으면 월별 상세 계획안을 볼 수 있어요.',
    member: '기관 인증 신청은 준비 중이에요. 지금 계획안이 필요하면 기관 프로그램 문의로 알려 주세요.',
    login: '로그인하기',
    showAnnual: '연간 계획안 보기',
  },
}

export const plans = [
  {
    id: '2027-annual',
    kind: 'annual',
    title: '2027 연간 계획안',
    period: '2027.03 ~ 2028.02 · 월별 주차 프로그램',
    portrait: true,
    image: null,
  },
  {
    id: '2027-03',
    kind: 'monthly',
    title: '3월 계획안',
    period: '2027.03 · 1~5주차 · 영아/유아',
    preview: null,
  },
  {
    id: '2027-04',
    kind: 'monthly',
    title: '4월 계획안',
    period: '2027.04 · 1~4주차 · 영아/유아',
    preview: null,
  },
]
