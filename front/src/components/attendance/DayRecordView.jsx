import { dayRecordView, dayEndForm } from '../../data/attendanceContent'
import AttendanceScreen from './AttendanceScreen'
import ClassStatusBadge from './ClassStatusBadge'
import { formatClock, formatRoute, fromDateKey } from './attendanceFormat'
import './ClassRecordView.css'
import './ClassSummary.css'

const LOCK_ICON = (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
    <rect x="5" y="11" width="14" height="9" rx="2" />
    <path d="M8 11V8a4 4 0 0 1 8 0v3" />
  </svg>
)

// "2026-10-28T15:10:00" → "10월 28일 15:10"
function formatDateTime(dateTime) {
  if (!dateTime) return null
  const date = fromDateKey(dateTime.slice(0, 10))
  return `${date.getMonth() + 1}월 ${date.getDate()}일 ${formatClock(dateTime)}`
}

function checkText(check) {
  return dayRecordView.checkText(
    formatClock(check.checkedAt),
    dayRecordView.methods[check.method],
    check.institutionName,
    check.distanceMeters,
  )
}

// 지난 하루 기록 보기 (EMP-SCR-001 ⑨, EMP-FR-020). 정정 기한(그날, EMP-FR-014 규칙)이 지나 읽기만 한다
function DayRecordView({ day, onBack }) {
  const { start, end } = day
  const rows = [
    start && [dayRecordView.start, checkText(start)],
    start?.reason && [dayRecordView.reason, start.reason],
    start?.status === 'REJECTED' && [dayRecordView.rejectReason, start.rejectReason],
    end && [dayRecordView.end, checkText(end)],
    end?.reason && [dayRecordView.reason, end.reason],
    end?.status === 'REJECTED' && [dayRecordView.rejectReason, end.rejectReason],
    end && [dayRecordView.corrected, formatDateTime(day.correctedAt) ?? dayRecordView.none],
  ].filter(Boolean)

  return (
    <AttendanceScreen
      title={dayRecordView.title(fromDateKey(day.date))}
      onBack={onBack}
      footer={
        <p className="class-record__locked">
          {LOCK_ICON}
          {dayRecordView.locked}
        </p>
      }
    >
      <section className="class-summary">
        <div className="class-summary__head">
          <strong className="class-summary__name">{formatRoute(day)}</strong>
          <ClassStatusBadge status={day.status} day />
        </div>
        <span className="class-summary__meta">{dayEndForm.summary(day.classes.length, null, null)}</span>
        <dl className="class-record__rows">
          {rows.map(([term, value], index) => (
            <div key={`${term}-${index}`} className="class-record__row">
              <dt className="class-record__term">{term}</dt>
              <dd className="class-record__value">{value}</dd>
            </div>
          ))}
        </dl>
      </section>

      {day.images.length > 0 && (
        <section className="attendance-field">
          <h2 className="attendance-field__label">{dayRecordView.photos(day.images.length)}</h2>
          <ul className="class-record__photos">
            {day.images.map((image, index) => (
              <li key={image.imageKey} className="class-record__photo">
                <a href={image.url} target="_blank" rel="noreferrer">
                  <img src={image.url} alt={dayRecordView.photoAlt(index + 1)} loading="lazy" />
                </a>
              </li>
            ))}
          </ul>
        </section>
      )}

      {day.notes.length > 0 && (
        <section className="attendance-field">
          <h2 className="attendance-field__label">{dayRecordView.notes}</h2>
          {day.notes.map((note) => (
            <div key={note.institutionId} className="class-record__note">
              <span className="class-record__note-label">{note.institutionName}</span>
              <p className="class-record__note-text">{note.note || dayRecordView.none}</p>
            </div>
          ))}
        </section>
      )}
    </AttendanceScreen>
  )
}

export default DayRecordView
