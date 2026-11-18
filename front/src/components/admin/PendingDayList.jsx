import { classStartAdmin, dayRecordAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatClock, formatRoute } from '../attendance/attendanceFormat'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import { dayKey } from './attendanceAdminFormat'
import StartLocation from './StartLocation'
import './PendingStartList.css'

// 승인 대기 중인 하루 시작·종료 (EMP-FR-020)
function pendingChecks(day) {
  return ['start', 'end'].filter((type) => day[type]?.status === 'PENDING')
}

// 하루 시작·종료 승인 대기 목록. items는 AdminWorkDayResponse[], 고르면 onSelect(dayKey)
function PendingDayList({ items, selectedKey, onSelect }) {
  return (
    <ul className="pending-start-list" aria-label={classStartAdmin.listLabel}>
      {items.map((item) => {
        const { day } = item
        const key = dayKey(item)
        const selected = key === selectedKey
        const pending = pendingChecks(day)
        return (
          <li key={key}>
            <button
              type="button"
              className={`pending-start-list__card ${selected ? 'pending-start-list__card--selected' : ''}`}
              aria-pressed={selected}
              onClick={() => onSelect(key)}
            >
              <span className="pending-start-list__head">
                <strong className="pending-start-list__title">
                  {classStartAdmin.dayCard(item.staffName, shortDateLabel(day.date))}
                </strong>
                <ClassStatusBadge status={day.status} day />
              </span>
              <span className="pending-start-list__meta">
                <span>{dayRecordAdmin.route(formatRoute(day), day.classes.length)}</span>
                <span>{classStartAdmin.pendingOf(pending.map((type) => classStartAdmin.pendingParts[type]))}</span>
              </span>
              {pending.map((type) => (
                <span key={type} className="pending-start-list__meta">
                  <span>
                    {classStartAdmin.pendingParts[type]} {classStartAdmin.registeredAt(formatClock(day[type].checkedAt))}
                  </span>
                  <StartLocation start={day[type]} />
                </span>
              ))}
            </button>
          </li>
        )
      })}
    </ul>
  )
}

export default PendingDayList
