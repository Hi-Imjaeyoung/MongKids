import { classCard, classHistory } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import ClassStatusBadge from './ClassStatusBadge'
import { formatClassMeta, formatClock, formatStart } from './attendanceFormat'
import './ClassCard.css'

function detailLines(classSession, future) {
  const { status, start, end } = classSession
  const meta = formatClassMeta(classSession)
  if (future || !start) return [meta, classSession.institutionAddress]
  const startLine = [formatStart(start)]
  if (end) startLine.push(classCard.ended(formatClock(end.endedAt)), classCard.photoCount(end.images.length))
  const lines = [meta, startLine.join(' · ')]
  if (status === 'REJECTED') lines.push(classCard.rejectReason(start.rejectReason))
  else if (end?.note) lines.push(classHistory.note(end.note.split('\n')[0]))
  return lines
}

// 날짜별 수업 기록 카드 (EMP-SCR-001 ⑧·⑩). 오늘 수업 카드(ClassCard)와 모양은 같고 버튼만 다르다:
// 앞으로 날짜 → 버튼 없이 `예정`, 기록이 있으면(반려 포함) → 기록 보기(읽기 전용)
function HistoryClassCard({ classSession, future, onView }) {
  const { status, start, end } = classSession

  let badge = <ClassStatusBadge status={status} />
  if (future) badge = <span className="class-status class-status--blue">{classHistory.scheduled}</span>
  else if (!start) badge = <span className="class-status class-status--plain">{classHistory.noRecord}</span>

  let action = null
  if (!future && (start || end)) {
    action = (
      <AttendanceAction variant="outline" size="md" onClick={onView}>
        {classHistory.viewRecord}
      </AttendanceAction>
    )
  }

  return (
    <article className={`class-card ${status === 'REJECTED' && !future ? 'class-card--rejected' : ''}`}>
      <div className="class-card__head">
        <h3 className="class-card__name">{classSession.institutionName}</h3>
        {badge}
      </div>
      <div className="class-card__details">
        {detailLines(classSession, future).filter(Boolean).map((line) => (
          <p key={line} className="class-card__detail class-card__detail--clamp">
            {line}
          </p>
        ))}
      </div>
      {action}
    </article>
  )
}

export default HistoryClassCard
