import { workCalendarAdmin } from '../../data/attendanceAdminContent'
import './ClassSessionTable.css'
import './WorkCalendarTotals.css'

// 월간 직원별 합계 표 (EMP-FR-017): 할당(정기 / 행사·특별활동)·수업 종료 수업 수, 승인 대기·반려 건수. staffs: WorkCalendarResponse.staffs
function WorkCalendarTotals({ title, staffs }) {
  const { totalsColumns: columns } = workCalendarAdmin
  return (
    <>
      <h2 className="work-calendar-totals__title">{title}</h2>
      <div
        className="class-session-table work-calendar-totals"
        role="region"
        aria-label={workCalendarAdmin.totalsLabel}
        tabIndex={0}
      >
        <table>
          <thead>
            <tr>
              <th scope="col">{columns.staff}</th>
              <th scope="col" className="work-calendar-totals__num">
                {columns.assigned}
              </th>
              <th scope="col" className="work-calendar-totals__num">
                {columns.regular}
              </th>
              <th scope="col" className="work-calendar-totals__num">
                {columns.events}
              </th>
              <th scope="col" className="work-calendar-totals__num">
                {columns.completed}
              </th>
              <th scope="col" className="work-calendar-totals__num">
                {columns.pending}
              </th>
              <th scope="col" className="work-calendar-totals__num">
                {columns.rejected}
              </th>
            </tr>
          </thead>
          <tbody>
            {staffs.map(({ staffId, staffName, summary }) => (
              <tr key={staffId}>
                <th scope="row" className="work-calendar-totals__staff">
                  {staffName}
                </th>
                <td className="work-calendar-totals__num">{workCalendarAdmin.classes(summary.assignedClasses)}</td>
                <td className="work-calendar-totals__num">
                  {workCalendarAdmin.classes(summary.assignedClasses - summary.eventClasses)}
                </td>
                <td className="work-calendar-totals__num">{workCalendarAdmin.classes(summary.eventClasses)}</td>
                <td className="work-calendar-totals__num">{workCalendarAdmin.classes(summary.completedClasses)}</td>
                <td className="work-calendar-totals__num">{workCalendarAdmin.cases(summary.pendingChecks)}</td>
                <td className="work-calendar-totals__num">{workCalendarAdmin.cases(summary.rejectedChecks)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}

export default WorkCalendarTotals
