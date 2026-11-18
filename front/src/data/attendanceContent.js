// 근태 관리 하루 수업 시작·종료 체크 화면(EMP-SCR-001, EMP-FR-020) 문구

// 헤더 이름 옆 진입 링크 (STAFF·ADMIN)
export const headerAttendanceLink = '수업 체크'

export const attendancePage = {
  checking: '로그인 상태를 확인하는 중이에요.',
  loginRequired: '직원 계정으로 로그인해 주세요.',
  login: '로그인',
  notStaff: '직원만 볼 수 있는 화면이에요.',
  goHome: '홈으로',
  loading: '오늘 수업을 불러오는 중이에요.',
  empty: '오늘 할당된 수업이 없어요.',
  retry: '다시 불러오기',
  back: '뒤로',
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

// 오늘 날짜 표시: 10월 29일 (목)
export const todayLabel = (date) => `${date.getMonth() + 1}월 ${date.getDate()}일 (${WEEKDAYS[date.getDay()]})`

// 하루 상태. id는 백엔드 DayStatus. 수업 중(파랑)과 수업 종료(초록)는 색을 달리한다
export const dayStatuses = {
  NOT_STARTED: { label: '시작 전', tone: 'plain' },
  STARTED: { label: '수업 중', tone: 'blue' },
  PENDING: { label: '시작 승인 대기', tone: 'yellow' },
  REJECTED: { label: '시작 반려', tone: 'red' },
  END_PENDING: { label: '종료 승인 대기', tone: 'yellow' },
  END_REJECTED: { label: '종료 반려', tone: 'red' },
  ENDED: { label: '수업 종료', tone: 'green' },
}

// 하루 단위 적용 전 수업 단위 기록의 상태. id는 백엔드 ClassStatus
export const classStatuses = {
  NOT_STARTED: { label: '시작 전', tone: 'plain' },
  STARTED: { label: '수업 시작', tone: 'blue' },
  PENDING: { label: '승인 대기', tone: 'yellow' },
  REJECTED: { label: '반려', tone: 'red' },
  ENDED: { label: '수업 종료', tone: 'green' },
}

// 시작·종료 기록 한 줄
export const checkLine = {
  started: (time) => `${time} 시작`,
  ended: (time) => `${time} 종료`,
  manual: (time) => `수동 등록 ${time}`,
  adminRegistered: (time) => `관리자 등록 ${time}`,
  distance: (meters) => `기관까지 ${meters}m`,
  photoCount: (count) => `사진 ${count}장`,
  rejectReason: (reason) => `반려 사유: ${reason}`,
}

// ① 오늘 하루
export const dayToday = {
  title: (classCount, institutionCount) => `오늘 수업 ${classCount}개 · 기관 ${institutionCount}곳`,
  emptyTitle: '오늘 수업 없음',
  cardTitle: '오늘 하루',
  guide: {
    NOT_STARTED: (first) => [`첫 기관 `, first, `에 도착하면 수업 시작을 눌러 주세요. 오늘 수업 시작은 한 번만 해요.`],
    STARTED: (last) => [`마지막 기관 `, last, `에서 수업을 마치면 수업 종료를 눌러 주세요.`],
    PENDING: (last) => [
      `관리자가 확인하면 수업 중으로 바뀌어요. 승인 대기 중에도 마지막 기관 `,
      last,
      `에서 수업 종료를 할 수 있어요.`,
    ],
    REJECTED: (first) => [`첫 기관 `, first, `에서 다시 수업 시작을 확인해 주세요.`],
    END_PENDING: () => ['관리자가 확인하면 수업 종료로 바뀌어요.'],
    END_REJECTED: (last) => [`마지막 기관 `, last, `에서 다시 수업 종료를 해 주세요. 사진·특이사항도 다시 올려요.`],
    ENDED: () => ['오늘 수업을 모두 마쳤어요. 사진·특이사항은 오늘 안에 정정할 수 있어요.'],
  },
  endKept: '제출한 수업 종료 기록은 남아 있어요.',
  firstBadge: '첫 기관 · 시작 위치',
  lastBadge: '마지막 기관 · 종료 위치',
  onlyBadge: '시작·종료 위치',
  move: '이동 (체크 없음)',
  startByLocation: '현재 위치로 수업 시작',
  restart: '다시 수업 시작',
  endByLocation: '현재 위치로 수업 종료',
  reEnd: '다시 수업 종료',
  correct: '수업 기록 정정',
  radiusHint: (institution, radius) => `${institution} 반경 ${radius}m 안에서 확인해요`,
  locating: '위치를 확인하는 중이에요…',
  radius: 100,
}

// ② 위치 확인 실패. 반경 밖(outOfRange)과 위치를 못 받음(unavailable)·시간 초과(timeout)가 같은 화면을 쓴다
export const locationFail = {
  titles: { start: '수업 시작', end: '수업 종료' },
  outOfRange: {
    heading: '기관 근처로 확인되지 않았어요',
    message: (institution, distance, radius) =>
      `현재 위치가 ${institution}에서 약 ${distance}m 떨어져 있어요. (기준 ${radius}m)`,
  },
  unavailable: {
    heading: '위치를 받지 못했어요',
    message: '신호가 약하거나 위치를 찾지 못했어요.',
  },
  timeout: {
    heading: '위치를 받지 못했어요',
    message: '위치를 찾는 데 시간이 너무 오래 걸렸어요.',
  },
  tipsTitle: '이렇게 해 보세요',
  tips: ['건물 밖이나 창가로 이동한 뒤 다시 시도', '와이파이를 켜면 위치가 더 정확해져요'],
  retry: '다시 시도',
  manual: '사유 적고 수동 등록',
  guide: '위치 설정 방법 보기',
}

// ③ 위치 권한 거부·위치 꺼짐 안내
export const locationGuide = {
  titles: { start: '수업 시작', end: '수업 종료' },
  heading: '위치 정보를 켜 주세요',
  message: '수업 시작·종료 확인에 현재 위치가 필요해요. 위치가 꺼져 있거나 브라우저에서 허용하지 않았어요.',
  tabsLabel: '기기 선택',
  devices: [
    {
      id: 'ios',
      label: '아이폰',
      steps: [
        '설정 앱 → 개인정보 보호 및 보안 → 위치 서비스를 켜요',
        '같은 화면에서 Safari 웹 사이트 → “앱을 사용하는 동안”을 골라요',
        'Safari 주소창의 “가가” → 웹 사이트 설정 → 위치 → 허용',
      ],
    },
    {
      id: 'chrome',
      label: '안드로이드 Chrome',
      steps: [
        '화면 위에서 아래로 내려 빠른 설정의 “위치”를 켜요',
        'Chrome 주소창 왼쪽 아이콘 → 권한 → 위치 → 허용',
        '목록에 없으면 Chrome 설정 → 사이트 설정 → 위치에서 허용',
      ],
    },
    {
      id: 'samsung',
      label: '삼성 인터넷',
      steps: [
        '빠른 설정에서 “위치”를 켜요',
        '삼성 인터넷 메뉴 → 설정 → 사이트 및 다운로드 → 사이트 권한 → 위치',
        '이 사이트를 “허용”으로 바꿔요',
      ],
    },
  ],
  after: '설정을 바꾼 뒤 이 화면으로 돌아와 다시 시도를 눌러 주세요.',
  retry: '다시 시도',
  unsupported: '이 브라우저는 위치 확인을 지원하지 않아요. 다른 브라우저로 열거나 수동 등록해 주세요.',
  manual: '사유 적고 수동 등록',
}

// ④ 하루 시작 수동 등록. 하루 종료 수동 등록은 ② 수업 종료 화면에 사유 칸을 더해 쓴다
export const manualStart = {
  title: '수업 시작 수동 등록',
  institutionLabel: '시작 위치 (첫 기관)',
  measured: (distance, accuracy) =>
    `측정 위치: 기관까지 ${distance}m${accuracy != null ? ` · 정확도 ±${Math.round(accuracy)}m` : ''}`,
  notMeasured: '측정 위치: 위치를 받지 못했어요',
  reasonLabel: '사유',
  required: '(필수)',
  reasonPlaceholder: '예: 지하 강당이라 위치가 잡히지 않아요',
  reasonMaxLength: 200,
  notice: '관리자가 확인하기 전까지 승인 대기로 표시돼요. 승인 대기 중에도 수업 종료는 할 수 있고, 반려되면 다시 수업 시작을 확인할 수 있어요.',
  submit: '승인 요청하기',
  submitting: '요청하는 중…',
}

// ② 하루 수업 종료, 수동 종료, 수업 기록 정정
export const dayEndForm = {
  endTitle: '오늘 수업 종료',
  manualTitle: '수업 종료 수동 등록',
  editTitle: '수업 기록 정정',
  inRange: (institution) => `${institution} 반경 안이에요`,
  inRangeDetail: (distance, radius) => `마지막 기관까지 ${distance}m · 기준 ${radius}m`,
  summary: (classCount, route, startTime) =>
    [`수업 ${classCount}개`, route, startTime && `${startTime} 시작`].filter(Boolean).join(' · '),
  reasonLabel: '수동 등록 사유',
  reasonPlaceholder: '예: 체육실이 지하라 위치가 잡히지 않았어요',
  photoLabel: '오늘 수업 사진',
  photoRule: '1장 이상, 최대 20장 · 장당 10MB · JPG·PNG·WEBP',
  photoManyInstitutions: '여러 기관 사진을 한 번에 올려요.',
  photoAdd: '사진 추가',
  photoRemove: (index) => `${index}번째 사진 빼기`,
  photoCountError: '사진은 최대 20장까지 올릴 수 있어요.',
  photoFileError: 'JPG·PNG·WEBP 형식의 10MB 이하 사진만 올릴 수 있어요.',
  photoRequired: '수업 사진을 1장 이상 올려 주세요.',
  notesLabel: '기관별 특이사항',
  noteMaxLength: 2000,
  // 자세히 쓰도록 돕는 예시. 기관 순서대로 돌려 쓴다
  notePlaceholders: [
    '예: 달님반 한 아이가 넘어져 무릎을 살짝 긁혔어요. 담임 선생님께 전달했어요.',
    '예: 체육관 공사로 교실에서 수업했어요. 다음 주까지 같아요.',
    '예: 햇살반 3명 결석. 새로 온 아이가 줄넘기를 어려워해 따로 연습했어요.',
  ],
  endNotice:
    '위치를 확인하지 못하면 사유를 적어 수동 등록할 수 있어요. 관리자가 확인한 뒤 종료가 확정돼요. 제출한 기록은 오늘 안에 정정할 수 있어요.',
  manualNotice: '관리자가 확인하기 전까지 종료 승인 대기로 표시돼요. 제출한 기록은 오늘 안에 정정할 수 있어요.',
  editNotice: '정정해도 수업 시작·종료 시각은 바뀌지 않아요. 오늘 안에만 정정할 수 있어요.',
  lastCorrected: (time) => `마지막 정정: ${time ?? '없음'}`,
  submitEnd: '수업 종료 제출',
  submitManual: '승인 요청하기',
  submitEdit: '정정 내용 저장',
  submitting: '저장하는 중…',
}

// ⑧·⑩ 날짜별 수업 기록 (EMP-FR-015). 오늘 하루 헤더의 시계 버튼에서 들어간다
export const classHistory = {
  open: '내 수업 기록',
  title: '수업 기록',
  calendar: '달력에서 날짜 고르기',
  weekLabel: '날짜 선택',
  prevWeek: '이전 주',
  nextWeek: '다음 주',
  today: '오늘',
  weekRange: (start, end) =>
    `${start.getMonth() + 1}월 ${start.getDate()}일 – ${end.getMonth() + 1}월 ${end.getDate()}일`,
  weekday: (date) => WEEKDAYS[date.getDay()],
  dayLabel: (date, count) =>
    `${date.getMonth() + 1}월 ${date.getDate()}일 (${WEEKDAYS[date.getDay()]})${count ? `, 수업 ${count}개` : ''}`,
  dateTitle: (date, count) => `${todayLabel(date)} · 수업 ${count}개`,
  loading: '수업 기록을 불러오는 중이에요.',
  empty: '이날은 할당된 수업이 없어요.',
  retry: '다시 불러오기',
  scheduled: '예정',
  noRecord: '기록 없음',
  viewRecord: '기록 보기',
  legacyNotice: '하루 단위로 바뀌기 전 기록이라 수업마다 시작·종료가 따로 있어요.',
  note: (text) => `특이사항: ${text}`,
  pastNotice: '지난 수업 기록은 볼 수만 있어요. 정정은 그날 안에 오늘 하루 화면에서 할 수 있어요.',
  futureNotice: '수업 시작은 수업 당일 오늘 하루 화면에서 할 수 있어요. 일정이 바뀌면 관리자에게 알려 주세요.',
}

// ⑨ 지난 하루 기록 보기 (읽기 전용)
export const dayRecordView = {
  title: (date) => `${todayLabel(date)} 수업 기록`,
  start: '수업 시작',
  end: '수업 종료',
  corrected: '마지막 정정',
  rejectReason: '반려 사유',
  reason: '수동 등록 사유',
  checkText: (time, method, institution, distance) =>
    [time, method, institution, distance != null ? `기관까지 ${distance}m` : null].filter(Boolean).join(' · '),
  methods: { LOCATION: '위치 확인', MANUAL: '수동 등록', ADMIN: '관리자 등록' },
  photos: (count) => `수업 사진 ${count}장`,
  photoAlt: (index) => `수업 사진 ${index}`,
  notes: '기관별 특이사항',
  none: '없음',
  locked: '정정 기간(그날)이 지나 볼 수만 있어요.',
}

// 하루 단위 적용 전 수업 단위 기록 보기 (읽기 전용)
export const classRecordView = {
  title: (date) => `${todayLabel(date)} 수업 기록`,
  start: '수업 시작',
  end: '수업 종료',
  corrected: '마지막 정정',
  rejectReason: '반려 사유',
  distance: (meters) => `기관까지 ${meters}m`,
  methods: { LOCATION: '위치 확인', MANUAL: '수동 등록', ADMIN: '관리자 등록' },
  photos: (count) => `수업 사진 ${count}장`,
  photoAlt: (index) => `수업 사진 ${index}`,
  notes: '특이사항',
  startNote: '수업 시작',
  endNote: '수업 종료',
  none: '없음',
  locked: '하루 단위로 바뀌기 전 기록이라 볼 수만 있어요.',
}

// ⑪ 달력으로 날짜 고르기. 월요일부터 시작한다
export const historyCalendar = {
  title: (year, month) => `${year}년 ${month}월`,
  prevMonth: '이전 달',
  nextMonth: '다음 달',
  weekdays: ['월', '화', '수', '목', '금', '토', '일'],
  legendClass: '수업 있음',
  legendRejected: '반려된 기록 있음',
  legendToday: '오늘',
  loadError: '수업 표시를 불러오지 못했어요.',
  goToday: '오늘로',
  view: (date) => `${date.getMonth() + 1}월 ${date.getDate()}일 보기`,
  close: '닫기',
}
