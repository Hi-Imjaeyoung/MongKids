import { attendancePage, todayLabel, todayTitle } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import ClassCard from './ClassCard'
import { pastDateOf } from './attendanceFormat'
import './ClassList.css'

const ACTIONABLE = ['NOT_STARTED', 'REJECTED', 'STARTED', 'PENDING']

// 오늘 수업 화면 (EMP-SCR-001 ①). classes가 null이면 불러오는 중이다
// locatingId: 위치를 받는 중인 수업. 그동안 다른 버튼은 누를 수 없다
function ClassList({ classes, today, error, actionError, locatingId, onReload, onStart, onEnd, onCorrect }) {
  const todayCount = classes?.filter((classSession) => !pastDateOf(classSession, today)).length
  const highlightedId = classes?.find((classSession) => ACTIONABLE.includes(classSession.status))?.id

  let body
  if (error) {
    body = (
      <div className="class-list__notice" role="alert">
        <p className="class-list__notice-text">{error}</p>
        <AttendanceAction variant="outline" size="md" onClick={onReload}>
          {attendancePage.retry}
        </AttendanceAction>
      </div>
    )
  } else if (!classes) {
    body = <p className="class-list__notice-text">{attendancePage.loading}</p>
  } else if (classes.length === 0) {
    body = <p className="class-list__notice-text">{attendancePage.empty}</p>
  } else {
    body = classes.map((classSession) => (
      <ClassCard
        key={classSession.id}
        classSession={classSession}
        today={today}
        highlighted={classSession.id === highlightedId}
        locating={classSession.id === locatingId}
        disabled={locatingId != null}
        onStart={() => onStart(classSession)}
        onEnd={() => onEnd(classSession)}
        onCorrect={() => onCorrect(classSession)}
      />
    ))
  }

  return (
    <section className="class-list" aria-labelledby="class-list-title">
      <header className="class-list__head">
        <span className="class-list__date">{todayLabel(today)}</span>
        <h1 id="class-list-title" className="class-list__title">
          {todayTitle(todayCount ?? 0)}
        </h1>
      </header>
      <div className="class-list__body">
        {actionError && (
          <p className="attendance-error class-list__action-error" role="alert">
            {actionError}
          </p>
        )}
        {body}
      </div>
    </section>
  )
}

export default ClassList
