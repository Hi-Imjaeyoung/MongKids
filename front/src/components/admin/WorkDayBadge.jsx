import { calendarStatus } from './attendanceAdminFormat'
import './WorkDayBadge.css'

// 근무 캘린더 칸의 하루 상태 배지 (EMP-FR-017). short면 월간 달력용 짧은 이름
function WorkDayBadge({ day, today, short = false }) {
  const status = calendarStatus(day, today)
  return (
    <span className={`work-day-badge work-day-badge--${status.tone}`}>{short ? status.short : status.label}</span>
  )
}

export default WorkDayBadge
