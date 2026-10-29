import { classCard } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import ClassStatusBadge from './ClassStatusBadge'
import { formatClassMeta, formatClock, formatStart, formatTimeRange, pastDateOf } from './attendanceFormat'
import './ClassCard.css'

const PIN_ICON = (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
    <path d="M12 21s-7-6.2-7-11a7 7 0 0 1 14 0c0 4.8-7 11-7 11z" />
    <circle cx="12" cy="10" r="2.5" />
  </svg>
)

function detailLines(classSession) {
  const { status, start, end } = classSession
  const timeRange = formatTimeRange(classSession)
  if (status === 'NOT_STARTED') return [formatClassMeta(classSession), classSession.institutionAddress]
  if (status === 'REJECTED') return [formatClassMeta(classSession), classCard.rejectReason(start.rejectReason)]
  if (status === 'ENDED') {
    return [
      [timeRange, formatStart(start), classCard.ended(formatClock(end.endedAt)), classCard.photoCount(end.images.length)]
        .join(' · '),
    ]
  }
  const startLine = [timeRange, formatStart(start)]
  if (start.method === 'LOCATION' && start.distanceMeters != null) startLine.push(classCard.distance(start.distanceMeters))
  return status === 'PENDING' ? [startLine.join(' · '), classCard.pendingHint] : [startLine.join(' · ')]
}

// 오늘 수업 카드 (EMP-SCR-001 ①). 상태에 따라 버튼이 바뀐다:
// 시작 전 → 현재 위치로 수업 시작, 반려 → 다시 수업 시작, 시작 확정·승인 대기 → 수업 종료, 종료 완료 → 수업 기록 정정(정정 기한 안에서만)
// highlighted: 지금 해야 할 첫 수업. 크게 보여 준다
function ClassCard({ classSession, today, highlighted = false, locating = false, disabled = false, onStart, onEnd, onCorrect }) {
  const { status } = classSession
  const pastDate = pastDateOf(classSession, today)
  const name = pastDate
    ? `${classSession.institutionName} ${classCard.pastDate(...pastDate)}`
    : classSession.institutionName

  let action = null
  if (status === 'NOT_STARTED' || status === 'REJECTED') {
    const restart = status === 'REJECTED'
    action = (
      <AttendanceAction
        variant={restart ? 'outline-blue' : 'primary'}
        size={highlighted ? 'lg' : 'md'}
        icon={restart ? null : PIN_ICON}
        disabled={disabled}
        onClick={onStart}
      >
        {locating ? classCard.locating : restart ? classCard.restart : classCard.startByLocation}
      </AttendanceAction>
    )
  } else if (status === 'STARTED' || status === 'PENDING') {
    action = (
      <AttendanceAction variant="dark" size={highlighted ? 'lg' : 'md'} disabled={disabled} onClick={onEnd}>
        {classCard.end}
      </AttendanceAction>
    )
  } else if (classSession.end?.correctable) {
    action = (
      <AttendanceAction variant="outline" size="md" disabled={disabled} onClick={onCorrect}>
        {classCard.correct}
      </AttendanceAction>
    )
  }

  return (
    <article
      className={`class-card ${highlighted ? 'class-card--highlighted' : ''} ${status === 'REJECTED' ? 'class-card--rejected' : ''}`}
    >
      <div className="class-card__head">
        {highlighted ? (
          <span className="class-card__next">{classCard.next}</span>
        ) : (
          <h2 className="class-card__name">{name}</h2>
        )}
        <ClassStatusBadge status={status} />
      </div>
      {highlighted && <h2 className="class-card__name class-card__name--lg">{name}</h2>}
      <div className="class-card__details">
        {detailLines(classSession).map((line) => (
          <p key={line} className="class-card__detail">
            {line}
          </p>
        ))}
      </div>
      {action}
    </article>
  )
}

export default ClassCard
