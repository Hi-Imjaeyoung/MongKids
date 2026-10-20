// 몽키즈 연구소(SCR-024) 화면 문구. 상세·작성 화면은 수업 현장(newsContent.js) 문구를 함께 쓴다

export const labHero = {
  eyebrow: 'MONGKIDS LAB',
  title: '아이의 움직임과 성장을 함께 알아가요',
  description: '부모님의 궁금증, 교구 이야기, 움직임과 성장에 대한 정보를 전합니다.',
  searchLabel: '연구소 콘텐츠 검색',
  searchInputLabel: '검색어',
  searchPlaceholder: '궁금한 내용을 검색해 보세요 (예: 편식, 균형감각)',
  searchButton: '검색',
}

// id는 백엔드 LabCategory 값. ALL은 분류 없이 전체 조회
export const labCategories = [
  { id: 'ALL', label: '전체' },
  { id: 'PARENT_QA', label: '부모님 궁금증', tone: 'yellow' },
  { id: 'GROWTH', label: '움직임과 성장', tone: 'blue' },
  { id: 'EQUIPMENT', label: '교구 이야기', tone: 'green' },
]

export const labList = {
  title: '몽키즈 연구소 콘텐츠',
  totalPrefix: '총',
  totalSuffix: '건',
  searchResult: '검색 결과',
  write: '글쓰기',
  loading: '콘텐츠를 불러오는 중이에요.',
  empty: '아직 등록된 콘텐츠가 없어요.',
  searchEmpty: (keyword) => `‘${keyword}’에 대한 콘텐츠가 없습니다`,
  searchEmptyHint: '다른 검색어로 찾아보거나 분류를 바꿔 보세요.',
  clearSearch: '검색어 지우기',
  imageLabel: '대표 이미지',
  pagination: '페이지',
  previousPage: '이전 페이지',
  nextPage: '다음 페이지',
}

// 목록 하단·상세 본문 아래 안내
export const labNotice = '건강·발달 관련 글은 참고용 정보입니다. 아이의 상태에 대한 판단은 전문가와 상담해 주세요.'

// 작성·수정 화면 분류 아래 작성자 안내
export const labEditorNotice = '건강·발달 관련 글은 최종 게시 전에 출처를 확인하고, 필요하면 전문가 검토를 받아 주세요.'
