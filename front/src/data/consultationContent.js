// 기관 상담 신청 모달(SCR-009) 문구와 선택지. value는 백엔드 enum 이름과 같다

export const institutionConsultation = {
  badge: '어린이집 · 유치원',
  title: '기관 프로그램 상담 신청',
  description: '남겨 주신 연락처로 담당자가 직접 연락드려요. 로그인하지 않아도 신청할 수 있어요.',
  submit: '상담 신청하기',
  submitting: '신청하는 중…',
  errorMessage: '신청하지 못했습니다. 잠시 후 다시 시도해 주세요.',
}

export const consultationFields = {
  institutionName: { label: '기관명', placeholder: '예) 몽키즈 어린이집' },
  schedule: { label: '희망 수업 일정', timeLabel: '희망 시간대', timePlaceholder: '희망 시간대  예) 오전 10시~11시' },
  children: { label: '원아 연령 · 인원', countLabel: '총 인원', countUnit: '명' },
  phone: { label: '연락받을 전화번호', placeholder: '010-1234-5678 또는 02-123-4567' },
  note: {
    label: '특이사항',
    optional: '(선택)',
    placeholder: '수업 공간, 시작 희망 시기 등 알려 주실 내용을 적어 주세요.',
    hint: '원아 개인의 건강정보는 적지 말아 주세요.',
  },
}

export const preferredDays = [
  { value: 'MONDAY', label: '월' },
  { value: 'TUESDAY', label: '화' },
  { value: 'WEDNESDAY', label: '수' },
  { value: 'THURSDAY', label: '목' },
  { value: 'FRIDAY', label: '금' },
  { value: 'SATURDAY', label: '토' },
]

export const childAgeGroups = [
  { value: 'UNDER_2', label: '만 2세 이하' },
  { value: 'AGE_3', label: '만 3세' },
  { value: 'AGE_4', label: '만 4세' },
  { value: 'AGE_5', label: '만 5세' },
  { value: 'OVER_6', label: '만 6세 이상' },
]

// 개인정보 수집·이용 안내 (FR-023)
export const privacyNotice = {
  title: '개인정보 수집·이용 안내',
  items: [
    { term: '수집 항목', description: '기관명, 연락받을 전화번호, 희망 수업 일정, 원아 연령·인원, 특이사항' },
    { term: '목적', description: '상담 연락 및 수업 안내' },
    { term: '보관 기간', description: '상담 완료 후 3년' },
  ],
  refusal: '동의하지 않을 수 있으며, 동의하지 않으면 상담 신청을 할 수 없어요.',
  agree: '개인정보 수집·이용에 동의합니다',
  required: '(필수)',
}

export const consultationDone = {
  title: '상담 신청이 접수되었어요',
  description: ['남겨 주신 전화번호로 담당자가 확인 후 연락드릴게요.', '조금만 기다려 주세요!'],
  confirm: '확인',
}

// 백엔드 검증(InstitutionConsultationRequest)과 같은 제한
export const consultationRules = {
  institutionNameMax: 100,
  preferredTimeMax: 100,
  childCountMin: 1,
  childCountMax: 999,
  noteMax: 1000,
  phonePattern: /^0\d{1,2}-?\d{3,4}-?\d{4}$/,
}

export const fieldErrorMessages = {
  phone: '전화번호 형식을 확인해 주세요.',
  childCount: '인원은 1~999명으로 입력해 주세요.',
  default: '입력값을 확인해 주세요.',
}
