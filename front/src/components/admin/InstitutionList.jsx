import { institutionAdmin } from '../../data/attendanceAdminContent'
import './InstitutionList.css'

// 기관 목록 (EMP-FR-012). 고르면 onSelect(institution)
function InstitutionList({ institutions, selectedId, onSelect }) {
  return (
    <div className="institution-list">
      <p className="institution-list__count">{institutionAdmin.count(institutions.length)}</p>
      <ul className="institution-list__items" aria-label={institutionAdmin.listLabel}>
        {institutions.map((institution) => {
          const selected = institution.id === selectedId
          return (
            <li key={institution.id}>
              <button
                type="button"
                className={`institution-list__item ${selected ? 'institution-list__item--selected' : ''}`}
                aria-pressed={selected}
                onClick={() => onSelect(institution)}
              >
                <strong>{institution.name}</strong>
                <span className="institution-list__meta">{institution.address}</span>
                <span className="institution-list__meta">
                  {institution.latitude}, {institution.longitude} ·{' '}
                  {institutionAdmin.sessionCount(institution.classSessionCount)}
                </span>
              </button>
            </li>
          )
        })}
      </ul>
    </div>
  )
}

export default InstitutionList
