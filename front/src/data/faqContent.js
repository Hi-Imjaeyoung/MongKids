// 자주 묻는 질문(SCR-022) 정적 데이터. 홈 FAQ 미리보기와 프로그램 상세(SCR-016~020)도 이 문구를 함께 쓴다.
// 분류 id는 프로그램 id(programContent.js)와 같게 맞춘다.

export const faqHero = {
  eyebrow: 'FAQ',
  title: '자주 묻는 질문',
  description: '상담 전에 많이 궁금해하시는 내용을 확인해 보세요.',
}

export const faqCategories = [
  { id: 'all', label: '전체' },
  { id: 'common', label: '공통' },
  { id: 'school', label: '어린이집·유치원' },
  { id: 'home', label: '1:1 가정 방문' },
  { id: 'taekwondo', label: '태권도장' },
  { id: 'sportsday', label: '운동회·체육대회' },
  { id: 'public', label: '복지센터·공공기관' },
]

// alsoCategory: 함께 속하는 분류, categoryLabel: 두 분류에 걸칠 때 표시할 이름
export const faqs = [
  {
    id: 'region',
    category: 'common',
    question: '어느 지역에서 수업하나요?',
    answer: '현재 용인·수원·경기광주를 중심으로 활동합니다. 세부 방문 가능 여부는 위치와 일정에 따라 상담으로 안내합니다.',
  },
  {
    id: 'age',
    category: 'common',
    question: '수업 대상 연령은 어떻게 되나요?',
    answer: '프로그램별 대상 연령과 난이도가 다릅니다. 아이의 연령과 수업 형태를 확인한 뒤 안내합니다.',
  },
  {
    id: 'apply',
    category: 'common',
    question: '수업은 어떻게 신청하나요?',
    answer: '상담 신청 → 운영 조건 및 일정 협의 → 수업 확정 → 진행 순서로 안내합니다. 세부 절차는 계약 유형에 따라 달라질 수 있습니다.',
  },
  {
    id: 'school-same',
    category: 'school',
    question: '어린이집과 유치원의 수업이 같나요?',
    answer: '같은 교구를 사용하더라도 연령과 활동 수준에 따라 진행 방식과 난이도를 조정합니다.',
  },
  {
    id: 'school-trial',
    category: 'school',
    question: '시범수업을 받을 수 있나요?',
    answer: '현재 가능 여부와 진행 조건은 기관 위치와 강사 일정에 따라 별도로 상담합니다.',
  },
  {
    id: 'home-visit',
    category: 'home',
    question: '선생님이 집으로 방문하나요?',
    answer: '네. 몽키즈의 1:1 가정 방문체육은 강사가 가정으로 방문하는 수업입니다.',
  },
  {
    id: 'home-space',
    category: 'home',
    question: '집에 넓은 공간이 꼭 있어야 하나요?',
    answer: '수업에 필요한 공간은 프로그램과 교구에 따라 다릅니다. 상담 시 가정의 사용 가능한 공간을 확인합니다.',
  },
  {
    id: 'taekwondo-skill',
    category: 'taekwondo',
    question: '태권도 기술을 가르치는 수업인가요?',
    answer: '아닙니다. 태권도장 공간에서 진행하는 일반 유아체육 프로그램입니다.',
  },
  {
    id: 'event-oneoff',
    category: 'sportsday',
    alsoCategory: 'public',
    categoryLabel: '운동회·단기',
    question: '운동회와 1회성 수업도 가능한가요?',
    answer: '네. 운동회·체육대회와 복지센터·공공기관의 1회성·단기 프로그램을 진행합니다.',
  },
  {
    id: 'event-quote',
    category: 'sportsday',
    alsoCategory: 'public',
    categoryLabel: '운동회·단기',
    question: '행사 견적은 어떻게 받나요?',
    answer: '행사 예정일·장소·예상 인원·연령을 알려주시면 조건을 확인해 상담을 진행합니다.',
  },
]

export const faqCta = {
  title: '원하는 답을 찾지 못하셨나요?',
  description: '상담으로 우리 기관, 우리 아이에게 맞게 안내해 드립니다.',
}

const categoryLabels = Object.fromEntries(faqCategories.map((category) => [category.id, category.label]))

export function faqCategoryLabel(faq) {
  return faq.categoryLabel ?? categoryLabels[faq.category]
}

// 'all'이면 전체, 그 밖에는 해당 분류(함께 속하는 분류 포함)의 질문
export function faqsByCategory(categoryId) {
  if (categoryId === 'all') return faqs
  return faqs.filter((faq) => faq.category === categoryId || faq.alsoCategory === categoryId)
}

export function faqsByIds(ids) {
  return ids.map((id) => faqs.find((faq) => faq.id === id))
}
