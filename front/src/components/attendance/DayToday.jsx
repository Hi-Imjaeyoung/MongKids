import { Fragment } from 'react'
import { attendancePage, checkLine, classHistory, dayToday, todayLabel } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import ClassStatusBadge from './ClassStatusBadge'
import {
  countInstitutions,
  firstInstitution,
  formatCheck,
  formatClassMeta,
  groupByInstitution,
  lastInstitution,
} from './attendanceFormat'
import './DayToday.css'

const PIN_ICON = (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
    <path d="M12 21s-7-6.2-7-11a7 7 0 0 1 14 0c0 4.8-7 11-7 11z" />
    <circle cx="12" cy="10" r="2.5" />
  </svg>
)

const START_STATUSES = ['NOT_STARTED', 'REJECTED']
const END_STATUSES = ['STARTED', 'PENDING', 'END_REJECTED']

// 하루 시작·종료 기록 한 줄씩: "09:52 시작 · 해님어린이집 · 기관까지 32m"
function recordLines(day) {
  const { start, end } = day
  const lines = []
  if (start) {
    const parts = [formatCheck(start, 'start'), start.institutionName]
    if (start.method === 'LOCATION' && start.distanceMeters != null) parts.push(checkLine.distance(start.distanceMeters))
    lines.push(parts.join(' · '))
    if (start.rejectReason && start.status === 'REJECTED') lines.push(checkLine.rejectReason(start.rejectReason))
  }
  if (end) {
    lines.push([formatCheck(end, 'end'), end.institutionName, checkLine.photoCount(day.images.length)].join(' · '))
    if (end.rejectReason && end.status === 'REJECTED') lines.push(checkLine.rejectReason(end.rejectReason))
  }
  return lines
}

// 오늘 하루 안내 문구. 기관 이름은 굵게
function guideText(day) {
  const institution = START_STATUSES.includes(day.status) ? firstInstitution(day) : lastInstitution(day)
  const [before, name, after] = dayToday.guide[day.status](institution)
  return (
    <>
      {before}
      {name && <strong className="day-today__strong">{name}</strong>}
      {after}
    </>
  )
}

function badgeOf(index, count) {
  if (count === 1) return { text: dayToday.onlyBadge, tone: 'first' }
  if (index === 0) return { text: dayToday.firstBadge, tone: 'first' }
  if (index === count - 1) return { text: dayToday.lastBadge, tone: 'last' }
  return null
}

// 오늘 하루 화면 (EMP-SCR-001 ①, EMP-FR-020). day가 null이면 불러오는 중이다.
// 상태에 따라 아래 버튼이 바뀐다: 시작 전·시작 반려 → 수업 시작, 수업 중·시작 승인 대기·종료 반려 → 수업 종료,
// 수업 종료·종료 승인 대기 → 수업 기록 정정(그날 안에서만). locating: 위치를 받는 중. onHistory: 날짜별 수업 기록(⑧)
function DayToday({ day, today, error, actionError, locating, onReload, onStart, onEnd, onCorrect, onHistory }) {
  const classes = day?.classes ?? []
  const groups = groupByInstitution(classes)
  const title = day && classes.length > 0 ? dayToday.title(classes.length, countInstitutions(classes)) : dayToday.emptyTitle

  let body
  if (error) {
    body = (
      <div className="day-today__notice" role="alert">
        <p className="day-today__notice-text">{error}</p>
        <AttendanceAction variant="outline" size="md" onClick={onReload}>
          {attendancePage.retry}
        </AttendanceAction>
      </div>
    )
  } else if (!day) {
    body = <p className="day-today__notice-text">{attendancePage.loading}</p>
  } else if (classes.length === 0) {
    body = <p className="day-today__notice-text">{attendancePage.empty}</p>
  } else {
    body = (
      <>
        <section className={`day-card day-card--${day.status.toLowerCase().replace('_', '-')}`} aria-label={dayToday.cardTitle}>
          <div className="day-card__head">
            <strong className="day-card__title">{dayToday.cardTitle}</strong>
            <ClassStatusBadge status={day.status} day />
          </div>
          {recordLines(day).map((line) => (
            <p key={line} className="day-card__line">
              {line}
            </p>
          ))}
          <p className="day-card__guide">
            {guideText(day)}
            {day.status === 'REJECTED' && day.end && ` ${dayToday.endKept}`}
          </p>
        </section>

        <ol className="day-today__groups">
          {groups.map((group, index) => {
            const badge = badgeOf(index, groups.length)
            return (
              <Fragment key={`${group.institutionId}-${group.classes[0].id}`}>
                {index > 0 && (
                  <li className="day-today__move" aria-hidden="true">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                      <path d="M12 5v14M6 13l6 6 6-6" />
                    </svg>
                    {dayToday.move}
                  </li>
                )}
                <li className="day-group">
                  <div className="day-group__head">
                    <strong className="day-group__name">{group.institutionName}</strong>
                    {badge && <span className={`day-group__badge day-group__badge--${badge.tone}`}>{badge.text}</span>}
                  </div>
                  <div className="day-group__classes">
                    {group.classes.map((dayClass) => (
                      <span key={dayClass.id}>{formatClassMeta(dayClass)}</span>
                    ))}
                  </div>
                </li>
              </Fragment>
            )
          })}
        </ol>
      </>
    )
  }

  let action = null
  if (day && classes.length > 0) {
    if (START_STATUSES.includes(day.status)) {
      const restart = day.status === 'REJECTED'
      action = (
        <>
          <AttendanceAction variant={restart ? 'outline-blue' : 'primary'} icon={PIN_ICON} disabled={locating} onClick={onStart}>
            {locating ? dayToday.locating : restart ? dayToday.restart : dayToday.startByLocation}
          </AttendanceAction>
          <span className="day-today__hint">{dayToday.radiusHint(firstInstitution(day), dayToday.radius)}</span>
        </>
      )
    } else if (END_STATUSES.includes(day.status)) {
      action = (
        <>
          <AttendanceAction variant="dark" icon={PIN_ICON} disabled={locating} onClick={onEnd}>
            {locating ? dayToday.locating : day.status === 'END_REJECTED' ? dayToday.reEnd : dayToday.endByLocation}
          </AttendanceAction>
          <span className="day-today__hint">{dayToday.radiusHint(lastInstitution(day), dayToday.radius)}</span>
        </>
      )
    } else if (day.correctable) {
      action = (
        <AttendanceAction variant="outline" onClick={onCorrect}>
          {dayToday.correct}
        </AttendanceAction>
      )
    }
  }

  return (
    <section className="day-today" aria-labelledby="day-today-title">
      <header className="day-today__head">
        <div className="day-today__heading">
          <span className="day-today__date">{todayLabel(today)}</span>
          <h1 id="day-today-title" className="day-today__title">
            {title}
          </h1>
        </div>
        <button type="button" className="day-today__history" aria-label={classHistory.open} onClick={onHistory}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
            <circle cx="12" cy="12" r="9" />
            <path d="M12 7v5l3 2" />
          </svg>
        </button>
      </header>
      <div className="day-today__body">
        {actionError && (
          <p className="attendance-error day-today__action-error" role="alert">
            {actionError}
          </p>
        )}
        {body}
      </div>
      {action && <footer className="day-today__foot">{action}</footer>}
    </section>
  )
}

export default DayToday
