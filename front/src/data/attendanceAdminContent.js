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
  staffHint: '직원 권한을 받은 회원만 보여요',
  target: '대상 (반)',
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

// 기관 위치 (EMP-FR-012)
export const institutionAdmin = {
  title: '기관 위치',
  description: '기관명이나 주소로 기관을 찾고 지도에서 출입구 쪽으로 핀을 맞춰 저장해요. 이 좌표에서 100m 안이면 수업 시작이 자동 확정돼요.',
  add: '새 기관 등록',
  listLabel: '기관 목록',
  count: (count) => `기관 ${count}곳`,
  empty: '등록된 기관이 없어요. 새 기관을 등록해 주세요.',
  sessionCount: (count) => `수업 ${count}회`,
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
  deleteLocked: '할당된 수업이 있어 지울 수 없어요',
  deleteConfirm: (name) => `${name}을(를) 지울까요?`,
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
  institution: '기관',
  allInstitutions: '전체 기관',
  periodHint: '한 번에 최대 31일까지 볼 수 있어요.',
}
