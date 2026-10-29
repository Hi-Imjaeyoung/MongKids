import { attendancePage } from '../../data/attendanceContent'
import './AttendanceScreen.css'

// 근태 하위 화면(②~⑦)의 틀: 뒤로 버튼 + 제목, 본문, 아래 고정 버튼 영역
function AttendanceScreen({ title, onBack, footer, children }) {
  return (
    <section className="attendance-screen" aria-labelledby="attendance-screen-title">
      <header className="attendance-screen__head">
        <button type="button" className="attendance-screen__back" aria-label={attendancePage.back} onClick={onBack}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
            <path d="M15 5l-7 7 7 7" />
          </svg>
        </button>
        <h1 id="attendance-screen-title" className="attendance-screen__title">
          {title}
        </h1>
      </header>
      <div className="attendance-screen__body">{children}</div>
      {footer && <footer className="attendance-screen__foot">{footer}</footer>}
    </section>
  )
}

export default AttendanceScreen
