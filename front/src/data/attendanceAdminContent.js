// 근태 관리자 화면(EMP-SCR-006) 문구. 관리자 화면(SCR-014) /admin의 "근태 관리" 메뉴

// 승인 대기 건수 배지. 화면에는 숫자만 보이고 화면 읽기 프로그램에는 이 문구를 읽어 준다
export const pendingStartCountLabel = (count) => `승인 대기 ${count}건`

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

// '2026-10-30' → '10/30(금)'
export const shortDateLabel = (isoDate) => {
  const [year, month, day] = isoDate.split('-').map(Number)
  return `${month}/${day}(${WEEKDAYS[new Date(year, month - 1, day).getDay()]})`
}

// '2026-10-30' → '10월 30일(금)'
export const longDateLabel = (isoDate) => {
  const [year, month, day] = isoDate.split('-').map(Number)
  return `${month}월 ${day}일(${WEEKDAYS[new Date(year, month - 1, day).getDay()]})`
}

// 수업 유형 (EMP-FR-004). 행사 장소는 기관과 같이 등록한다
export const classTypes = [
  { id: 'REGULAR', label: '정기 수업' },
  { id: 'EVENT', label: '행사·특별활동' },
]
export const eventTag = '행사'

// 백엔드 DayOfWeek 순서(월~일)
export const daysOfWeek = [
  { id: 'MONDAY', label: '월', index: 1 },
  { id: 'TUESDAY', label: '화', index: 2 },
  { id: 'WEDNESDAY', label: '수', index: 3 },
  { id: 'THURSDAY', label: '목', index: 4 },
  { id: 'FRIDAY', label: '금', index: 5 },
  { id: 'SATURDAY', label: '토', index: 6 },
  { id: 'SUNDAY', label: '일', index: 0 },
]

// 거리·정확도 표시 (EMP-FR-011)
export const locationText = {
  distance: (meters) => `기관까지 ${meters}m`,
  radius: (meters) => `기준 ${meters}m`,
  accuracy: (meters) => `정확도 ±${Math.round(meters)}m`,
  none: '위치 없음',
  noneDetail: '위치를 받지 못하고 등록',
}

// 수업 시작 기준 반경 (ADR-006). 서버 RADIUS_METERS와 같다
export const RADIUS_METERS = 100

export const startMethods = {
  LOCATION: '위치 확인 자동 확정',
  MANUAL: '직원 수동 등록',
  ADMIN: '관리자 직접 등록',
}

// 수업 시작·종료 승인 (EMP-FR-020). 하루 단위 적용 전 수업 단위 승인 대기(EMP-FR-011)도 함께 보여 준다
export const classStartAdmin = {
  title: '수업 시작·종료 승인',
  description: '위치로 확인하지 못해 직원이 수동 등록한 하루 시작·종료예요. 거리·정확도·사유를 보고 승인하거나 반려해 주세요.',
  count: (count) => `승인 대기 ${count}건 · 오래된 순`,
  empty: '승인을 기다리는 수업 시작·종료가 없어요.',
  listLabel: '승인 대기 목록',
  legacyTitle: '하루 단위로 바뀌기 전 수업 시작 승인 대기',
  dayCard: (staffName, date) => `${staffName} · ${date}`,
  pendingParts: { start: '시작', end: '종료' },
  pendingOf: (parts) => `${parts.join('·')} 승인 대기`,
  close: '닫기',
  registeredAt: (time) => `등록 ${time}`,
  fields: { institution: '기관', classTime: '수업', registeredAt: '등록 시각', distance: '기관까지', accuracy: '정확도' },
  reason: '직원이 적은 사유',
  approve: '승인',
  rejectReason: '반려 사유 (직원 화면에 보여요)',
  rejectPlaceholder: '예: 정문 앞에서 다시 수업 시작을 눌러 주세요.',
  rejectHint: '반려하면 직원이 같은 수업에서 수업 시작을 다시 확인할 수 있어요. 최대 200자',
  reject: '반려',
  rejectRequired: '반려 사유를 적어 주세요.',
}

