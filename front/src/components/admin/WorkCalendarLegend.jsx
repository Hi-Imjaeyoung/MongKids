import { workCalendarAdmin } from '../../data/attendanceAdminContent'
import './WorkDayBadge.css'
import './WorkCalendarLegend.css'

// 근무 캘린더 상태 색 범례
function WorkCalendarLegend() {
  return (
    <ul className="work-calendar-legend" aria-label={workCalendarAdmin.legendLabel}>
      {workCalendarAdmin.legend.map((item) => (
        <li key={item.label} className={`work-day-badge work-day-badge--${item.tone}`}>
          {item.label}
        </li>
      ))}
    </ul>
  )
}

export default WorkCalendarLegend
