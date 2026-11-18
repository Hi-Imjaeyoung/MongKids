// 근태 관리자 화면(EMP-SCR-006) 날짜 계산. 날짜는 서버와 같은 'YYYY-MM-DD' 문자열로 다룬다

// Date → '2026-10-30' (브라우저 현지 날짜)
export function toIsoDate(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export function parseIsoDate(isoDate) {
  const [year, month, day] = isoDate.split('-').map(Number)
  return new Date(year, month - 1, day)
}

export function addDays(isoDate, days) {
  const date = parseIsoDate(isoDate)
  date.setDate(date.getDate() + days)
  return toIsoDate(date)
}

// first~last(포함) 가운데 요일 번호(일=0)가 dayIndexes에 든 날짜. 서버의 반복 등록과 같은 규칙
export function repeatDates(first, last, dayIndexes) {
  if (!first || !last || first > last) return []
  const dates = []
  for (let date = first; date <= last && dates.length <= 400; date = addDays(date, 1)) {
    if (dayIndexes.includes(parseIsoDate(date).getDay())) dates.push(date)
  }
  return dates
}

// 하루 항목(AdminWorkDayResponse)의 키: 직원·날짜
export function dayKey(item) {
  return `${item.staffId}-${item.day.date}`
}

// 하루 시작·종료를 직접 등록할 수 있는지 (EMP-FR-020): 기록이 없거나 반려.
// 종료는 하루 시작이 확정·승인 대기여야 한다. 하루 단위 적용 전 날짜(legacyClasses)는 등록하지 않는다
export function canRegisterDay(day, type) {
  if (day.legacyClasses.length > 0) return false
  const check = day[type]
  const open = !check || check.status === 'REJECTED'
  if (type === 'end') return open && day.start != null && day.start.status !== 'REJECTED'
  return open
}

// 고치거나 지울 수 있는 수업: 수업 단위 시작·종료 기록이 없음.
// 하루 시작 기록이 있는 날(EMP-FR-020)과 시작 시도 로그는 서버가 다시 확인한다
export function isEditableSession(classSession) {
  return !classSession.start && !classSession.end
}

// "10:00:00" → "10:00"
export function toTimeInput(time) {
  return time?.slice(0, 5) ?? ''
}
