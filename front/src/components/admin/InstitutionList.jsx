import { institutionAdmin } from '../../data/attendanceAdminContent'
import './AdminForm.css'
import './InstitutionList.css'

// 기관 목록 표 (EMP-FR-012). 기관 이름이나 "수정"을 누르면 onSelect(institution)
function InstitutionList({ institutions, selectedId, onSelect }) {
  const { columns } = institutionAdmin

  return (
    <div className="institution-list">
      <p className="institution-list__count">{institutionAdmin.count(institutions.length)}</p>
      <div className="institution-list__table" role="region" aria-label={institutionAdmin.listLabel} tabIndex={0}>
        <table>
          <thead>
            <tr>
              <th scope="col">{columns.name}</th>
              <th scope="col">{columns.address}</th>
              <th scope="col">{columns.location}</th>
              <th scope="col" className="institution-list__number">
                {columns.sessions}
              </th>
              <th scope="col">{columns.actions}</th>
            </tr>
          </thead>
          <tbody>
            {institutions.map((institution) => {
              const selected = institution.id === selectedId
              return (
                <tr key={institution.id} className={selected ? 'institution-list__row--selected' : undefined}>
                  <td>
                    <button
                      type="button"
                      className="institution-list__name"
                      aria-pressed={selected}
                      onClick={() => onSelect(institution)}
                    >
                      {institution.name}
                    </button>
                  </td>
                  <td className="institution-list__address">{institution.address}</td>
                  <td className="institution-list__location">
                    {institution.latitude}, {institution.longitude}
                  </td>
                  <td className="institution-list__number">{institutionAdmin.sessionCount(institution.classSessionCount)}</td>
                  <td>
                    <button
                      type="button"
                      className="admin-form__button admin-form__button--ghost admin-form__button--sm"
                      aria-label={institutionAdmin.editTitle(institution.name)}
                      onClick={() => onSelect(institution)}
                    >
                      {institutionAdmin.edit}
                    </button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default InstitutionList
