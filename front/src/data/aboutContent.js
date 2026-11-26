// ABOUT 몽키즈(SCR-002) 정적 데이터.
// [대괄호] 문구는 대표 확인 전 빈칸이다. 확인되면 이 파일만 바꾼다.

import aboutMainImage from '../assets/about-main.jpg'
import trainingImage from '../assets/about-training.jpg'
import themeClassImage from '../assets/about-theme-class.jpg'
import teacherImage1 from '../assets/teacher-1.jpg'
import teacherImage2 from '../assets/teacher-2.jpg'
import teacherImage3 from '../assets/teacher-3.jpg'
import rabbitTeacherImage from '../assets/rabbit_teacher.png'
import whaleTeacherImage from '../assets/whale_teacher.png'

export const aboutHero = {
  eyebrow: 'ABOUT MONGKIZ',
  titleLines: ['즐거움과 체계적인', '수업을 함께'],
  descriptionLines: ['아이들에게는 기다려지는 체육시간,', '기관에는 신뢰할 수 있는 방문체육 파트너.'],
  area: '용인 · 수원 · 성남 · 동탄 · 오산 · 평택 · 광주 · 그 외 경기 남부지역',
  photo: aboutMainImage,
  photoAlt: '몽키즈 캐릭터를 따라 평균대 위를 걷는 아이와 응원하는 친구들 그림',
}

export const aboutStandards = {
  eyebrow: 'OUR STANDARD',
  accent: 'STANDARD',
  title: '몽키즈의 수업 원칙',
  more: '자세히 보기',
}

// anchor: 아래 원칙별 상세 섹션 id
export const aboutPrinciples = [
  {
    id: 'training',
    theme: 'yellow',
    number: '01',
    name: '대표 직접 강사 교육',
    summary: '대표가 직접 모든 강사를 교육한 후 수업을 진행합니다.',
    label: '강사 선발과 교육',
    title: '대표가 직접 확인한 강사만 수업합니다',
    // 상세 섹션은 줄을 나눠 보여 준다. summary는 위 수업 원칙 카드(OUR STANDARD)에서 쓴다
    titleLines: ['대표가 직접 확인한', '강사만 수업합니다'],
    detailLines: ['자격증만 확인하고 바로 배정하지 않습니다.', '수업 태도와 진행력을 확인한 뒤 기관수업에 배정합니다.'],
    photo: trainingImage,
    photoLabel: '강사 교육 사진',
    // 강사 선발 4단계 (2026-11-19 사용자 디자인 수정)
    steps: [
      { number: 1, theme: 'blue', title: '면담·태도 확인', description: '책임감과 아이 소통방식 확인' },
      { number: 2, theme: 'yellow', title: '기본교육', description: '유아 발달·안전·수업언어' },
      { number: 3, theme: 'green', title: '모의수업 평가', description: '30분 구성·동선·돌발대응 점검' },
      { number: 4, theme: 'pink', title: '현장 동행', description: '기관수업 동행 후 최종 배정' },
    ],
    stepsLabel: '강사 선발 단계',
    routine: '월별 프로그램 브리핑 · 교구 안전점검 · 수업 피드백 · 정기 보수교육',
  },
  {
    id: 'curriculum',
    theme: 'blue',
    number: '02',
    name: '연령별 맞춤 커리큘럼',
    summary: '어린이집과 유치원 아이들의 연령에 맞춰 활동을 구성합니다.',
    label: '연령별 맞춤 커리큘럼',
    title: '아이들 연령에 맞춘 월별 커리큘럼',
    // 연령 구분 칩과 연령별 카드는 같은 순서·색(theme)을 쓴다
    ageGroups: [
      { id: 'infant', theme: 'yellow', label: '영아 · 만 1~2세' },
      { id: 'toddler', theme: 'blue', label: '유아 · 만 3~4세' },
      { id: 'challenge', theme: 'green', label: '도전 · 만 5~7세' },
    ],
    ageCards: [
      {
        id: 'infant',
        title: '탐색하고 성공하는 움직임',
        keywords: '감각탐색 · 걷기 · 오르기 · 기본균형',
        description: '짧은 설명과 반복활동으로 안정감을 높입니다.',
      },
      {
        id: 'toddler',
        title: '규칙을 이해하고 반복하는 활동',
        keywords: '균형 · 점프 · 던지기 · 차례 지키기',
        description: '성공경험을 충분히 주고 규칙을 자연스럽게 익힙니다.',
      },
      {
        id: 'challenge',
        title: '협동하고 도전하는 복합활동',
        keywords: '민첩성 · 협응 · 팀워크 · 문제해결',
        description: '여러 동작을 연결하고 팀 미션으로 확장합니다.',
      },
    ],
    curriculum: {
      title: '월별 커리큘럼 예시',
      rows: [
        { month: '[월]', topic: '[활동 주제]', goal: '[목표]' },
        { month: '[월]', topic: '[활동 주제]', goal: '[목표]' },
        { month: '[월]', topic: '[활동 주제]', goal: '[목표]' },
      ],
      link: { label: '정규 체육수업 프로그램 보기', to: '/programs/school' },
    },
  },
  {
    id: 'activity',
    theme: 'green',
    number: '03',
    name: '새로운 활동 경험',
    summary: '다양한 교구와 2개월마다 진행하는 스토리텔링 테마수업을 활용합니다.',
    label: '새로운 활동 경험',
    title: '2개월마다 새로워지는 스토리텔링 테마수업',
    photo: themeClassImage,
    photoLabel: '테마수업 · 교구 사진',
    examples: [
      { label: '테마 예시', value: '폴짝! 개구리' },
      { label: '사용 교구', value: '포트리스, 연잎, 개구리' },
    ],
    // 수업 계획안(SCR-027) 진입. 테마 예시 아래 버튼으로 둔다 (#120)
    planLink: { label: '세부 수업 보러가기', to: '/plans' },
  },
]

