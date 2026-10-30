import { classFilters } from '../../data/attendanceAdminContent'
import './AdminForm.css'
import './ClassFilters.css'

// 근태 목록의 기간·직원·기관 필터. value: { from, to, staffId, institutionId }, onChange(patch).
// institutions를 넘기지 않으면 기관 필터를 숨긴다. children(버튼 등)은 줄 오른쪽 끝에 둔다
function ClassFilters({ value, onChange, staffs, institutions, children }) {
  return (
    <div className="class-filters">
      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="class-filter-from">
          {classFilters.from}
        </label>
        <input
          id="class-filter-from"
          className="admin-form__input class-filters__date"
          type="date"
          value={value.from}
          onChange={(event) => onChange({ from: event.target.value })}
        />
      </div>
      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="class-filter-to">
          {classFilters.to}
        </label>
        <input
          id="class-filter-to"
          className="admin-form__input class-filters__date"
          type="date"
          value={value.to}
          min={value.from}
          onChange={(event) => onChange({ to: event.target.value })}
        />
      </div>
      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="class-filter-staff">
          {classFilters.staff}
        </label>
        <select
          id="class-filter-staff"
          className="admin-form__input class-filters__select"
          value={value.staffId ?? ''}
          onChange={(event) => onChange({ staffId: event.target.value ? Number(event.target.value) : null })}
        >
          <option value="">{classFilters.allStaff}</option>
          {staffs.map((staff) => (
            <option key={staff.id} value={staff.id}>
              {staff.name}
            </option>
          ))}
        </select>
      </div>
      {institutions && (
        <div className="admin-form__field">
          <label className="admin-form__label" htmlFor="class-filter-institution">
            {classFilters.institution}
          </label>
          <select
            id="class-filter-institution"
            className="admin-form__input class-filters__select"
            value={value.institutionId ?? ''}
            onChange={(event) =>
              onChange({ institutionId: event.target.value ? Number(event.target.value) : null })
            }
          >
            <option value="">{classFilters.allInstitutions}</option>
            {institutions.map((institution) => (
              <option key={institution.id} value={institution.id}>
                {institution.name}
              </option>
            ))}
          </select>
        </div>
      )}
      {children && <div className="class-filters__actions">{children}</div>}
    </div>
  )
}

export default ClassFilters
