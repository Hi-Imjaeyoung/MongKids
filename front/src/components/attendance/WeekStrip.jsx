import { classHistory } from '../../data/attendanceContent'
import { addDays, toDateKey } from './attendanceFormat'
import './WeekStrip.css'

// 주간 날짜 띠 (EMP-SCR-001 ⑧·⑩). weekStart(월요일)부터 7일, 수업 있는 날은 점, 반려가 있는 날은 빨간 테두리 원
// summary: 날짜 키 → { count, rejected }. 오늘을 누르면 onSelect가 오늘 수업으로 돌려보낸다
function WeekStrip({ weekStart, selectedKey, todayKey, summary, onSelect, onPrevWeek, onNextWeek, onToday }) {
  const days = Array.from({ length: 7 }, (_, index) => addDays(weekStart, index))

  return (
    <section className="week-strip" aria-label={classHistory.weekLabel}>
      <div className="week-strip__nav">
        <button type="button" className="week-strip__arrow" aria-label={classHistory.prevWeek} onClick={onPrevWeek}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
            <path d="M15 5l-7 7 7 7" />
          </svg>
        </button>
        <span className="week-strip__range">{classHistory.weekRange(days[0], days[6])}</span>
        <div className="week-strip__nav-end">
          <button type="button" className="week-strip__today" onClick={onToday}>
            {classHistory.today}
          </button>
          <button type="button" className="week-strip__arrow" aria-label={classHistory.nextWeek} onClick={onNextWeek}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <path d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
      <div className="week-strip__days">
        {days.map((day) => {
          const key = toDateKey(day)
          const info = summary.get(key)
          const selected = key === selectedKey
          const isToday = key === todayKey
          return (
            <button
              key={key}
              type="button"
              className={`week-strip__day ${selected ? 'week-strip__day--selected' : ''} ${isToday ? 'week-strip__day--today' : ''}`}
              aria-current={selected ? 'date' : undefined}
              aria-label={classHistory.dayLabel(day, info?.count)}
              onClick={() => onSelect(day)}
            >
              <span className="week-strip__weekday">{isToday ? classHistory.today : classHistory.weekday(day)}</span>
              <span className="week-strip__date">{day.getDate()}</span>
              <span
                className={`week-strip__mark ${info ? 'week-strip__mark--class' : ''} ${info?.rejected ? 'week-strip__mark--rejected' : ''}`}
                aria-hidden="true"
              />
            </button>
          )
        })}
      </div>
    </section>
  )
}

export default WeekStrip
