// 상담 신청 모달(SCR-009 기관, SCR-010 개인) 문구와 선택지. value는 백엔드 enum 이름과 같다

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

// ---- 개인 상담 신청 (SCR-010, FR-002) ----

export const personalConsultation = {
  badge: '우리 아이 1:1 방문체육',
  title: '1:1 방문체육 상담 신청',
  description: '남겨 주신 연락처로 담당자가 직접 연락드려요. 로그인하지 않아도 신청할 수 있어요.',
  storyTitle: '아이에 대해 알려 주세요',
  storyDescription: '모두 선택 항목이에요. 적어 주시면 첫 수업을 아이에게 맞춰 준비할게요.',
  steps: ['기본 정보', '아이 이야기 · 동의'],
  next: '다음',
  prev: '이전',
  submit: '상담 신청하기',
  submitting: '신청하는 중…',
  errorMessage: '신청하지 못했습니다. 잠시 후 다시 시도해 주세요.',
}

export const personalConsultationFields = {
  optional: '(선택)',
  guardianName: { label: '보호자 이름', placeholder: '예) 김몽키' },
  // 번호 인증을 하지 않아 010을 고정하고 8자리만 받는다
  phone: {
    label: '휴대전화 번호',
    prefix: '010',
    middleLabel: '휴대전화 번호 가운데 4자리',
    lastLabel: '휴대전화 번호 마지막 4자리',
    middlePlaceholder: '1234',
    lastPlaceholder: '5678',
    hint: '번호 인증은 하지 않아요. 정확히 적어 주세요.',
  },
  childName: { label: '아이 이름', placeholder: '예) 김하늘' },
  childGender: { label: '아이 성별' },
  childAge: {
    birthLabel: '아이 생년월일',
    monthsLabel: '아이 개월 수',
    toMonths: '개월 수로 입력',
    toBirth: '생년월일로 입력',
    monthsUnit: '개월',
    hint: '생년월일이 기억나지 않으면 개월 수로 적어 주세요.',
  },
  address: {
    label: '수업받을 주소',
    placeholder: '예) 서울시 마포구 몽키로 12, 101동 1001호',
    hint: '강사가 방문할 주소예요. 동·호수까지 적어 주세요.',
  },
  attendsDaycare: { label: '어린이집 · 유치원 이용 여부' },
}

// 2단계 아이 이야기 (모두 선택 항목)
export const personalStoryFields = [
  { name: 'temperament', label: '아이 성향', placeholder: '예) 처음엔 낯을 가리지만 금방 친해져요', rows: 2 },
  { name: 'favoritePlay', label: '좋아하는 놀이 · 장난감', placeholder: '예) 공놀이, 블록', rows: 2 },
  { name: 'favoriteCharacter', label: '좋아하는 캐릭터 · 노래', placeholder: '예) 공룡 캐릭터, 동요', rows: 2 },
  {
    name: 'healthNote',
    label: '건강 · 알레르기 · 주의사항',
    placeholder: '예) 땅콩 알레르기가 있어요',
    rows: 2,
    hint: '적어 주시면 아래에서 건강정보 수집에 따로 동의해 주셔야 해요.',
  },
  { name: 'motivation', label: '신청 이유 · 기대하는 점', placeholder: '예) 몸으로 노는 시간을 늘려 주고 싶어요', rows: 3 },
]

export const childGenders = [
  { value: 'MALE', label: '남아' },
  { value: 'FEMALE', label: '여아' },
]

export const daycareOptions = [
  { value: true, label: '이용 중' },
  { value: false, label: '이용하지 않음' },
]

// 개인정보 수집·이용 안내 (FR-023)
export const personalPrivacyNotice = {
  ...privacyNotice,
  items: [
    {
      term: '수집 항목',
      description: '보호자 이름, 휴대전화 번호, 아이 이름·성별·생년월일(또는 개월 수), 수업받을 주소, 어린이집 이용 여부, 아이 성향, 좋아하는 놀이·캐릭터, 신청 이유',
    },
    { term: '목적', description: '상담 연락 및 수업 안내' },
    { term: '보관 기간', description: '상담 완료 후 3년' },
  ],
}

// 건강정보 별도 동의 (FR-023). 건강 메모를 적었을 때만 보여 준다
export const healthNotice = {
  title: '건강정보 수집·이용 안내 (별도 동의)',
  items: [
    { term: '수집 항목', description: '건강·알레르기·주의사항' },
    { term: '목적', description: '안전한 수업 준비' },
    { term: '보관 기간', description: '상담 완료 후 3년' },
  ],
  refusal: '동의하지 않으려면 건강 · 알레르기 · 주의사항 칸을 비워 주세요.',
  agree: '건강정보 수집·이용에 동의합니다',
  required: '(필수)',
}

// 백엔드 검증(PersonalConsultationRequest)과 같은 제한
export const personalConsultationRules = {
  nameMax: 50,
  phonePartLength: 4,
  ageMonthsMin: 0,
  ageMonthsMax: 120,
  addressMax: 255,
  storyMax: 1000,
}

export const personalFieldErrorMessages = {
  phone: '휴대전화 번호 8자리를 확인해 주세요.',
  childAgeMonths: '개월 수는 0~120으로 입력해 주세요.',
  childBirthDate: '생년월일을 확인해 주세요.',
  default: '입력값을 확인해 주세요.',
}
