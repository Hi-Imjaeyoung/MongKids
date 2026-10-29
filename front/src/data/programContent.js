// 체육/행사 프로그램 전체 안내(SCR-003)와 프로그램 상세(SCR-016~020) 정적 데이터

import classSchoolImage from '../assets/class-school.jpg'
import classHomeImage from '../assets/class-home-2.jpg'
import classTaekwondoImage from '../assets/class-taekwondo.jpg'
import schoolIcon from '../assets/program-icon-school.png'
import homeIcon from '../assets/program-icon-home.png'
import taekwondoIcon from '../assets/program-icon-taekwondo.png'
import sportsdayIcon from '../assets/program-icon-sportsday.png'
import publicIcon from '../assets/program-icon-public.png'

export const programGroups = [
  {
    id: 'regular',
    anchor: 'regular',
    eyebrow: 'REGULAR CLASSES',
    accent: 'CLASSES',
    title: '정기 방문체육',
  },
  {
    id: 'special',
    anchor: 'special',
    eyebrow: 'SPECIAL PROGRAMS',
    accent: 'PROGRAMS',
    title: '운동회·특별 프로그램',
    description: '운동회·체육대회, 복지센터·공공기관의 1회성·단기 프로그램을 안내합니다.',
  },
]

// consult: 상담 버튼 종류 (institution: 기관 프로그램 문의 SCR-009, personal: 1:1 방문체육 상담 SCR-010)
export const programs = [
  {
    id: 'school',
    group: 'regular',
    theme: 'yellow',
    tag: '현재 주력 사업',
    name: '어린이집·유치원',
    summary: '연령별 맞춤 정기 방문체육',
    blurb: '같은 교구도 연령과 활동 수준에 맞춰 다르게 구성하는 정기 체육수업',
    details: [
      '연령에 따라 활동 난이도와 진행 방식을 조정합니다.',
      '다양한 교구를 활용한 신체활동을 운영합니다.',
      '2개월마다 스토리텔링 테마수업을 진행합니다.',
    ],
    icon: schoolIcon,
    photo: classSchoolImage,
    photoLabel: '기관 수업 사진',
    consult: 'institution',
  },
  {
    id: 'home',
    group: 'regular',
    theme: 'blue',
    tag: '운영 중',
    name: '1:1 가정 방문체육',
    summary: '우리 집으로 찾아오는 맞춤 체육',
    blurb: '아이의 연령과 흥미에 맞춰 강사가 가정으로 찾아가는 개별 체육수업',
    details: [
      '가정으로 직접 방문하는 1:1 수업입니다.',
      '아이의 흥미와 활동 수준을 고려해 수업을 구성합니다.',
      '방문 가능 지역·준비 공간·일정은 상담 후 안내합니다.',
    ],
    icon: homeIcon,
    photo: classHomeImage,
    photoLabel: '가정 방문 수업 사진',
    consult: 'personal',
  },
  {
    id: 'taekwondo',
    group: 'regular',
    theme: 'green',
    tag: '운영 중',
    name: '태권도장 유아체육',
    summary: '도장에서 만나는 일반 유아체육',
    blurb: '태권도 기술 수업이 아닌, 태권도장 공간에서 진행하는 일반 유아체육',
    details: [
      '도장의 유아반을 위한 일반 유아체육입니다.',
      '다양한 교구 및 연령별 놀이체육을 운영합니다.',
      '도장 공간과 일정에 맞는 운영 조건을 협의합니다.',
    ],
    icon: taekwondoIcon,
    photo: classTaekwondoImage,
    photoLabel: '태권도장 수업 사진',
    consult: 'institution',
  },
  {
    id: 'sportsday',
    group: 'special',
    theme: 'yellow',
    tag: '운영 중',
    name: '운동회·체육대회',
    summary: '모두 함께 즐기는 특별한 날',
    blurb: '기관과 단체의 규모·연령·장소에 맞춰 진행하는 운동회와 체육대회',
    details: [
      '행사 장소·참여 인원·연령을 바탕으로 협의합니다.',
      '놀이체육과 레크리에이션 활동을 구성합니다.',
      '행사 일정과 운영 방식에 따라 별도 견적을 안내합니다.',
    ],
    icon: sportsdayIcon,
    photoLabel: '운동회 사진',
    consult: 'institution',
  },
  {
    id: 'public',
    group: 'special',
    theme: 'blue',
    tag: '운영 중',
    name: '복지센터·공공기관',
    summary: '1회성·단기 체육 프로그램',
    blurb: '복지센터와 공공기관의 목적에 따라 진행하는 단기 또는 1회성 체육 프로그램',
    details: [
      '기관별 행사 목적과 참여 연령을 확인합니다.',
      '1회성 및 단기 프로그램 운영 경험을 바탕으로 협의합니다.',
      '프로그램 내용·장소·인원에 따라 구성과 비용을 제안합니다.',
    ],
    icon: publicIcon,
    photoLabel: '공공기관 프로그램 사진',
    consult: 'institution',
  },
]

export function findProgram(programId) {
  return programs.find((program) => program.id === programId)
}

export function programPath(programId) {
  return `/programs/${programId}`
}

// 전체 안내 (SCR-003)
export const programsHero = {
  eyebrow: 'PROGRAMS',
  title: '필요한 장소로 찾아가는 유아체육',
  description: '기관, 가정, 태권도장까지 상황에 맞는 프로그램을 선택하세요.',
}

export const programsProcess = {
  eyebrow: 'HOW IT WORKS',
  accent: 'WORKS',
  title: '상담부터 수업까지',
  steps: [
    { title: '상담 신청', description: '원하는 서비스 선택' },
    { title: '조건 협의', description: '연령·장소·일정 확인' },
    { title: '일정 확정', description: '진행 조건 안내' },
    { title: '수업 진행', description: '몽키즈 강사 방문' },
  ],
  note: '실제 절차는 프로그램 및 기관 계약 조건에 따라 달라질 수 있습니다.',
}

export const programsCta = {
  title: '어떤 형태의 수업을 찾고 계신가요?',
  description: '정확한 수업 가능 여부는 상담으로 안내합니다.',
}

export const programCardMore = '자세히 보기'

// 상세 (SCR-016~020)
export const programDetail = {
  breadcrumbRoot: '체육/행사프로그램',
  eyebrow: 'MONGKIDS PROGRAM',
  guide: { eyebrow: 'PROGRAM GUIDE', accent: 'GUIDE', title: '프로그램 안내' },
  photoPending: '실제 사진 등록 예정',
  process: {
    eyebrow: 'PROCESS',
    accent: 'PROCESS',
    title: '상담부터 진행까지',
    steps: [
      { title: '문의', description: '서비스 선택' },
      { title: '협의', description: '내용·일정 확인' },
      { title: '확정', description: '운영 조건 정리' },
      { title: '진행', description: '수업 또는 행사' },
    ],
  },
  others: '다른 프로그램 보기',
}
