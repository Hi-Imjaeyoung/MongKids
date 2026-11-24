import { longDateLabel, workCalendarAdmin } from '../../data/attendanceAdminContent'
import AdminBadge from './AdminBadge'
import { calendarStatus } from './attendanceAdminFormat'
import WorkDayBadge from './WorkDayBadge'
import './WorkMonthCalendar.css'

// 월간 근무 달력 (EMP-SCR-006 ⑤). 월요일부터 7일씩 그 달이 든 주를 모두 보여 주고, 다른 달 날짜는 흐리게 숫자만.
// 날짜마다 그날 수업이 있는 직원의 상태·수업 수. 날짜를 누르면 onSelectDate(date)로 그 주 주간 보기
// weeks: [['YYYY-MM-DD' × 7]], month: 그 달 1일, staffs: WorkCalendarResponse.staffs.
// unassigned가 있으면 날짜마다 "미정 N개" 줄을 더한다 (EMP-FR-021)
function WorkMonthCalendar({ weeks, month, staffs, unassigned = [], today, onSelectDate }) {
  const monthKey = month.slice(0, 7)
  const unassignedByDate = new Map(unassigned.map((day) => [day.date, day.classCount]))
  const linesByDate = new Map()
  staffs.forEach((staff) => {
    staff.days.forEach((day) => {
      const lines = linesByDate.get(day.date) ?? []
      lines.push({ staffId: staff.staffId, staffName: staff.staffName, day })
      linesByDate.set(day.date, lines)
    })
  })

  return (
    <div className="work-month-calendar" role="region" aria-label={workCalendarAdmin.monthListLabel} tabIndex={0}>
      <div className="work-month-calendar__grid">
        <div className="work-month-calendar__row work-month-calendar__head">
          {workCalendarAdmin.weekdays.map((weekday) => (
            <span key={weekday}>{weekday}</span>
          ))}
        </div>
        {weeks.map((dates) => (
          <div key={dates[0]} className="work-month-calendar__row">
            {dates.map((date, index) => {
              const day = Number(date.slice(8))
              if (date.slice(0, 7) !== monthKey) {
                return (
                  <div key={date} className="work-month-calendar__day work-month-calendar__day--out">
                    <span className="work-month-calendar__date">{`${Number(date.slice(5, 7))}/${day}`}</span>
                  </div>
                )
              }
              const lines = linesByDate.get(date) ?? []
              const unassignedCount = unassignedByDate.get(date)
              const attention =
                Boolean(unassignedCount) ||
                lines.some((line) => ['yellow', 'red'].includes(calendarStatus(line.day, today).tone))
              const classNames = [
                'work-month-calendar__day',
                date === today && 'work-month-calendar__day--today',
                date !== today && attention && 'work-month-calendar__day--attention',
                date !== today && !attention && index >= 5 && 'work-month-calendar__day--weekend',
              ]
              return (
                <button
                  key={date}
                  type="button"
                  className={classNames.filter(Boolean).join(' ')}
                  aria-label={workCalendarAdmin.monthDayLabel(longDateLabel(date))}
                  onClick={() => onSelectDate(date)}
                >
                  <span className="work-month-calendar__date">
                    {date === today ? `${day} ${workCalendarAdmin.today}` : day}
                  </span>
                  {lines.map((line) => (
                    <span key={line.staffId} className="work-month-calendar__line">
                      <span className="work-month-calendar__name">{line.staffName}</span>
                      <WorkDayBadge day={line.day} today={today} short />
                      <span className="work-month-calendar__count">
                        {workCalendarAdmin.monthCount(line.day.classCount)}
                      </span>
                      {line.day.eventCount > 0 && (
                        <AdminBadge tone="purple" shape="tag">
                          {workCalendarAdmin.eventCount(line.day.eventCount)}
                        </AdminBadge>
                      )}
                    </span>
                  ))}
                  {unassignedCount && (
                    <span className="work-month-calendar__line">
                      <AdminBadge tone={date < today ? 'red' : 'yellow'}>{workCalendarAdmin.unassignedRow}</AdminBadge>
                      <span className="work-month-calendar__count">{workCalendarAdmin.monthCount(unassignedCount)}</span>
                    </span>
                  )}
                </button>
              )
            })}
          </div>
        ))}
      </div>
    </div>
  )
}

export default WorkMonthCalendar
