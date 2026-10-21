// 수업 후기 화면(SCR-012) 문구

export const reviewHero = {
  eyebrow: 'REVIEW',
  title: '수업 후기',
  description: '몽키즈와 함께한 아이들과 보호자·선생님의 이야기입니다.',
  loginToWrite: '로그인하고 후기 쓰기',
}

// id는 백엔드 ReviewType. ALL은 전체 탭
export const reviewTypes = [
  { id: 'ALL', label: '전체' },
  { id: 'FREE_TRIAL', label: '무료체험 후기', tone: 'green' },
  { id: 'CLASS', label: '수업 후기', tone: 'blue' },
]

export const reviewList = {
  sort: '최신순',
  loading: '후기를 불러오는 중',
  loadingMore: '후기를 더 불러오는 중',
  ended: '모든 후기를 확인했어요',
  empty: '아직 등록된 후기가 없어요.',
  mine: '내 후기',
  edit: '수정',
  delete: '삭제',
  deleteConfirm: '이 후기를 삭제할까요? 삭제하면 되돌릴 수 없어요.',
  replyTitle: '몽키즈 답글',
  photoLabel: (index) => `후기 사진 ${index}`,
  ratingLabel: (rating) => `만족도 5점 중 ${rating}점`,
}

// 비로그인은 최근 후기 4개까지만 본다 (FR-012)
export const reviewGate = {
  title: '로그인하면 모든 후기를 볼 수 있어요',
  description: '비회원은 최근 후기 4개까지 볼 수 있습니다.',
  button: '로그인하고 더 보기',
}

export const reviewWriteBox = {
  // 앞에 회원 이름을 강조해 붙인다
  titleSuffix: '님, 후기를 남겨 주세요',
  hint: '유형 · 만족도 · 사진은 다음 단계에서 고를 수 있어요',
  placeholder: '아이와 함께한 몽키즈 수업은 어땠나요?',
  button: '후기 쓰기',
}

export const reviewForm = {
  createTitle: '후기 쓰기',
  editTitle: '후기 수정',
  typeLabel: '후기 유형',
  ratingLabel: '만족도',
  ratingHint: '바나나를 눌러 골라 주세요',
  ratingText: (rating) => `${rating}점 / 5점`,
  contentLabel: '후기 내용',
  contentPlaceholder: '아이가 수업에서 어떤 모습이었는지, 좋았던 점을 자유롭게 적어 주세요.',
  contentMaxLength: 1000,
  photoLabel: '사진',
  photoRule: '(선택 · 최대 5장 · 장당 10MB · JPG·PNG·WEBP)',
  photoHint: '첫 번째 사진이 목록에서 크게 보입니다.',
  photoAdd: '사진 추가',
  photoRemove: (index) => `사진 ${index} 빼기`,
  photoCountError: '사진은 최대 5장까지 올릴 수 있어요.',
  photoFileError: 'JPG·PNG·WEBP 형식의 10MB 이하 사진만 올릴 수 있어요.',
  notices: [
    '등록하면 바로 공개됩니다.',
    '작성자 이름은 일부를 가려서 보여 줍니다. (예: 김**)',
    '다른 아이의 얼굴이 나온 사진은 올리지 말아 주세요.',
  ],
  submit: '후기 등록',
  submitEdit: '수정 완료',
  submitting: '저장하는 중…',
  required: '유형·만족도·내용을 입력하면 등록할 수 있어요.',
  doneTitle: '후기가 등록되었어요',
  doneEditTitle: '후기가 수정되었어요',
  doneDescription: '소중한 이야기를 들려주셔서 감사합니다.',
  doneButton: '후기 목록 보기',
  close: '닫기',
}
