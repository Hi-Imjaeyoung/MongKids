// ABOUT 몽키즈(SCR-002) 정적 데이터.
// [대괄호] 문구는 대표 확인 전 빈칸이다. 확인되면 이 파일만 바꾼다.

import trainingImage from '../assets/about-training.jpg'
import themeClassImage from '../assets/about-theme-class.jpg'
import teacherImage1 from '../assets/teacher-1.jpg'
import teacherImage2 from '../assets/teacher-2.jpg'
import teacherImage3 from '../assets/teacher-3.jpg'

export const aboutHero = {
  eyebrow: 'ABOUT MONGKIDS',
  title: '즐거움과 체계적인 수업을 함께',
  descriptionLines: ['아이들에게는 기다려지는 체육시간,', '기관에는 신뢰할 수 있는 방문체육 파트너.'],
  area: '용인 · 수원 · 경기광주 중심 운영',
  photoLabel: '대표 사진 (게시 동의 사진)',
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
    label: '강사 교육',
    title: '대표가 직접 교육한 강사만 수업합니다',
    photo: trainingImage,
    photoLabel: '강사 교육 사진',
    checks: ['[교육 과정 · 기간]', '[수업 시연 · 평가 방식]', '[정기 보수 교육 주기]'],
  },
  {
    id: 'curriculum',
    theme: 'blue',
    number: '02',
    name: '연령별 맞춤 커리큘럼',
    summary: '어린이집과 유치원 아이들의 연령에 맞춰 활동을 구성합니다.',
    label: '연령별 맞춤 커리큘럼',
    title: '아이들 연령에 맞춘 월별 커리큘럼',
    ageGroups: ['[연령 구분 1]', '[연령 구분 2]', '[연령 구분 3]'],
    curriculum: {
      title: '월별 커리큘럼 예시',
      rows: [
        { month: '[월]', topic: '[활동 주제 · 목표]' },
        { month: '[월]', topic: '[활동 주제 · 목표]' },
        { month: '[월]', topic: '[활동 주제 · 목표]' },
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
      { label: '테마 예시', value: '[테마 이름]' },
      { label: '사용 교구', value: '[교구 종류]' },
    ],
  },
]

export const aboutWhy = {
  eyebrow: 'WHY MONGKIDS',
  accent: 'MONGKIDS',
  title: '기관이 안심하고 맡기는 이유',
  items: [
    {
      id: 'substitute',
      theme: 'blue',
      title: '대체강사 시스템',
      description: '강사 부재 시에도 수업이 빠지지 않도록 [대체강사 배정 방식]을 운영합니다.',
    },
    { id: 'safety', theme: 'green', title: '안전 관리', description: '[수업 전 안전 점검 · 보험 등 안전 기준]' },
    { id: 'manage', theme: 'yellow', title: '대표 직접 관리', description: '[수업 점검 · 기관 소통 방식]' },
  ],
}

export const aboutTeachers = {
  eyebrow: 'TEACHER TRAINING',
  title: '강사를 소개합니다',
  recruit: { label: '함께할 강사를 찾습니다 · 강사 채용', href: '#recruit' },
  photoLabel: '강사 사진',
  items: [
    { id: 1, photo: teacherImage1, name: '[강사 이름]', school: '[출신 학교 · 전공]', career: '[주요 경력 · 수업 횟수]' },
    { id: 2, photo: teacherImage2, name: '[강사 이름]', school: '[출신 학교 · 전공]', career: '[주요 경력 · 수업 횟수]' },
    { id: 3, photo: teacherImage3, name: '[강사 이름]', school: '[출신 학교 · 전공]', career: '[주요 경력 · 수업 횟수]' },
  ],
}

export const aboutCta = {
  titleLines: ['우리 기관, 우리 아이에게 맞는 수업을', '상담으로 안내해 드립니다'],
  description: '정확한 수업 가능 지역과 일정은 상담으로 확인해 주세요.',
}