// 하루 기록 표 (EMP-FR-003, 007, 020)
export const dayRecordAdmin = {
  title: '수업 기록',
  description:
    '직원별 하루 시작·종료 기록과 하루 사진·기관별 특이사항을 확인해요. 기록이 없거나 반려된 하루는 관리자가 직접 등록할 수 있어요.',
  statusLabel: '상태',
  allStatuses: '전체',
  listLabel: '하루 기록 목록',
  columns: { date: '날짜', staff: '직원', route: '기관', status: '상태', start: '시작', end: '종료' },
  route: (route, count) => `${route} · 수업 ${count}개`,
  legacy: '이전 방식 기록',
  empty: '조건에 맞는 하루 기록이 없어요.',
  close: '닫기',
  detailLabel: '선택한 하루 기록',
}

// 하루 근무 상세 ④ (EMP-SCR-006, EMP-FR-020). 근무 시간은 계산하지 않는다(EMP-FR-017)
export const dayDetailAdmin = {
  label: '하루 근무 상세',
  kicker: '근무 상세',
  title: (staffName, date) => `${staffName} · ${date}`,
  summary: (classCount, institutionCount) => `수업 ${classCount}개 · 기관 ${institutionCount}곳`,
  sections: { start: '수업 시작', end: '수업 종료' },
  classesTitle: (count) => `수업 ${count}개`,
  fields: { time: '시각', institution: '확인 기관', location: '위치', reason: '사유', rejectReason: '반려 사유', processedAt: '처리' },
  first: '(첫 기관)',
  last: '(마지막 기관)',
  none: '아직 기록이 없어요.',
  methodStatus: (method, status) => `${method} · ${status}`,
  checkStatuses: { CONFIRMED: '확정', PENDING: '승인 대기', REJECTED: '반려' },
  approve: { start: '시작 승인', end: '종료 승인' },
  reject: '반려',
  rejectLabel: (section) => `${section} 반려 사유 (직원 화면에 보여요)`,
  rejectPlaceholder: '예: 마지막 기관 정문 앞에서 다시 수업 종료를 해 주세요.',
  rejectHint: '반려하면 직원이 그날 다시 확인할 수 있어요. 지난 날짜는 관리자가 직접 등록해 주세요. 최대 200자',
  rejectRequired: '반려 사유를 적어 주세요.',
  register: { start: '시작 직접 등록', end: '종료 직접 등록' },
  endNeedsStart: '수업 시작이 확정되거나 승인 대기여야 종료를 등록할 수 있어요.',
  photos: (count) => `수업 사진 ${count}장`,
  photosSub: '하루 묶음',
  photoAlt: (index) => `수업 사진 ${index}`,
  notes: '기관별 특이사항',
  corrected: (dateTime) => `마지막 정정 ${dateTime}`,
  noNote: '-',
  legacyTitle: '하루 단위로 바뀌기 전 수업별 기록',
}

// 하루 단위 적용 전 수업 기록 상세 (EMP-FR-003, 007). 하루 근무 상세 안에서 읽기 전용으로 보여 준다
export const classRecordAdmin = {
  detailLabel: '하루 단위 적용 전 수업 기록',
  fields: {
    institution: '기관',
    classTime: '수업',
    start: '수업 시작',
    startNote: '시작 메모',
    end: '수업 종료',
    reason: '사유',
    rejectReason: '반려 사유',
  },
  corrected: (dateTime) => `정정 ${dateTime}`,
  photos: (count) => `수업 사진 ${count}장`,
  photoAlt: (index) => `수업 사진 ${index}`,
  note: '특이사항',
  noNote: '-',
  noEnd: '-',
}

