import { shortDateLabel, workCalendarAdmin } from '../../data/attendanceAdminContent'
import { formatClock, formatTime } from '../attendance/attendanceFormat'
import AdminBadge from './AdminBadge'
import { calendarStatus } from './attendanceAdminFormat'
import WorkDayBadge from './WorkDayBadge'
import './WorkWeekTable.css'

// 칸 강조: 승인 대기는 노랑, 반려는 빨강
function cellTone(day, today) {
  const { tone } = calendarStatus(day, today)
  if (tone === 'yellow') return 'work-week-table__cell--attention'
  if (tone === 'red') return 'work-week-table__cell--rejected'
  return ''
}

// 실제 하루 시작 – 종료 시각. 시작 전이면 첫 수업 시각
function timeText(day) {
  if (!day.start || day.legacy) return workCalendarAdmin.firstClass(formatTime(day.firstClassTime))
  const manual = [day.start, day.end].find((check) => check && check.method !== 'LOCATION')
  return workCalendarAdmin.timeRange(
    formatClock(day.start.checkedAt),
    day.end ? formatClock(day.end.checkedAt) : '',
    manual ? workCalendarAdmin.methodMarks[manual.method] : null,
  ).trimEnd()
}

// 주간 근무 표 (EMP-SCR-006 ③). 직원 × 7일(월~일) 칸과 이번 주 합계. 칸을 누르면 onSelect(staffId, date)
// dates: 월~일 'YYYY-MM-DD', staffs: WorkCalendarResponse.staffs, today: 오늘 날짜.
// unassigned(WorkCalendarResponse.unassigned)가 있으면 맨 아래 "미정" 줄을 두고, 칸을 누르면 onSelectUnassigned(date) (EMP-FR-021)
function WorkWeekTable({ dates, staffs, unassigned = [], today, onSelect, onSelectUnassigned }) {
  const unassignedByDate = new Map(unassigned.map((day) => [day.date, day.classCount]))
  const unassignedTotal = dates.reduce((sum, date) => sum + (unassignedByDate.get(date) ?? 0), 0)
  return (
    <div className="work-week-table" role="region" aria-label={workCalendarAdmin.weekListLabel} tabIndex={0}>
      <table>
        <thead>
          <tr>
            <th scope="col" className="work-week-table__staff-col">
              {workCalendarAdmin.staffColumn}
            </th>
            {dates.map((date) => (
              <th key={date} scope="col" className={date === today ? 'work-week-table__today' : undefined}>
                {workCalendarAdmin.dayColumn(shortDateLabel(date), date === today)}
              </th>
            ))}
            <th scope="col" className="work-week-table__sum-col">
              {workCalendarAdmin.weekTotalColumn}
            </th>
          </tr>
        </thead>
        <tbody>
          {staffs.map((staff) => {
            const days = new Map(staff.days.map((day) => [day.date, day]))
            const { summary } = staff
            return (
              <tr key={staff.staffId}>
                <th scope="row" className="work-week-table__staff">
                  {staff.staffName}
                </th>
                {dates.map((date) => {
                  const day = days.get(date)
                  const weekend = dates.indexOf(date) >= 5
                  const columnClass = date === today ? 'work-week-table__today-col' : weekend ? 'work-week-table__weekend' : ''
                  return (
                    <td key={date} className={columnClass || undefined}>
                      {day ? (
                        <button
                          type="button"
                          className={`work-week-table__cell ${cellTone(day, today)}`}
                          aria-label={workCalendarAdmin.cellLabel(staff.staffName, shortDateLabel(date))}
                          onClick={() => onSelect(staff.staffId, date)}
                        >
                          <WorkDayBadge day={day} today={today} />
                          <span className="work-week-table__count">
                            {workCalendarAdmin.classCount(day.classCount)}
                            {day.eventCount > 0 && (
                              <>
                                {' '}
                                <AdminBadge tone="purple" shape="tag">
                                  {workCalendarAdmin.eventCount(day.eventCount)}
                                </AdminBadge>
                              </>
                            )}
                          </span>
                          <span className="work-week-table__sub">{timeText(day)}</span>
                          <span className="work-week-table__route">{day.institutionNames.join(' → ')}</span>
                        </button>
                      ) : (
                        <span className="work-week-table__cell work-week-table__cell--empty">
                          {workCalendarAdmin.noClass}
                        </span>
                      )}
                    </td>
                  )
                })}
                <td>
                  <div className="work-week-table__sum">
                    <span className="work-week-table__sum-main">
                      {workCalendarAdmin.weekSum(summary.completedClasses, summary.assignedClasses)}
                    </span>
                    <span className="work-week-table__sub">{workCalendarAdmin.weekSumSub}</span>
                    <span className="work-week-table__sub">
                      {workCalendarAdmin.checks(summary.pendingChecks, summary.rejectedChecks)}
                    </span>
                  </div>
                </td>
              </tr>
            )
          })}
          {unassignedTotal > 0 && (
            <tr className="work-week-table__unassigned">
              <th scope="row" className="work-week-table__staff">
                {workCalendarAdmin.unassignedRow}
              </th>
              {dates.map((date, index) => {
                const count = unassignedByDate.get(date)
                const columnClass = date === today ? 'work-week-table__today-col' : index >= 5 ? 'work-week-table__weekend' : ''
                const past = date < today
                return (
                  <td key={date} className={columnClass || undefined}>
                    {count ? (
                      <button
                        type="button"
                        className={`work-week-table__cell ${past ? 'work-week-table__cell--rejected' : 'work-week-table__cell--attention'}`}
                        aria-label={workCalendarAdmin.unassignedCellLabel(shortDateLabel(date))}
                        onClick={() => onSelectUnassigned(date)}
                      >
                        <AdminBadge tone={past ? 'red' : 'yellow'}>{workCalendarAdmin.unassignedRow}</AdminBadge>
                        <span className="work-week-table__count">{workCalendarAdmin.classCount(count)}</span>
                        <span className="work-week-table__sub">{workCalendarAdmin.unassignedSub}</span>
                      </button>
                    ) : (
                      <span className="work-week-table__cell work-week-table__cell--empty">-</span>
                    )}
                  </td>
                )
              })}
              <td>
                <div className="work-week-table__sum">
                  <span className="work-week-table__sum-main">{workCalendarAdmin.unassignedSum(unassignedTotal)}</span>
                  <span className="work-week-table__sub">{workCalendarAdmin.unassignedSub}</span>
                </div>
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}

export default WorkWeekTable
