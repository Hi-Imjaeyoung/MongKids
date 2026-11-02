import { classRecordView } from '../../data/attendanceContent'
import AttendanceScreen from './AttendanceScreen'
import ClassSummary from './ClassSummary'
import { formatClock, fromDateKey } from './attendanceFormat'
import './ClassRecordView.css'

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

function startText(start) {
  const parts = [formatClock(start.startedAt), classRecordView.methods[start.method]]
  if (start.distanceMeters != null) parts.push(classRecordView.distance(start.distanceMeters))
  return parts.join(' · ')
}

// 지난 수업 기록 보기 (EMP-SCR-001 ⑨). 정정 기한(수업 당일, EMP-FR-014)이 지나 읽기만 한다
function ClassRecordView({ classSession, onBack }) {
  const { start, end } = classSession
  const rows = [
    start && [classRecordView.start, startText(start)],
    start?.rejectReason && [classRecordView.rejectReason, start.rejectReason],
    end && [classRecordView.end, formatClock(end.endedAt)],
    end && [classRecordView.corrected, formatDateTime(end.correctedAt) ?? classRecordView.none],
  ].filter(Boolean)
  const images = end?.images ?? []

  return (
    <AttendanceScreen
      title={classRecordView.title(fromDateKey(classSession.classDate))}
      onBack={onBack}
      footer={
        <p className="class-record__locked">
          {LOCK_ICON}
          {classRecordView.locked}
        </p>
      }
    >
      <ClassSummary classSession={classSession} showStatus>
        <dl className="class-record__rows">
          {rows.map(([term, value]) => (
            <div key={term} className="class-record__row">
              <dt className="class-record__term">{term}</dt>
              <dd className="class-record__value">{value}</dd>
            </div>
          ))}
        </dl>
      </ClassSummary>

      {images.length > 0 && (
        <section className="attendance-field">
          <h2 className="attendance-field__label">{classRecordView.photos(images.length)}</h2>
          <ul className="class-record__photos">
            {images.map((image, index) => (
              <li key={image.imageKey} className="class-record__photo">
                <a href={image.url} target="_blank" rel="noreferrer">
                  <img src={image.url} alt={classRecordView.photoAlt(index + 1)} loading="lazy" />
                </a>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="attendance-field">
        <h2 className="attendance-field__label">{classRecordView.notes}</h2>
        {start && (
          <div className="class-record__note">
            <span className="class-record__note-label">{classRecordView.startNote}</span>
            <p className="class-record__note-text">{start.note || classRecordView.none}</p>
          </div>
        )}
        {end && (
          <div className="class-record__note">
            <span className="class-record__note-label">{classRecordView.endNote}</span>
            <p className="class-record__note-text">{end.note || classRecordView.none}</p>
          </div>
        )}
      </section>
    </AttendanceScreen>
  )
}

export default ClassRecordView
