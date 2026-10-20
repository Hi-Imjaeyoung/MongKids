// 홈 화면 정적 콘텐츠. 추후 API 연동 시 이 데이터를 응답값으로 대체한다.

export const navItems = [
  { label: '몽키즈 소개', href: '#about' },
  { label: '체육/행사프로그램', href: '#programs' },
  { label: '몽키즈 연구소', to: '/lab' },
  { label: '강사 채용', href: '#recruit' },
  { label: '수업 현장', to: '/news' },
  { label: '자주 묻는 질문', href: '#faq' },
]

export const ctaButtons = {
  institution: { label: '기관 프로그램 문의', icon: '🏫' },
  private: { label: '1:1 방문체육 상담', icon: '💬' },
}

export const hero = {
  titleLines: ['놀이는 신나게!', '성장은 알차게!'],
  highlight: '신나게!',
  description: ['어린이집·유치원부터 우리 집, 태권도장까지.', '아이들의 연령에 맞춰 찾아가는 즐거운 유아체육.'],
  area: '용인 · 수원 · 경기광주 중심 운영',
}

// 섹션 공통 제목. eyebrow 중 accent 부분만 파란색으로 강조한다.
export const sectionHeadings = {
  services: {
    eyebrow: 'OUR PROGRAMS',
    accent: 'PROGRAMS',
    title: '우리 아이에게 맞는 체육 프로그램',
    description: '정기 방문수업 3종과 행사·단기 프로그램을 만나보세요.',
  },
  why: { eyebrow: 'WHY MONGKIDS?', accent: 'MONGKIDS?', title: '몽키즈가 특별한 이유' },
  gallery: {
    eyebrow: 'CLASSROOM STORIES',
    accent: 'STORIES',
    title: '실제 몽키즈 수업 현장',
    description: '기관·가정·태권도장별 실제 사진은 게시 동의를 확인한 뒤 올립니다.',
  },
  process: { eyebrow: 'HOW IT WORKS', accent: 'WORKS', title: '상담부터 수업까지' },
  special: {
    eyebrow: 'SPECIAL PROGRAMS',
    accent: 'SPECIAL',
    title: '운동회와 단기 프로그램도 함께',
  },
  faq: {
    eyebrow: 'FAQ',
    accent: 'FAQ',
    title: '자주 묻는 질문',
    description: '상담 전에 많이 궁금해하시는 내용을 확인해 보세요.',
  },
}

// 확인되지 않은 수치는 쓰지 않고 운영 방식만 소개한다
export const highlights = [
  { icon: '📍', value: '용인·수원·경기광주', label: '중심 운영 지역' },
  { icon: '👩‍🏫', value: '대표 직접', label: '강사 교육' },
  { icon: '🧠', value: '연령별', label: '맞춤 수업 설계' },
  { icon: '🎨', value: '2개월마다', label: '스토리텔링 테마수업' },
]

export const services = [
  {
    id: 'school',
    theme: 'yellow',
    tag: '현재 주력 사업',
    title: '어린이집·유치원',
    description: '같은 교구도 연령과 활동 수준에 맞춰 다르게 구성하는 정기 방문체육',
    imageLabel: '기관 수업 사진',
    href: '#programs',
  },
  {
    id: 'homeclass',
    theme: 'blue',
    tag: '운영 중',
    title: '1:1 가정 방문체육',
    description: '아이의 연령과 흥미에 맞춰 강사가 가정으로 찾아가는 개별 체육수업',
    imageLabel: '1:1 수업 사진',
    href: '#private',
  },
  {
    id: 'taekwondo',
    theme: 'green',
    tag: '운영 중',
    title: '태권도장 유아체육',
    description: '태권도 기술 수업이 아닌, 도장 공간에서 진행하는 일반 유아체육',
    imageLabel: '태권도장 수업 사진',
    href: '#programs',
  },
]

export const specialPrograms = [
  {
    id: 'sportsday',
    icon: '🎈',
    tag: '운영 중',
    title: '운동회·체육대회',
    description: '기관과 단체의 규모·연령·장소에 맞춰 운동회와 체육대회를 진행합니다.',
    href: '#events',
  },
  {
    id: 'public',
    icon: '🏛️',
    tag: '운영 중',
    title: '복지센터·공공기관',
    description: '기관의 목적과 참여 연령에 맞춘 1회성·단기 체육 프로그램을 운영합니다.',
    href: '#events',
  },
]

