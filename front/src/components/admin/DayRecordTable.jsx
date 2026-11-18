import { dayRecordAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatClock, formatRoute } from '../attendance/attendanceFormat'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import { dayKey } from './attendanceAdminFormat'
import './AdminForm.css'
import './ClassSessionTable.css'

function checkText(check) {
  if (!check) return '-'
  const distance = check.distanceMeters == null ? '' : ` · ${check.distanceMeters}m`
  return `${formatClock(check.checkedAt)}${distance}`
}

// 하루 기록 표 (EMP-FR-003, 007, 020). items는 AdminWorkDayResponse[]. 하루를 고르면 onSelect(dayKey)
// 하루 단위 적용 전 날짜는 상태 대신 "이전 방식 기록"으로 표시한다
function DayRecordTable({ items, selectedKey, onSelect }) {
  const { columns } = dayRecordAdmin

  return (
    <div className="class-session-table" role="region" aria-label={dayRecordAdmin.listLabel} tabIndex={0}>
      <table>
        <thead>
          <tr>
            <th scope="col">{columns.date}</th>
            <th scope="col">{columns.staff}</th>
            <th scope="col">{columns.route}</th>
            <th scope="col">{columns.status}</th>
            <th scope="col">{columns.start}</th>
            <th scope="col">{columns.end}</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => {
            const { day } = item
            const key = dayKey(item)
            const selected = key === selectedKey
            const legacy = day.legacyClasses.length > 0
            return (
              <tr key={key} className={selected ? 'class-session-table__row--selected' : undefined}>
                <td>{shortDateLabel(day.date)}</td>
                <td>{item.staffName}</td>
                <td>
                  <button
                    type="button"
                    className="class-session-table__title"
                    aria-pressed={selected}
                    onClick={() => onSelect(key)}
                  >
                    {dayRecordAdmin.route(formatRoute(day), day.classes.length)}
                  </button>
                </td>
                <td>{legacy ? dayRecordAdmin.legacy : <ClassStatusBadge status={day.status} day />}</td>
                <td>{legacy ? '-' : checkText(day.start)}</td>
                <td>{legacy ? '-' : checkText(day.end)}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default DayRecordTable
