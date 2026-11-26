import { classFilters } from '../../data/attendanceAdminContent'
import './AdminForm.css'
import './SeriesFilter.css'

// 미정 정기 수업 목록의 기관 필터 (EMP-SCR-007). institutions: [{ id, name }], value: 기관 id 또는 null, onChange(id | null)
function SeriesFilter({ institutions, value, onChange }) {
  return (
    <div className="admin-form__field series-filter">
      <label className="admin-form__label" htmlFor="series-filter-institution">
        {classFilters.institution}
      </label>
      <select
        id="series-filter-institution"
        className="admin-form__input series-filter__select"
        value={value ?? ''}
        onChange={(event) => onChange(event.target.value ? Number(event.target.value) : null)}
      >
        <option value="">{classFilters.allInstitutions}</option>
        {institutions.map((institution) => (
          <option key={institution.id} value={institution.id}>
            {institution.name}
          </option>
        ))}
      </select>
    </div>
  )
}

export default SeriesFilter
