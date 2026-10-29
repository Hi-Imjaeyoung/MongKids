import { classStatuses } from '../../data/attendanceContent'
import './ClassStatusBadge.css'

// 수업 상태 배지 (EMP-FR-013). status는 백엔드 ClassStatus
function ClassStatusBadge({ status }) {
  const { label, tone } = classStatuses[status]
  return <span className={`class-status class-status--${tone}`}>{label}</span>
}

export default ClassStatusBadge
