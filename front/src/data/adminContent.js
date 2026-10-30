// 관리자 화면(SCR-014) 문구

export const adminPage = {
  eyebrow: 'MONGKIDS ADMIN',
  title: '관리자',
  menuLabel: '관리 메뉴',
  checking: '로그인 상태를 확인하는 중이에요.',
  loginRequired: '관리자 계정으로 로그인해 주세요.',
  login: '로그인',
  notAdmin: '관리자만 볼 수 있는 화면이에요.',
  goHome: '홈으로',
  loading: '불러오는 중이에요.',
}

// 헤더 이름 옆 진입 링크 (ADMIN만)
export const headerAdminLink = '관리자'

// 처리하지 않은(신규) 상담 건수 배지 (FR-005). 화면에는 숫자만 보이고 화면 읽기 프로그램에는 이 문구를 읽어 준다
export const newConsultationCountLabel = (count) => `신규 상담 ${count}건`

// group이 있으면 메뉴에서 그 이름 아래에 모은다. 근태 관리 메뉴(EMP-SCR-006) 문구는 attendanceAdminContent.js
export const adminTabs = [
  { id: 'consultations', label: '상담 신청' },
  { id: 'reviews', label: '후기 관리' },
  { id: 'members', label: '회원 · 직원 권한' },
  { id: 'class-starts', label: '수업 시작 승인', group: '근태 관리' },
  { id: 'class-records', label: '수업 기록', group: '근태 관리' },
  { id: 'class-sessions', label: '수업 할당', group: '근태 관리' },
  { id: 'institutions', label: '기관 위치', group: '근태 관리' },
]

// 상담 신청 (FR-004, FR-026)
export const consultationTypes = [
  { id: 'ALL', label: '전체' },
  { id: 'INSTITUTION', label: '기관', tone: 'blue' },
  { id: 'PERSONAL', label: '개인', tone: 'yellow' },
]

export const consultationStatuses = [
  { id: 'RECEIVED', label: '신규', tone: 'yellow' },
  { id: 'CONTACTED', label: '연락 완료', tone: 'blue' },
  { id: 'COMPLETED', label: '상담 완료', tone: 'green' },
]

export const consultationAdmin = {
  title: '상담 신청',
  description: '홈페이지로 들어온 기관·개인 상담 신청이에요. 최근 접수 순으로 보여요.',
  allStatuses: '모든 상태',
  listLabel: '상담 신청 목록',
  columns: { createdAt: '접수일', type: '유형', title: '신청자', phone: '연락처', status: '상태' },
  empty: '조건에 맞는 상담 신청이 없어요.',
  detailLabel: '상담 신청 상세',
  selectHint: '목록에서 신청자를 고르면 상세 내용이 여기에 보여요.',
  call: '전화 걸기',
  statusLabel: '처리 상태',
  receivedAt: '접수',
  completedAt: '상담 완료',
  privacyAgreedAt: '개인정보 수집 동의',
  healthAgreedAt: '건강정보 수집 동의',
  typeSuffix: ' 상담',
}

// 유형별 상세 항목. format이 없으면 값을 그대로 보여 준다
export const institutionFields = [
  { key: 'institutionName', label: '기관명' },
  { key: 'preferredSchedule', label: '희망 일정' },
  { key: 'childAgeRange', label: '원아 연령' },
  { key: 'childCount', label: '원아 인원', format: (count) => `${count}명` },
  { key: 'note', label: '특이사항' },
]

export const personalFields = [
  { key: 'guardianName', label: '보호자 이름' },
  { key: 'childName', label: '아이 이름' },
  { key: 'childGender', label: '아이 성별', format: (gender) => (gender === 'MALE' ? '남아' : '여아') },
  { key: 'childBirthDate', label: '생년월일' },
  { key: 'childAgeMonths', label: '개월 수', format: (months) => `${months}개월` },
  { key: 'address', label: '수업 주소' },
  { key: 'attendsDaycare', label: '어린이집 · 유치원', format: (attends) => (attends ? '이용 중' : '이용하지 않음') },
  { key: 'temperament', label: '아이 성향' },
  { key: 'favoritePlay', label: '좋아하는 놀이' },
  { key: 'favoriteCharacter', label: '좋아하는 캐릭터' },
  { key: 'healthNote', label: '건강 · 주의사항' },
  { key: 'motivation', label: '신청 이유' },
]

// 후기 관리 (FR-021)
export const reviewAdmin = {
  title: '후기 관리',
  description: '부적절한 후기는 숨기면 후기 페이지에서 보이지 않아요. 숨긴 후기는 언제든 다시 공개할 수 있어요.',
  hiddenOnly: '숨긴 후기만 보기',
  hiddenBadge: '숨김',
  hide: '숨기기',
  unhide: '다시 공개',
  photos: (count) => `사진 ${count}장`,
  replied: '관리자 답글 있음',
  noReply: '답글 없음',
  viewOnPage: '후기 페이지에서 답글 달기',
  empty: '조건에 맞는 후기가 없어요.',
}

// 회원 · 직원 권한 (FR-022)
export const memberRoles = [
  { id: 'ALL', label: '전체' },
  { id: 'STAFF', label: '직원', tone: 'blue' },
  { id: 'MEMBER', label: '일반 회원', tone: 'gray' },
  { id: 'ADMIN', label: '관리자', tone: 'dark', hidden: true },
]

export const memberProviders = {
  KAKAO: '카카오',
  NAVER: '네이버',
}

export const memberAdmin = {
  title: '회원 · 직원 권한',
  description: '직원으로 지정한 회원은 수업 현장·연구소 게시물을 쓰고, 자기 글을 수정·삭제할 수 있어요.',
  searchLabel: '회원 검색',
  searchPlaceholder: '이름 또는 전화번호',
  search: '검색',
  listLabel: '회원 목록',
  columns: { name: '이름', phone: '전화번호', provider: '가입 경로', createdAt: '가입일', role: '권한' },
  empty: '조건에 맞는 회원이 없어요.',
  grant: '직원으로 지정',
  revoke: '직원 해제',
  grantConfirm: (name) => `${name} 님을 직원으로 지정할까요?\n수업 현장·연구소 게시물을 쓰고 자기 글을 수정·삭제할 수 있게 됩니다.`,
  revokeConfirm: (name) =>
    `${name} 님의 직원 권한을 해제할까요?\n이미 쓴 게시물은 남지만, 새 글 작성과 자기 글 수정·삭제는 할 수 없게 됩니다.`,
}