export const gallery = {
  items: [
    { id: 'school', title: '기관 수업 현장' },
    { id: 'homeclass', title: '1:1 방문 수업' },
    { id: 'taekwondo', title: '태권도장 유아체육' },
  ],
  pending: '실제 사진 등록 예정',
  more: { label: '수업 현장 더 보기', to: '/news' },
}

export const processSteps = {
  steps: [
    { icon: '💬', theme: 'yellow', title: '상담 신청', description: '원하는 서비스 선택' },
    { icon: '📝', theme: 'blue', title: '조건 협의', description: '연령·장소·일정 확인' },
    { icon: '📅', theme: 'green', title: '일정 확정', description: '진행 조건 안내' },
    { icon: '🎈', theme: 'yellow', title: '수업 진행', description: '몽키즈 강사 방문' },
  ],
  note: '실제 절차는 프로그램과 기관 계약 조건에 따라 달라질 수 있습니다.',
}

export const faqPreview = [
  {
    question: '어느 지역에서 수업하나요?',
    answer: '현재 용인·수원·경기광주를 중심으로 활동합니다. 세부 방문 가능 여부는 위치와 일정에 따라 상담으로 안내합니다.',
  },
  {
    question: '수업 대상 연령은 어떻게 되나요?',
    answer: '프로그램별 대상 연령과 난이도가 다릅니다. 아이의 연령과 수업 형태를 확인한 뒤 안내합니다.',
  },
  {
    question: '수업은 어떻게 신청하나요?',
    answer: '상담 신청 → 운영 조건 및 일정 협의 → 수업 확정 → 진행 순서로 안내합니다. 세부 절차는 계약 유형에 따라 달라질 수 있습니다.',
  },
  {
    question: '운동회와 1회성 수업도 가능한가요?',
    answer: '네. 운동회·체육대회와 복지센터·공공기관의 1회성·단기 프로그램을 진행합니다.',
  },
]

export const contactCta = {
  title: '몽키즈와 함께하는 즐거운 체육시간',
  description: '어떤 형태의 수업을 찾고 계신가요?',
}

export const reasons = [
  {
    icon: '🎓',
    theme: 'blue',
    title: '체계적인 강사 교육',
    description: '엄격한 선발과 체계적인 교육으로 전문성을 갖춘 강사진이 수업을 진행합니다.',
    highlight: '전문성',
  },
  {
    icon: '📖',
    theme: 'yellow',
    title: '월별 커리큘럼',
    description: '연령별 발달에 맞춘 체계적인 커리큘럼으로 매월 새로운 수업이 진행됩니다.',
    highlight: '커리큘럼',
  },
  {
    icon: '🔄',
    theme: 'green',
    title: '대체강사 시스템',
    description: '담당 강사 변동 시에도 동일한 수업 품질을 유지하는 시스템을 운영합니다.',
    highlight: '수업 품질',
  },
  {
    icon: '🛡️',
    theme: 'purple',
    title: '안전 최우선',
    description: '아이들의 안전을 최우선으로 생각하며 체계적인 안전관리를 실천합니다.',
    highlight: '안전관리',
  },
  {
    icon: '💗',
    theme: 'pink',
    title: '대표 직접 관리',
    description: '대표가 직접 품질을 관리하여 신뢰할 수 있는 서비스를 제공합니다.',
    highlight: '직접 품질',
  },
]

// 실제 이용 후기만 싣는다. 후기 API(SCR-012) 연동 전까지는 비워 두고 빈 상태를 보여 준다.
export const reviews = []

export const reviewsCopy = {
  titleLines: ['이용자들의', '솔직한 이야기'],
  empty: '아직 공개된 후기가 없습니다.',
  emptySub: '실제 이용 고객의 동의와 확인을 거쳐 등록합니다.',
}

export const footer = {
  brand: '몽키즈유아체육',
  slogan: '놀이는 신나게! 성장은 알차게!',
  area: '용인 · 수원 · 경기광주 중심 운영 / 정확한 수업 가능 여부는 상담으로 안내합니다.',
  instagram: { label: '@m0ngkids_', href: 'https://www.instagram.com/m0ngkids_/' },
  pending: '연락처 · 사업자 정보 · 개인정보처리방침은 정식 오픈 전에 등록합니다.',
}

export const mobileCta = {
  institution: '기관 문의',
  private: '1:1 상담',
}

export const floatingActions = [
  { id: 'kakao', icon: '💬', label: '카카오톡\n상담하기', theme: 'yellow' },
  { id: 'phone', icon: '📞', label: '전화 문의', theme: 'blue' },
  { id: 'estimate', icon: '📋', label: '견적 신청', theme: 'green' },
]
