import { checkLine } from '../../data/attendanceContent'

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

function formatByMethod(method, time, located) {
  if (method === 'MANUAL') return checkLine.manual(time)
  if (method === 'ADMIN') return checkLine.adminRegistered(time)
  return located(time)
}

// 하루 단위 적용 전 수업 시작 기록: 위치 확인 "09:52 시작", 수동 등록 "수동 등록 11:24", 관리자 등록 "관리자 등록 11:24"
export function formatStart(start) {
  return formatByMethod(start.method, formatClock(start.startedAt), checkLine.started)
}

// 하루 시작·종료 기록(EMP-FR-020): "09:52 시작" / "15:47 종료", 수동·관리자 등록은 방식과 시각
export function formatCheck(check, kind) {
  return formatByMethod(check.method, formatClock(check.checkedAt), kind === 'end' ? checkLine.ended : checkLine.started)
}

// 그날 수업을 연달아 같은 기관끼리 묶는다 (수업 순서 유지). 첫 묶음이 시작 위치, 마지막 묶음이 종료 위치다
export function groupByInstitution(classes) {
  const groups = []
  classes.forEach((dayClass) => {
    const last = groups[groups.length - 1]
    if (last && last.institutionId === dayClass.institutionId) {
      last.classes.push(dayClass)
    } else {
      groups.push({
        institutionId: dayClass.institutionId,
        institutionName: dayClass.institutionName,
        institutionAddress: dayClass.institutionAddress,
        classes: [dayClass],
      })
    }
  })
  return groups
}

// 그날 수업한 기관 수 (같은 기관에 두 번 가도 한 곳)
export function countInstitutions(classes) {
  return new Set(classes.map((dayClass) => dayClass.institutionId)).size
}

// 첫·마지막 수업 기관 이름
export function firstInstitution(day) {
  return day.classes[0]?.institutionName ?? ''
}

export function lastInstitution(day) {
  return day.classes[day.classes.length - 1]?.institutionName ?? ''
}

// "해님어린이집 → 별빛유치원" (기관이 하나면 그 이름만)
export function formatRoute(day) {
  const first = firstInstitution(day)
  const last = lastInstitution(day)
  return first === last ? first : `${first} → ${last}`
}

// 반려된 기록이 있는 하루인지 (하루 단위 적용 전 수업 단위 기록 포함)
export function hasRejected(day) {
  return (
    day.status === 'REJECTED' ||
    day.status === 'END_REJECTED' ||
    day.legacyClasses.some((classSession) => classSession.status === 'REJECTED')
  )
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
export function summarizeByDate(days) {
  const summary = new Map()
  days?.forEach((day) => {
    summary.set(day.date, { count: day.classes.length, rejected: hasRejected(day) })
  })
  return summary
}