// 관리자 직접 등록 대화상자 (EMP-FR-020). 하루 시작·종료 공용
export const directStartDialog = {
  titles: { start: '수업 시작 직접 등록', end: '수업 종료 직접 등록' },
  day: '날짜',
  staff: '직원',
  classTime: '수업',
  reason: '등록 사유',
  placeholder: '예: 직원 휴대폰 고장으로 전화로 도착 확인',
  hint: '관리자가 등록하면 바로 확정돼요. 위치·거리 없이 저장되고 사유와 처리한 관리자가 기록에 남아요. 종료 등록은 사진·특이사항 없이 저장돼요. 최대 200자',
  cancel: '취소',
  close: '닫기',
  submit: '등록',
  required: '등록 사유를 적어 주세요.',
}

// 수업 할당 안 "미정 수업 배정" 보기: 정기 수업 묶음 담당 직원 추천 (EMP-SCR-007, EMP-FR-022)
export const seriesAdmin = {
  viewsLabel: '수업 할당 보기',
  views: { sessions: '전체 수업', series: '미정 수업 배정' },
  description:
    '담당 직원이 정해지지 않은 정기 수업 묶음이에요. 묶음을 고르면 오늘 이후 수업을 맡을 수 있는 직원을 순서대로 추천해요.',
  listLabel: '직원 미정 정기 수업 목록',
  columns: { series: '수업 묶음', schedule: '요일 · 시간', period: '기간', count: '오늘 이후 미정' },
  count: (count) => `${count}회`,
  pick: (label) => `${label} 추천 보기`,
  empty: '오늘 이후 직원 미정 수업이 남은 정기 수업이 없어요.',
  singleHint:
    '한 회만 다른 직원에게 맡기거나 지난 미정 수업을 배정하려면 "전체 수업"이나 근무 캘린더에서 그 수업을 고쳐요. 정기 수업 기능이 생기기 전에 등록한 수업은 여기에 나오지 않아요.',
  choose: '목록에서 수업 묶음을 고르면 추천 직원을 보여 줘요.',
  loading: '추천 직원을 계산하고 있어요. 처음 쓰는 기관 사이 이동 시간은 길찾기로 불러와서 조금 걸릴 수 있어요.',
  panelLabel: (title) => `${title} 담당 직원 추천`,
  facts: { schedule: '요일·시간', period: '기간', target: '배정 대상' },
  target: (count, first, last) => `오늘 이후 미정 수업 ${count}회 (${first} ~ ${last})`,
  ruleTitle: '추천 순서',
  rule: '남은 기간 주 평균 수업이 적은 순 → 같으면 회차 평균 추가 이동 시간이 짧은 순. 모든 회차에서 겹치지 않고, 앞뒤 수업에서 차로 이동해 수업 시작 3분 전까지 도착할 수 있는 직원만 추천해요.',
  candidatesLegend: (count) => `추천 직원 ${count}명`,
  noCandidates: '모든 회차를 맡을 수 있는 직원이 없어요. 아래 사유를 보고 수업 시간을 바꾸거나 한 회씩 배정해 주세요.',
  weekly: (average) => `주 평균 ${average}회`,
  extraAverage: (minutes) => `추가 이동 평균 ${minutes}분`,
  extraMax: (minutes) => `최대 ${minutes}분`,
  position: { BEFORE: '앞', AFTER: '뒤' },
  noNeighbor: (position) => `${position}: 없음`,
  neighbor: (position, item, timeRange, arrival, partial) =>
    `${position}: ${item.institutionName} ${timeRange} · 차 ${item.travelMinutes}분, ${arrival} 도착${
      partial ? ` (${item.count}회)` : ''
    }`,
  unavailableTitle: (count) => `배정할 수 없는 직원 ${count}명`,
  reasons: {
    OVERLAP: (reason) => `${reason.count}회 시간 겹침`,
    TRAVEL_SHORT: (reason) => `${reason.count}회 이동 시간 부족`,
    DAY_LOCKED: () => '오늘 하루 시작 기록 있음',
  },
  reasonDetail: {
    OVERLAP: (reason, timeRange) => `${reason.institutionName} ${timeRange} 수업이 있어요`,
    TRAVEL_SHORT: (reason) =>
      `${reason.institutionName} 수업과 사이 차 ${reason.requiredMinutes}분 필요, 여유 ${reason.availableMinutes}분 (수업 3분 전 도착 기준)`,
    DAY_LOCKED: () => '오늘 수업은 바꿀 수 없어요',
  },
  dates: (labels, rest) => `${labels.join(', ')}${rest > 0 ? ` 외 ${rest}회` : ''}`,
  submit: (name, count) => `${name}님에게 ${count}회 배정`,
  assigned: (count, name) => `수업 ${count}회를 ${name}님에게 배정했어요.`,
  travelNote: '이동 시간은 기관 사이 자동차 길찾기(추천 경로) 기준이에요. 직원 집에서 오가는 시간은 계산하지 않아요.',
}

