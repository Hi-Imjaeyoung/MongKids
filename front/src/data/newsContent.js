// 몽키즈 소식(SCR-006) 화면 문구

export const newsHero = {
  eyebrow: '몽키즈 소식',
  title: '아이들이 신나게 뛰노는 현장',
  highlight: '신나게 뛰노는',
  description: '몽키즈의 수업과 행사 현장, 새로운 소식을 사진과 함께 만나보세요.',
}

// id는 백엔드 PostCategory 값. ALL은 분류 없이 전체 조회
export const newsCategories = [
  { id: 'ALL', label: '전체' },
  { id: 'GENERAL', label: '게시글', tone: 'yellow' },
  { id: 'NEWS', label: '소식', tone: 'blue' },
  { id: 'EVENT', label: '행사 프로그램', tone: 'green' },
]

export const newsList = {
  totalPrefix: '총',
  totalSuffix: '건',
  write: '글쓰기',
  loadMore: '더 보기',
  empty: '아직 등록된 게시물이 없어요.',
  loading: '게시물을 불러오는 중이에요.',
  imageLabel: '대표 사진',
}

export const newsDetail = {
  backToList: '목록으로',
  edit: '수정',
  delete: '삭제',
  deleteConfirm: '이 게시물을 삭제할까요?',
  loading: '게시물을 불러오는 중이에요.',
  notFound: '게시물을 찾을 수 없어요.',
  photoAlt: (index, total) => `현장 사진 ${index}/${total}`,
}

export const newsEditor = {
  createTitle: '게시물 작성',
  editTitle: '게시물 수정',
  category: '분류',
  title: '제목',
  titlePlaceholder: '제목을 입력해 주세요 (100자 이내)',
  content: '본문',
  contentPlaceholder: '현장 이야기를 들려주세요.',
  images: '사진',
  imagesHint: '최대 20장, 장당 10MB 이하 (JPG·PNG·WEBP). 첫 번째 사진이 대표 사진이에요.',
  addImages: '사진 추가',
  moveBefore: '앞으로',
  moveAfter: '뒤로',
  removeImage: '사진 빼기',
  representative: '대표',
  cancel: '취소',
  submitCreate: '등록하기',
  submitEdit: '수정하기',
  submitting: '저장 중…',
  loginRequired: '게시물은 로그인한 뒤 작성할 수 있어요.',
  login: '로그인',
  forbidden: '작성자만 게시물을 수정할 수 있어요.',
  loading: '불러오는 중이에요.',
  errors: {
    required: '제목과 본문을 입력해 주세요.',
    tooMany: '사진은 최대 20장까지 올릴 수 있어요.',
    invalidFile: 'JPG·PNG·WEBP 형식의 10MB 이하 사진만 올릴 수 있어요.',
    submit: '저장하지 못했어요. 잠시 후 다시 시도해 주세요.',
  },
}
