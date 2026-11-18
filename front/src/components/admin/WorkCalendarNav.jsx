import { classFilters, workCalendarAdmin } from '../../data/attendanceAdminContent'
import FilterChips from './FilterChips'
import './AdminForm.css'
import './WorkCalendarNav.css'

function Chevron({ direction }) {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
      <path d={direction === 'prev' ? 'M15 5l-7 7 7 7' : 'M9 5l7 7-7 7'} />
    </svg>
  )
}

// 근무 캘린더 도구 줄: 주/월 전환, 이전·다음·이번 주(달), 직원 필터.
// view: 'week' | 'month', label: 지금 보는 기간 이름. onMove(-1 | 1), onCurrent(), onViewChange(view), onStaffChange(staffId | null)
function WorkCalendarNav({ view, label, staffs, staffId, onViewChange, onMove, onCurrent, onStaffChange }) {
  return (
    <div className="work-calendar-nav">
      <div className="work-calendar-nav__group">
        <FilterChips label={workCalendarAdmin.viewLabel} options={workCalendarAdmin.views} value={view} onChange={onViewChange} />
        <div className="work-calendar-nav__period">
          <button
            type="button"
            className="work-calendar-nav__icon"
            aria-label={workCalendarAdmin.prev[view]}
            onClick={() => onMove(-1)}
          >
            <Chevron direction="prev" />
          </button>
          <span className="work-calendar-nav__label" aria-live="polite">
            {label}
          </span>
          <button
            type="button"
            className="work-calendar-nav__icon"
            aria-label={workCalendarAdmin.next[view]}
            onClick={() => onMove(1)}
          >
            <Chevron direction="next" />
          </button>
          <button type="button" className="admin-form__button admin-form__button--ghost" onClick={onCurrent}>
            {workCalendarAdmin.current[view]}
          </button>
        </div>
      </div>
      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="work-calendar-staff">
          {classFilters.staff}
        </label>
        <select
          id="work-calendar-staff"
          className="admin-form__input work-calendar-nav__select"
          value={staffId ?? ''}
          onChange={(event) => onStaffChange(event.target.value ? Number(event.target.value) : null)}
        >
          <option value="">{classFilters.allStaff}</option>
          {staffs.map((staff) => (
            <option key={staff.id} value={staff.id}>
              {staff.name}
            </option>
          ))}
        </select>
      </div>
    </div>
  )
}

export default WorkCalendarNav