// 수업 할당 (EMP-FR-004)
export const classSessionAdmin = {
  title: '수업 할당',
  description: '기관 수업을 직원에게 할당해요. 기간과 요일을 고르면 날짜별 수업이 한꺼번에 만들어지고, 만든 뒤에는 수업 하나씩 고치거나 지울 수 있어요.',
  listLabel: '할당된 수업 목록',
  columns: { date: '날짜', time: '시간', institution: '기관 · 대상', staff: '직원', status: '상태', actions: '관리' },
  empty: '이 기간에 할당된 수업이 없어요.',
  lockedHint: '수업 시작·종료 기록이 있는 날의 수업은 고치거나 지울 수 없어요.',
  edit: '수정',
  delete: '삭제',
  lockedTitle: '수업 시작·종료 기록이 있어 고치거나 지울 수 없어요',
  deleteConfirm: (label) => `${label} 수업을 지울까요?`,
  add: '수업 등록',
  close: '닫기',
  createTitle: '수업 등록',
  editTitle: '수업 수정',
  formLabel: '수업 할당',
  institution: '기관',
  staff: '담당 직원',
  staffHint: '비워 두면 직원 미정으로 등록되고, 나중에 수정이나 일괄 배정으로 정해요. 직원 권한을 받은 회원만 보여요',
  staffNone: '나중에 배정 (미정)',
  // 직원 미정 수업 (EMP-FR-021)
  unassigned: '미정',
  unassignedPast: '미정 · 지난 수업',
  selectColumn: '고르기',
  selectRow: (label) => `${label} 고르기`,
  selectAll: '이 목록의 미정 수업 모두 고르기',
  assignTitle: (count) => `고른 미정 수업 ${count}회`,
  assignStaff: '배정할 직원',
  assignSubmit: '직원 배정',
  assignClear: '고르기 해제',
  assignHint: '미정 수업만 고를 수 있어요. 고른 수업이나 그 직원의 수업과 시간이 하나라도 겹치면 아무것도 배정하지 않아요.',
  assigned: (count, name) => `수업 ${count}회를 ${name}님에게 배정했어요.`,
  target: '대상 (반)',
  classType: '수업 유형',
  classTypeHint: '행사·특별활동 장소도 기관 메뉴에서 기관처럼 등록해 고르면 돼요',
  period: '기간',
  firstDate: '첫날',
  lastDate: '마지막 날',
  classDate: '수업 날짜',
  days: '요일',
  time: '수업 시간',
  startTime: '시작 시각',
  endTime: '끝 시각',
  summary: (first, last, days, count) => `${first} ~ ${last} · ${days} · 수업 ${count}회가 만들어져요`,
  noDates: '기간 안에 고른 요일이 없어요.',
  submitCreate: (count) => `수업 ${count}회 등록`,
  submitEdit: '저장',
  cancelEdit: '취소',
  created: (count) => `수업 ${count}회를 등록했어요.`,
  updated: '수업을 수정했어요.',
  noInstitution: '먼저 기관 위치 메뉴에서 기관을 등록해 주세요.',
  noStaff: '직원 권한을 받은 회원이 없어요. 회원 · 직원 권한 메뉴에서 먼저 지정해 주세요.',
  choose: '선택해 주세요',
  invalidTime: '끝 시각이 시작 시각보다 늦어야 해요.',
}

