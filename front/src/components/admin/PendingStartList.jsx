import { classStartAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatClock, formatTime } from '../attendance/attendanceFormat'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import StartLocation from './StartLocation'
import './PendingStartList.css'

// 수업 시작 승인 대기 목록 (EMP-FR-011). items는 AdminClassSessionResponse[], 고르면 onSelect(classSessionId)
function PendingStartList({ items, selectedId, onSelect }) {
  return (
    <ul className="pending-start-list" aria-label={classStartAdmin.listLabel}>
      {items.map((item) => {
        const { classSession } = item
        const selected = classSession.id === selectedId
        return (
          <li key={classSession.id}>
            <button
              type="button"
              className={`pending-start-list__card ${selected ? 'pending-start-list__card--selected' : ''}`}
              aria-pressed={selected}
              onClick={() => onSelect(classSession.id)}
            >
              <span className="pending-start-list__head">
                <strong className="pending-start-list__title">
                  {item.staffName} · {classSession.institutionName} {classSession.target}
                </strong>
                <ClassStatusBadge status={classSession.status} />
              </span>
              <span className="pending-start-list__meta">
                <span>
                  {shortDateLabel(classSession.classDate)} {formatTime(classSession.startTime)}–
                  {formatTime(classSession.endTime)}
                </span>
                <span>{classStartAdmin.registeredAt(formatClock(classSession.start.startedAt))}</span>
              </span>
              <span className="pending-start-list__meta">
                <StartLocation start={classSession.start} />
              </span>
            </button>
          </li>
        )
      })}
    </ul>
  )
}

export default PendingStartList
