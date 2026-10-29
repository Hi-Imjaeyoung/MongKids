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
