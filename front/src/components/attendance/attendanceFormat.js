import { classCard } from '../../data/attendanceContent'

// 근태 화면(EMP-SCR-001) 표시용 변환. 서버 시각은 Asia/Seoul 기준 ISO 문자열(2026-10-29T09:52:13)로 온다

// 수업 체크를 쓸 수 있는 직원인지 (STAFF·ADMIN). 서버도 DB의 현재 역할로 다시 검사한다
export function canUseAttendance(member) {
  return member?.role === 'STAFF' || member?.role === 'ADMIN'
}

// "10:00:00" → "10:00"
export function formatTime(time) {
  return time?.slice(0, 5) ?? ''
}

// "2026-10-29T09:52:13" → "09:52"
export function formatClock(dateTime) {
  return dateTime?.slice(11, 16) ?? ''
}

export function formatTimeRange(classSession) {
  return `${formatTime(classSession.startTime)} – ${formatTime(classSession.endTime)}`
}

// 수업 시간 · 대상(반)
export function formatClassMeta(classSession) {
  return [formatTimeRange(classSession), classSession.target].filter(Boolean).join(' · ')
}

// 오늘이 아닌 수업(반려된 채 남은 지난 수업)이면 [월, 일]
export function pastDateOf(classSession, today) {
  const [year, month, day] = classSession.classDate.split('-').map(Number)
  const isToday = year === today.getFullYear() && month === today.getMonth() + 1 && day === today.getDate()
  return isToday ? null : [month, day]
}

// 시작 방식에 따른 시작 시각: 위치 확인 "09:52 시작", 수동 등록 "수동 등록 11:24", 관리자 등록 "관리자 등록 11:24"
export function formatStart(start) {
  const time = formatClock(start.startedAt)
  if (start.method === 'MANUAL') return classCard.manual(time)
  if (start.method === 'ADMIN') return classCard.adminRegistered(time)
  return classCard.started(time)
}

// 날짜별 수업 기록(EMP-FR-015)의 날짜 계산. 날짜는 브라우저 시간대의 자정 Date와 'YYYY-MM-DD' 키를 오간다

export function toDateKey(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export function fromDateKey(key) {
  const [year, month, day] = key.split('-').map(Number)
  return new Date(year, month - 1, day)
}

export function addDays(date, days) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days)
}

// 그 주의 월요일
export function weekStartOf(date) {
  return addDays(date, -((date.getDay() + 6) % 7))
}

// 날짜 키별 수업 수와 반려 여부. 날짜 띠·달력의 표시에 쓴다
export function summarizeByDate(classes) {
  const summary = new Map()
  classes?.forEach((classSession) => {
    const current = summary.get(classSession.classDate) ?? { count: 0, rejected: false }
    summary.set(classSession.classDate, {
      count: current.count + 1,
      rejected: current.rejected || classSession.status === 'REJECTED',
    })
  })
  return summary
}
