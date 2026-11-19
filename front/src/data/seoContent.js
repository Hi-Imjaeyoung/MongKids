// 검색 노출(NFR-006): 브라우저 탭 제목과 검색 결과 설명 문구.
// 대상 검색어(몽키즈, 몽키즈유아체육, 운영 지역 + 유아체육·방문체육)는 초안이라 대표 확인 후 문구만 고친다.
// index.html의 기본 title·description과 같은 값을 유지한다 (자바스크립트를 실행하지 않는 수집기는 index.html만 읽는다)
export const siteMeta = {
  name: '몽키즈유아체육',
  origin: 'https://mongkiz.com',
  title: '몽키즈유아체육 | 용인·수원·성남 어린이집·유치원 방문 유아체육',
  description:
    '용인·수원·성남 어린이집·유치원·가정으로 찾아가는 방문 유아체육 전문 브랜드 몽키즈(MONGKIZ). 정기 방문체육, 1:1 가정 방문체육, 특수 체육, 운동회·체육대회까지 안내합니다.',
}

// 화면별 탭 제목. 탭에는 "{title} | 몽키즈유아체육"으로 보인다. description이 없으면 기본 설명을 쓴다
export const pageMeta = {
  about: {
    title: '몽키즈 소개',
    description: '대표가 직접 교육한 강사, 연령별 월별 커리큘럼, 대체강사 시스템까지. 방문 유아체육 전문 브랜드 몽키즈를 소개합니다.',
  },
  programs: {
    title: '프로그램 안내',
    description: '어린이집·유치원 정기 방문체육, 1:1 가정 방문체육, 태권도장 유아체육, 특수 체육, 운동회·체육대회, 복지센터·공공기관 프로그램을 안내합니다.',
  },
  faq: {
    title: '자주 묻는 질문',
    description: '몽키즈 방문 유아체육 수업 지역, 대상 연령, 신청 방법, 시범수업, 운동회 견적 등 자주 묻는 질문을 모았습니다.',
  },
  news: {
    title: '수업 현장',
    description: '어린이집·유치원·가정에서 진행한 몽키즈 방문 유아체육 수업 현장을 소개합니다.',
  },
  lab: {
    title: '몽키즈 연구소',
    description: '부모님의 궁금증, 교구 이야기, 아이의 움직임과 성장에 대한 유아체육 정보를 전합니다.',
  },
  reviews: {
    title: '수업 후기',
    description: '몽키즈 방문 유아체육 수업을 함께한 아이들과 보호자·선생님의 후기입니다.',
  },
  privacy: { title: '개인정보처리방침' },
  plans: {
    title: '수업 계획안',
    description: '몽키즈 방문 유아체육의 연간 수업 계획안을 확인하세요. 주차별 월간 계획안은 인증된 기관 회원에게 제공합니다.',
  },
  recruit: {
    title: '강사 채용',
    description: '몽키즈 방문 유아체육 강사를 모집합니다. 지원서 양식을 내려받아 작성한 뒤 올려 주시면 지원이 끝납니다.',
  },
  // 검색에 노출하지 않는 화면 (robots.txt에서 막음). 탭 제목만 구분한다
  write: { title: '글쓰기' },
  edit: { title: '글 수정' },
  admin: { title: '관리자' },
  attendance: { title: '수업 체크' },
  me: { title: '내 정보' },
}

// 프로그램 상세 (SCR-016~020, SCR-026) 설명 문구
export function programMetaDescription(program) {
  return `${program.name} ${program.summary}. 몽키즈 방문 유아체육 프로그램을 안내합니다.`
}
