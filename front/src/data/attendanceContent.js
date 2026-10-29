// 근태 관리 수업 시작·종료 체크 화면(EMP-SCR-001) 문구

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
export const todayTitle = (count) => `오늘 수업 ${count}개`

// id는 백엔드 ClassStatus
export const classStatuses = {
  NOT_STARTED: { label: '시작 전', tone: 'plain' },
  STARTED: { label: '시작 확정', tone: 'green' },
  PENDING: { label: '승인 대기', tone: 'yellow' },
  REJECTED: { label: '반려', tone: 'red' },
  ENDED: { label: '종료 완료', tone: 'green' },
}

export const classCard = {
  next: '다음 수업',
  startByLocation: '현재 위치로 수업 시작',
  restart: '다시 수업 시작',
  end: '수업 종료',
  correct: '수업 기록 정정',
  started: (time) => `${time} 시작`,
  ended: (time) => `${time} 종료`,
  distance: (meters) => `기관까지 ${meters}m`,
  manual: (time) => `수동 등록 ${time}`,
  adminRegistered: (time) => `관리자 등록 ${time}`,
  photoCount: (count) => `사진 ${count}장`,
  pendingHint: '관리자가 확인하면 시작 확정으로 바뀌어요.',
  rejectReason: (reason) => `반려 사유: ${reason}`,
  pastDate: (month, day) => `(${month}월 ${day}일)`,
  locating: '위치를 확인하는 중이에요…',
}

// ② 위치 확인 실패. 반경 밖(outOfRange)과 위치를 못 받음(unavailable)·시간 초과(timeout)가 같은 화면을 쓴다
export const locationFail = {
  title: '수업 시작',
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
  title: '수업 시작',
  heading: '위치 정보를 켜 주세요',
  message: '수업 시작 확인에 현재 위치가 필요해요. 위치가 꺼져 있거나 브라우저에서 허용하지 않았어요.',
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

// ④ 수업 시작 수동 등록
export const manualStart = {
  title: '수업 시작 수동 등록',
  classLabel: '시작할 수업',
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

// ⑤ 수업 시작 확정
export const startConfirmed = {
  title: '수업 시작',
  heading: '수업 시작이 확인됐어요',
  classLabel: '수업',
  distanceLabel: '기관까지 거리',
  statusLabel: '상태',
  noteLabel: '특이사항',
  optional: '(선택)',
  notePlaceholder: '예: 정문 공사 중이라 후문으로 들어왔어요',
  noteMaxLength: 1000,
  save: '특이사항 저장',
  saving: '저장하는 중…',
  skip: '저장하지 않고 목록으로',
}

// ⑥ 수업 종료, ⑦ 수업 기록 정정
export const classEndForm = {
  endTitle: '수업 종료',
  editTitle: '수업 기록 정정',
  photoLabel: '수업 사진',
  photoRule: '1장 이상, 최대 20장 · 장당 10MB · JPG·PNG·WEBP',
  photoAdd: '사진 추가',
  photoRemove: (index) => `${index}번째 사진 빼기`,
  photoCountError: '사진은 최대 20장까지 올릴 수 있어요.',
  photoFileError: 'JPG·PNG·WEBP 형식의 10MB 이하 사진만 올릴 수 있어요.',
  photoRequired: '수업 사진을 1장 이상 올려 주세요.',
  noteLabel: '특이사항',
  notePlaceholder: '예: 한 아이가 넘어져 무릎을 살짝 긁혔어요. 담임 선생님께 전달했어요.',
  noteMaxLength: 2000,
  endNotice: '수업 종료는 위치 정보를 사용하지 않아요.',
  editNotice: '정정해도 수업 시작·종료 시각은 바뀌지 않아요. 수업 당일까지만 정정할 수 있어요.',
  lastCorrected: (time) => `마지막 정정: ${time ?? '없음'}`,
  submitEnd: '수업 종료 제출',
  submitEdit: '정정 내용 저장',
  submitting: '저장하는 중…',
}
