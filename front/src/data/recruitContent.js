// 강사 채용 (SCR-007) 화면 문구. 지원서 양식 내려받기와 작성한 지원서 올리기(FR-007)만 제공한다

export const recruitHero = {
  eyebrow: 'TEACHER RECRUIT',
  title: '아이들의 첫 체육 선생님이 되어주세요.',
  description: '지원서 양식을 내려받아 작성한 뒤, 이 페이지에서 올려 주시면 지원이 끝납니다.',
}

export const recruitApplySection = { title: '지원하기' }

export const recruitSummary = {
  title: '모집 요강',
  items: [
    { term: '모집 분야', description: '정규 체육수업 강사' },
    { term: '근무 형태', description: '정규직(수습 3개월), 프리랜서(12개월)' },
    { term: '활동 지역', description: '용인·수원·성남·동탄·오산·평택·광주 등 경기 남부' },
    { term: '지원 자격', description: '아이들을 좋아하는 분, 자차 소지 필수. 유아체육 경력·관련 전공·자격증 우대' },
  ],
}

// 지원서 양식 파일은 front/public/files/ 에 두고 href에 경로를 적는다. 비어 있으면 "준비 중" 안내만 보인다
export const recruitTemplate = {
  step: 'STEP 1',
  title: '지원서 양식 내려받기',
  description: '양식에 이름·연락처·경력 등을 적어 주세요.',
  button: '지원서 양식 내려받기',
  href: '',
  fileLabel: '',
  pending: '지원서 양식을 준비하고 있어요. 아래 채용 문의로 연락해 주세요.',
}

// 백엔드 검증(FR-007)과 같은 규칙: 파일 1개, PDF·DOCX·HWP, 10MB 이하
export const recruitFileRules = {
  extensions: ['pdf', 'docx', 'hwp'],
  accept: '.pdf,.docx,.hwp',
  maxBytes: 10 * 1024 * 1024,
}

export const recruitUpload = {
  step: 'STEP 2',
  title: '작성한 지원서 올리기',
  description: 'PDF·DOCX·HWP 파일 1개, 10MB까지',
  required: '*',
  invalidType: 'PDF·DOCX·HWP 파일만 올릴 수 있어요.',
  tooLarge: '10MB 이하 파일만 올릴 수 있어요.',
}

// 개인정보처리방침(privacyContent.js) "강사 지원" 항목과 같은 내용 (FR-023)
export const recruitPrivacyNotice = {
  title: '개인정보 수집·이용 안내',
  items: [
    { term: '수집 항목', description: '지원서 파일(지원서에 적은 정보)' },
    { term: '이용 목적', description: '채용 검토와 지원자 연락' },
    { term: '보관 기간', description: '채용 절차가 끝난 뒤 6개월이 지나면 삭제' },
  ],
  refusal: '동의하지 않으실 수 있으며, 동의하지 않으면 지원서를 제출할 수 없습니다.',
  policyLink: { label: '개인정보처리방침', to: '/privacy' },
  agree: '개인정보 수집·이용에 동의합니다.',
  required: '(필수)',
}

export const recruitSubmit = {
  label: '지원서 제출',
  submitting: '제출하는 중…',
  hint: '지원서 파일을 올리고 개인정보 수집·이용에 동의하면 제출할 수 있어요.',
  errorMessage: '지원서를 보내지 못했어요. 잠시 후 다시 시도하거나 채용 문의로 연락해 주세요.',
}

export const recruitDone = {
  title: '지원서가 접수되었습니다',
  description: ['지원해 주셔서 감사합니다. 지원서를 검토한 뒤', '지원서에 적어 주신 연락처로 연락드리겠습니다.'],
  home: '홈으로',
}

export const recruitContact = {
  label: '채용 문의',
  phone: '[전화번호]',
  email: '[이메일]',
  note: '양식 작성이 어렵거나 파일이 올라가지 않으면 연락해 주세요.',
}
