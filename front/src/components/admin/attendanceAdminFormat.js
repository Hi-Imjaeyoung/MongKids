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

// 직접 등록할 수 있는 수업: 시작 기록이 없거나 반려 (EMP-FR-011)
export function canRegisterStart(classSession) {
  return !classSession.start || classSession.start.status === 'REJECTED'
}

// 고치거나 지울 수 있는 수업: 시작·종료 기록이 없음 (시작 시도 로그는 서버가 다시 확인한다)
export function isEditableSession(classSession) {
  return !classSession.start && !classSession.end
}

// "10:00:00" → "10:00"
export function toTimeInput(time) {
  return time?.slice(0, 5) ?? ''
}