// 월별 커리큘럼 예시 관리자 편집 (FR-034, ADR-008). 위 curriculum.rows가 기본 문구이고,
// 관리자가 저장한 값이 있으면 그 값을 보여 준다. 줄 수는 3줄 고정, 길이 제한은 백엔드 검증과 같다
export const aboutCurriculumEdit = {
  edit: '수정',
  save: '저장',
  saving: '저장 중…',
  cancel: '취소',
  reset: '기본 문구로 되돌리기',
  resetConfirm: '관리자가 저장한 월별 커리큘럼을 지우고 기본 문구로 되돌릴까요?',
  monthLabel: (index) => `${index + 1}번째 줄 월`,
  topicLabel: (index) => `${index + 1}번째 줄 활동 주제`,
  goalLabel: (index) => `${index + 1}번째 줄 목표`,
  monthPlaceholder: '예: 3월',
  topicPlaceholder: '활동 주제 (예: 기본 움직임 · 균형 잡기)',
  goalPlaceholder: '목표 (예: 몸의 중심 잡기)',
  monthMaxLength: 10,
  topicMaxLength: 40,
  goalMaxLength: 40,
  emptyError: '모든 줄의 월, 활동 주제, 목표를 입력해 주세요.',
}

export const aboutWhy = {
  label: '04 · 기관이 안심하고 맡기는 이유',
  eyebrow: 'WHY MONGKIZ',
  accent: 'MONGKIZ',
  title: '수업만 하고 끝나는 업체가 아닙니다',
  description: '강사 배정부터 월간 계획, 안전관리, 결강과 보강까지 본사에서 함께 관리합니다.',
  items: [
    { id: 'safety', theme: 'green', title: '수업 전 안전 점검', description: '공간·교구·인원에 맞춰 동선을 확인합니다.' },
    { id: 'plan', theme: 'blue', title: '월간 계획안 제공', description: '다음 달 활동 교구와 교육 목표를 미리 안내합니다.' },
    {
      id: 'substitute',
      theme: 'blue',
      title: '대체·보강 관리',
      description: '기관 현장 학습 등으로 빠진 수업은 보강으로 보장합니다.',
    },
    { id: 'manage', theme: 'yellow', title: '대표 직접 관리', description: '대표가 매주 기관 요청과 수업 피드백을 확인합니다.' },
  ],
}

export const aboutTeachers = {
  eyebrow: 'TEACHER TRAINING',
  title: '강사를 소개합니다',
  recruit: { label: '함께할 강사를 찾습니다 · 강사 채용', to: '/recruit' },
  photoLabel: '강사 사진',
  // 전담 분야: "전담 분야" 글자 뒤에 분야마다 뱃지 하나
  specialtyLabel: '전담 분야',
  items: [
    { id: 1, theme: 'mint', photo: teacherImage1, name: '몽키쌤', specialties: ['이벤트 및 레크레이션'] },
    { id: 2, theme: 'purple', photo: whaleTeacherImage, name: '고래쌤', specialties: ['0~3세'] },
    { id: 3, theme: 'sky', photo: teacherImage3, name: '악어쌤', specialties: ['3~7세'] },
    { id: 4, theme: 'pink', photo: rabbitTeacherImage, name: '토끼쌤', specialties: ['특수 체육'] },
    { id: 5, theme: 'yellow', photo: teacherImage2, name: '상어쌤', specialties: ['1대1 가정 방문'] },
  ],
}

export const aboutCta = {
  titleLines: ['우리 기관, 우리 아이에게 맞는 수업을', '상담으로 안내해 드립니다'],
  description: '정확한 수업 가능 지역과 일정은 상담으로 확인해 주세요.',
}
