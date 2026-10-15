// 홈 화면 정적 콘텐츠. 추후 API 연동 시 이 데이터를 응답값으로 대체한다.

export const navItems = [
  { label: 'ABOUT 몽키즈', href: '#about' },
  { label: '프로그램', href: '#programs' },
  { label: '행사 프로그램', href: '#events' },
  { label: '1:1 방문체육', href: '#private' },
  { label: '몽키즈 소식', to: '/news' },
  { label: '강사 채용', href: '#recruit' },
  { label: '고객센터', href: '#support' },
]

export const ctaButtons = {
  institution: { label: '기관 프로그램 문의', icon: '🏫' },
  private: { label: '1:1 방문체육 상담', icon: '💬' },
}

export const hero = {
  titleLines: ['강사가 달라져도', '수업의 기준은 같습니다.'],
  highlight: '수업의 기준',
  description: ['체계적인 교육과 관리로 운영되는', '방문 유아체육 전문 브랜드, 몽키즈'],
}

export const stats = {
  baseline: '* 2024년 기준',
  items: [
    { icon: '🏫', value: '300', suffix: '+', label: '운영 어린이집' },
    { icon: '👦', value: '100', suffix: '+', label: '전문 강사진' },
    { icon: '🎈', value: '200', suffix: '+', label: '행사 진행 경험' },
    { icon: '⭐', value: '98', suffix: '%', label: '원장님 만족도' },
  ],
}

export const services = [
  {
    id: 'regular',
    theme: 'yellow',
    title: '정규 체육수업',
    description: '연령별 맞춤 커리큘럼으로 아이들의 성장과 발달을 도와줍니다.',
    imageLabel: '정규 수업 사진',
    href: '#programs',
  },
  {
    id: 'events',
    theme: 'blue',
    title: '행사 프로그램',
    description: '운동회, 가족행사, 체육대회 등 아이들과 함께하는 특별한 행사를 진행합니다.',
    imageLabel: '행사 사진',
    href: '#events',
  },
  {
    id: 'private',
    theme: 'green',
    title: '1:1 방문체육',
    description: '개별 맞춤 지도로 아이의 신체 발달과 자신감을 키워줍니다.',
    imageLabel: '1:1 수업 사진',
    href: '#private',
  },
]

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

export const reviews = [
  {
    id: 1,
    rating: 5,
    content: '아이들이 수업을 너무 좋아해요! 강사님이 정말 친절하고 전문적이세요.',
    author: '서울 ○○유치원 원장님',
  },
  {
    id: 2,
    rating: 5,
    content: '행사 진행을 맡기면 항상 만족스럽습니다. 준비부터 마무리까지 완벽해요!',
    author: '경기 ○○어린이집 원장님',
  },
  {
    id: 3,
    rating: 5,
    content: '1:1 수업으로 아이의 자신감이 많이 향상되었어요. 감사합니다!',
    author: '인천 ○○어린이집 원장님',
  },
  {
    id: 4,
    rating: 5,
    content: '매달 바뀌는 커리큘럼 덕분에 아이들이 체육 시간을 손꼽아 기다려요.',
    author: '부천 ○○어린이집 원장님',
  },
]

export const floatingActions = [
  { id: 'kakao', icon: '💬', label: '카카오톡\n상담하기', theme: 'yellow' },
  { id: 'phone', icon: '📞', label: '전화 문의', theme: 'blue' },
  { id: 'estimate', icon: '📋', label: '견적 신청', theme: 'green' },
]
