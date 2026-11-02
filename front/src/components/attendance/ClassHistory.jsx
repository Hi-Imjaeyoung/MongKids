import { attendancePage, classHistory } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import HistoryClassCard from './HistoryClassCard'
import './ClassHistory.css'

// 날짜별 수업 기록 화면 (EMP-SCR-001 ⑧·⑩). 위쪽 날짜 띠(strip)는 바깥에서 넣고, 고른 날짜의 수업 카드를 보여 준다
// classes: 고른 날짜의 수업. null이면 불러오는 중. future: 고른 날짜가 오늘보다 뒤인지
function ClassHistory({ date, classes, error, future, strip, onBack, onCalendar, onReload, onView }) {
  let body
  if (error) {
    body = (
      <div className="class-history__notice" role="alert">
        <p className="class-history__notice-text">{error}</p>
        <AttendanceAction variant="outline" size="md" onClick={onReload}>
          {classHistory.retry}
        </AttendanceAction>
      </div>
    )
  } else if (!classes) {
    body = <p className="class-history__notice-text">{classHistory.loading}</p>
  } else if (classes.length === 0) {
    body = <p className="class-history__notice-text">{classHistory.empty}</p>
  } else {
    body = (
      <>
        {classes.map((classSession) => (
          <HistoryClassCard
            key={classSession.id}
            classSession={classSession}
            future={future}
            onView={() => onView(classSession)}
          />
        ))}
        <p className="attendance-hint">{future ? classHistory.futureNotice : classHistory.pastNotice}</p>
      </>
    )
  }

  return (
    <section className="class-history" aria-labelledby="class-history-title">
      <header className="class-history__head">
        <div className="class-history__head-start">
          <button type="button" className="class-history__icon" aria-label={attendancePage.back} onClick={onBack}>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <path d="M15 5l-7 7 7 7" />
            </svg>
          </button>
          <h1 id="class-history-title" className="class-history__title">
            {classHistory.title}
          </h1>
        </div>
        <button type="button" className="class-history__icon" aria-label={classHistory.calendar} onClick={onCalendar}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
            <rect x="4" y="5" width="16" height="15" rx="2" />
            <path d="M4 10h16M9 3v4M15 3v4" />
          </svg>
        </button>
      </header>
      {strip}
      <div className="class-history__body">
        <h2 className="class-history__date">{classHistory.dateTitle(date, classes?.length ?? 0)}</h2>
        {body}
      </div>
    </section>
  )
}

export default ClassHistory