// 기관 (EMP-FR-012, EMP-SCR-006 11-18 초안 "기관 (표 형태 목록)")
export const institutionAdmin = {
  title: '기관',
  description: '수업하는 기관을 한눈에 보고 관리해요. 기관을 등록한 뒤 "수업 추가"로 이 기관 수업을 넣을 수 있어요.',
  add: '새 기관 등록',
  listLabel: '기관 목록',
  count: (count) => `기관 ${count}곳`,
  empty: '등록된 기관이 없어요. 새 기관을 등록해 주세요.',
  searchLabel: '기관 찾기',
  searchPlaceholder: '기관 이름이나 주소',
  noMatch: '찾는 기관이 없어요.',
  columns: { name: '기관', schedule: '수업 요일·시간', next: '다음 수업', upcoming: '남은 수업', actions: '관리' },
  noSchedule: '-',
  upcomingCount: (count) => `${count}회`,
  totalCount: (count) => `전체 ${count}회`,
  unassignedCount: (count) => `미배정 ${count}회`,
  footnotes: [
    '수업 요일·시간: 앞으로 할당된 수업 기준',
    '남은 수업: 아직 끝나지 않은 수업 수 (오늘 남은 수업 포함)',
  ],
  addClass: '수업 추가',
  addClassTitle: (name) => `${name} · 수업 추가`,
  edit: '수정',
  createTitle: '새 기관 등록',
  editTitle: (name) => `${name} 수정`,
  assigned: (count) => `수업 ${count}회 할당됨`,
  placeSearch: '기관 찾기',
  placePlaceholder: '기관명이나 주소 (예: 한국유치원 수지)',
  placeSearchButton: '찾기',
  placeResults: (count) => `검색 결과 ${count}곳`,
  placeHint: '고르면 주소와 위치가 채워져요. 기관 이름이 비어 있으면 이름도 채워요.',
  placeNotFound: '찾지 못했어요. 지역명을 붙여 다시 찾거나, 아래 주소 칸에 도로명 주소를 입력해 찾아 주세요.',
  placeSelect: (name, address) => `${name}, ${address} 고르기`,
  name: '기관 이름',
  address: '주소',
  search: '주소로 찾기',
  searching: '찾는 중…',
  notFound: '주소를 찾지 못했어요. 도로명 주소나 지번 주소로 다시 입력해 주세요.',
  latitude: '위도',
  longitude: '경도',
  mapLabel: '기관 위치 지도. 핀 둘레의 원이 수업 시작 기준 반경 100m예요',
  mapTip: '핀을 끌어 기관 출입구 쪽으로 옮겨 주세요',
  mapEmpty: '기관명이나 주소로 찾으면 지도에 위치가 표시돼요.',
  mapLoading: '지도를 불러오는 중이에요.',
  mapUnavailable: '지도를 불러오지 못했어요. 위도·경도를 직접 입력할 수 있어요.',
  mapNoKey: '지도 키(VITE_KAKAO_MAP_KEY)가 설정되지 않았어요. 위도·경도를 직접 입력할 수 있어요.',
  warning: '좌표가 틀리면 이 기관의 수업 시작이 모두 반경 밖으로 거절돼요. 저장 전에 지도의 핀 위치를 꼭 확인해 주세요.',
  editInfo: '좌표를 바꾸면 앞으로의 수업 시작부터 새 좌표로 판정해요. 이미 저장된 수업 시작 기록(거리)은 바뀌지 않아요.',
  needLocation: '기관명·주소로 찾거나 위도·경도를 입력해 위치를 정해 주세요.',
  delete: '삭제',
  // 기관 삭제 확인 (EMP-FR-012, 11-18 초안 "기관 삭제 확인". 기록도 모두 지우는 결정(2026-11-02·11-24)대로)
  deleteDialog: {
    title: (name) => `${name}을(를) 삭제할까요?`,
    lead: '기관 목록과 수업 할당에서 사라지고, 이 기관의 수업과 수업 기록도 모두 함께 지워져요.',
    loading: '함께 지워질 기록을 세는 중이에요.',
    sessions: '수업 (지난 수업·앞으로 예정 모두)',
    sessionCount: (count) => `${count}회`,
    workDays: '하루 기록 (이 기관이 그날 첫·마지막 수업인 날)',
    workDayCount: (count) => `${count}일`,
    workDayHint: '그날 다른 기관의 사진·특이사항까지 하루 기록이 통째로 지워져요.',
    notes: '다른 날 하루 기록의 이 기관 특이사항',
    noteCount: (count) => `${count}건`,
    images: '수업 사진',
    imageCount: (count) => `${count}장`,
    legacy: '이전 수업 기록 (하루 단위 적용 전 수업 시작·종료)',
    legacyCount: (count) => `${count}건`,
    warning: '지운 기관과 기록은 되돌릴 수 없어요.',
    agree: '함께 지워지는 기록을 확인했고, 삭제에 동의해요',
    back: '돌아가기',
    cancel: '취소',
    submit: (count) => (count > 0 ? `수업 ${count}회와 함께 삭제` : '기관 삭제'),
    deleting: '지우는 중…',
  },
  cancel: '취소',
  save: '저장',
  saved: '저장했어요.',
  close: '닫기',
}

