import { classStatuses, dayStatuses } from '../../data/attendanceContent'
import './ClassStatusBadge.css'

// 상태 배지. day면 하루 상태(백엔드 DayStatus, EMP-FR-020), 아니면 하루 단위 적용 전 수업 상태(ClassStatus)
function ClassStatusBadge({ status, day = false }) {
  const { label, tone } = (day ? dayStatuses : classStatuses)[status]
  return <span className={`class-status class-status--${tone}`}>{label}</span>
}

export default ClassStatusBadge
