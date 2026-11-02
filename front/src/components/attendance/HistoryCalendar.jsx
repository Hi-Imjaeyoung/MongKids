import { useEffect } from 'react'
import { historyCalendar } from '../../data/attendanceContent'
import { addDays, toDateKey } from './attendanceFormat'
import './HistoryCalendar.css'

// 달력으로 날짜 고르기 (EMP-SCR-001 ⑪). 아래에서 올라오는 월 달력(월요일 시작)
// month: 그 달 1일. pickedKey: 고른 날(보기 버튼을 눌러야 적용). summary: 날짜 키 → { count, rejected }
function HistoryCalendar({ month, pickedKey, todayKey, summary, error, onPrevMonth, onNextMonth, onPick, onApply, onGoToday, onClose }) {
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [onClose])

  const leading = (month.getDay() + 6) % 7
  const dayCount = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate()
  const days = Array.from({ length: dayCount }, (_, index) => addDays(month, index))
  const picked = days.find((day) => toDateKey(day) === pickedKey)

  return (
    <div className="history-calendar" onClick={onClose}>
      <section
        className="history-calendar__sheet"
        role="dialog"
        aria-modal="true"
        aria-labelledby="history-calendar-title"
        onClick={(event) => event.stopPropagation()}
      >
        <span className="history-calendar__handle" aria-hidden="true" />
        <div className="history-calendar__nav">
          <button type="button" className="history-calendar__arrow" aria-label={historyCalendar.prevMonth} onClick={onPrevMonth}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <path d="M15 5l-7 7 7 7" />
            </svg>
          </button>
          <h2 id="history-calendar-title" className="history-calendar__title">
            {historyCalendar.title(month.getFullYear(), month.getMonth() + 1)}
          </h2>
          <button type="button" className="history-calendar__arrow" aria-label={historyCalendar.nextMonth} onClick={onNextMonth}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <path d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>

        <div className="history-calendar__grid">
          {historyCalendar.weekdays.map((weekday) => (
            <span key={weekday} className="history-calendar__weekday">
              {weekday}
            </span>
          ))}
          {Array.from({ length: leading }, (_, index) => (
            <span key={`blank-${index}`} aria-hidden="true" />
          ))}
          {days.map((day) => {
            const key = toDateKey(day)
            const info = summary.get(key)
            const selected = key === pickedKey
            return (
              <button
                key={key}
                type="button"
                className={`history-calendar__day ${key === todayKey ? 'history-calendar__day--today' : ''} ${selected ? 'history-calendar__day--selected' : ''}`}
                aria-pressed={selected}
                aria-label={`${day.getMonth() + 1}월 ${day.getDate()}일`}
                onClick={() => onPick(day)}
              >
                {day.getDate()}
                <span
                  className={`history-mark ${info ? 'history-mark--class' : ''} ${info?.rejected ? 'history-mark--rejected' : ''}`}
                  aria-hidden="true"
                />
              </button>
            )
          })}
        </div>

        {error && (
          <p className="attendance-error" role="alert">
            {historyCalendar.loadError}
          </p>
        )}
        <div className="history-calendar__legend">
          <span className="history-calendar__legend-item">
            <span className="history-mark history-mark--class" aria-hidden="true" />
            {historyCalendar.legendClass}
          </span>
          <span className="history-calendar__legend-item">
            <span className="history-mark history-mark--rejected" aria-hidden="true" />
            {historyCalendar.legendRejected}
          </span>
          <span className="history-calendar__legend-item">
            <span className="history-calendar__legend-today" aria-hidden="true" />
            {historyCalendar.legendToday}
          </span>
        </div>

        <div className="history-calendar__actions">
          <button type="button" className="history-calendar__button" onClick={onGoToday}>
            {historyCalendar.goToday}
          </button>
          <button
            type="button"
            className="history-calendar__button history-calendar__button--primary"
            disabled={!picked}
            onClick={() => onApply(picked)}
          >
            {picked ? historyCalendar.view(picked) : historyCalendar.close}
          </button>
        </div>
      </section>
    </div>
  )
}

export default HistoryCalendar