// 기간·직원·기관 필터
export const classFilters = {
  from: '시작일',
  to: '종료일',
  staff: '직원',
  allStaff: '전체 직원',
  unassigned: '직원 미정',
  institution: '기관',
  allInstitutions: '전체 기관',
  periodHint: '한 번에 최대 31일까지 볼 수 있어요.',
}

// 근무 캘린더 주간·월간 (EMP-SCR-006, EMP-FR-017). 근무 시간은 계산하지 않고 수업 수만 보여 준다. 주말 포함 항상 7일
export const workCalendarAdmin = {
  title: '근무 캘린더',
  descriptions: {
    week: '직원별 하루 수업 수, 시작·종료 시각, 다녀온 기관을 한눈에 봐요. 칸을 누르면 그날 시작·종료 기록과 사진·특이사항을 볼 수 있어요.',
    month: '한 달 동안 직원별 수업 수와 시작·종료 상태를 봐요. 날짜를 누르면 그 주 주간 보기로 이동해요.',
  },
  viewLabel: '보기',
  views: [
    { id: 'week', label: '주' },
    { id: 'month', label: '월' },
  ],
  prev: { week: '지난주', month: '지난달' },
  next: { week: '다음 주', month: '다음 달' },
  current: { week: '이번 주', month: '이번 달' },
  weekRange: (from, to) => {
    const [, fromMonth, fromDay] = from.split('-').map(Number)
    const [, toMonth, toDay] = to.split('-').map(Number)
    return fromMonth === toMonth
      ? `${fromMonth}월 ${fromDay}일 – ${toDay}일`
      : `${fromMonth}월 ${fromDay}일 – ${toMonth}월 ${toDay}일`
  },
  monthLabel: (isoDate) => {
    const [year, month] = isoDate.split('-').map(Number)
    return `${year}년 ${month}월`
  },
  weekdays: ['월', '화', '수', '목', '금', '토', '일'],
  legendLabel: '상태 색',
  legend: [
    { label: '예정 · 시작 전', tone: 'gray' },
    { label: '수업 중', tone: 'blue' },
    { label: '승인 대기', tone: 'yellow' },
    { label: '반려', tone: 'red' },
    { label: '수업 종료', tone: 'green' },
    { label: '행사 · 특별활동 포함', tone: 'purple' },
  ],
  // 칸 배지. 월간은 칸이 좁아 짧은 이름을 쓴다. 앞으로 날짜의 시작 전은 `예정`
  statuses: {
    NOT_STARTED: { label: '시작 전', short: '시작 전', tone: 'gray' },
    STARTED: { label: '수업 중', short: '수업 중', tone: 'blue' },
    PENDING: { label: '시작 승인 대기', short: '시작 대기', tone: 'yellow' },
    REJECTED: { label: '시작 반려', short: '시작 반려', tone: 'red' },
    END_PENDING: { label: '종료 승인 대기', short: '종료 대기', tone: 'yellow' },
    END_REJECTED: { label: '종료 반려', short: '종료 반려', tone: 'red' },
    ENDED: { label: '수업 종료', short: '수업 종료', tone: 'green' },
  },
  scheduled: { label: '예정', short: '예정', tone: 'gray' },
  legacy: { label: '이전 방식 기록', short: '이전 방식', tone: 'gray' },
  weekListLabel: '직원별 주간 근무 표',
  staffColumn: '직원',
  dayColumn: (dateLabel, today) => (today ? `${dateLabel} 오늘` : dateLabel),
  weekTotalColumn: '이번 주',
  classCount: (count) => `수업 ${count}개`,
  noClass: '수업 없음',
  // 직원 미정 수업 줄 (EMP-FR-021)
  unassignedRow: '미정',
  unassignedCount: (count) => `미정 ${count}개`,
  unassignedSum: (count) => `미정 ${count}회`,
  unassignedSub: '직원 배정 필요',
  unassignedCellLabel: (dateLabel) => `${dateLabel} 직원 미정 수업 보기`,
  unassignedTitle: (dateLabel) => `${dateLabel} 직원 미정 수업`,
  unassignedLead: '직원을 고르고 배정하면 그 직원의 하루 수업에 들어가요.',
  unassignedEmpty: '이 날 직원 미정 수업이 없어요.',
  unassignedAssign: '배정',
  firstClass: (time) => `${time} 첫 수업`,
  // 실제 하루 시작 – 하루 종료. 수동·관리자 등록이면 표시
  timeRange: (start, end, mark) => `${start} – ${end}${mark ? ` (${mark})` : ''}`,
  methodMarks: { MANUAL: '수동', ADMIN: '관리자' },
  weekSum: (completed, assigned) => `${completed} / ${assigned} 수업`,
  weekSumSub: '수업 종료 기준',
  checks: (pending, rejected) => (rejected > 0 ? `승인 대기 ${pending}건 · 반려 ${rejected}건` : `승인 대기 ${pending}건`),
  cellLabel: (staffName, dateLabel) => `${staffName} ${dateLabel} 하루 근무 상세 보기`,
  monthListLabel: '월간 근무 달력',
  monthDayLabel: (dateLabel) => `${dateLabel} 주간 보기`,
  today: '오늘',
  monthCount: (count) => `${count}개`,
  totalsTitle: (isoDate) => `${Number(isoDate.slice(5, 7))}월 직원별 합계`,
  totalsLabel: '직원별 합계',
  totalsColumns: {
    staff: '직원',
    assigned: '할당 수업',
    regular: '정기',
    events: '행사·특별활동',
    completed: '수업 종료',
    pending: '승인 대기',
    rejected: '반려',
  },
  eventCount: (count) => `행사 ${count}`,
  classes: (count) => `${count}개`,
  cases: (count) => `${count}건`,
  footnotes: {
    week: [
      '수업: 그날 할당된 수업 수',
      '시각: 실제 하루 시작 – 하루 종료 (예정 날짜는 첫 수업 시각)',
      '이번 주: 수업 종료가 확정된 수업 수 / 이번 주 할당 수업 수',
    ],
    month: [
      '할당 수업: 이달 전체 (앞으로 예정 포함), 정기 / 행사·특별활동으로 나눠 표시',
      '수업 종료: 하루 종료가 확정된 날의 수업 수',
      '승인 대기 · 반려: 하루 시작·종료 건수',
    ],
  },
  empty: '이 기간에 할당된 수업이 없어요.',
  detailLabel: '하루 근무 상세',
  close: '닫기',
}
