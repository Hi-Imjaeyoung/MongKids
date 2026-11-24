import { useState } from 'react'
import { daysOfWeek, institutionAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatTime } from '../attendance/attendanceFormat'
import './AdminForm.css'
import './InstitutionList.css'

const DAY_LABELS = Object.fromEntries(daysOfWeek.map((day) => [day.id, day.label]))

// { daysOfWeek: ['MONDAY', 'WEDNESDAY'], startTime, endTime } → "월 · 수 10:00–11:00"
function scheduleLabel(schedule) {
  const days = schedule.daysOfWeek.map((day) => DAY_LABELS[day]).join(' · ')
  return `${days} ${formatTime(schedule.startTime)}–${formatTime(schedule.endTime)}`
}

function matches(institution, query) {
  return `${institution.name} ${institution.address}`.toLowerCase().includes(query)
}

// 기관 표 (EMP-FR-012, EMP-SCR-006 11-18 초안). 기관 이름·주소로 찾고, 줄마다 수업 추가·수정·삭제.
// onAddClass(institution), onEdit(institution), onDelete(institution). selectedId인 줄을 강조한다
function InstitutionList({ institutions, selectedId, onAddClass, onEdit, onDelete }) {
  const [query, setQuery] = useState('')
  const text = institutionAdmin
  const { columns } = text
  const normalized = query.trim().toLowerCase()
  const shown = normalized ? institutions.filter((institution) => matches(institution, normalized)) : institutions

  return (
    <div className="institution-list">
      <div className="institution-list__bar">
        <div className="admin-form__field institution-list__search">
          <label className="admin-form__label" htmlFor="institution-query">
            {text.searchLabel}
          </label>
          <input
            id="institution-query"
            className="admin-form__input"
            type="search"
            placeholder={text.searchPlaceholder}
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>
        <p className="institution-list__count">{text.count(shown.length)}</p>
      </div>

      {shown.length === 0 ? (
        <p className="admin-callout admin-callout--plain">{text.noMatch}</p>
      ) : (
        <div className="institution-list__table" role="region" aria-label={text.listLabel} tabIndex={0}>
          <table>
            <thead>
              <tr>
                <th scope="col">{columns.name}</th>
                <th scope="col">{columns.schedule}</th>
                <th scope="col">{columns.next}</th>
                <th scope="col" className="institution-list__number">
                  {columns.upcoming}
                </th>
                <th scope="col">
                  <span className="institution-list__sr-only">{columns.actions}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {shown.map((institution) => {
                const selected = institution.id === selectedId
                const [firstSchedule, ...otherSchedules] = institution.schedules
                const next = institution.nextClass
                return (
                  <tr key={institution.id} className={selected ? 'institution-list__row--selected' : undefined}>
                    <td>
                      <button type="button" className="institution-list__name" onClick={() => onEdit(institution)}>
                        {institution.name}
                      </button>
                      <span className="institution-list__sub">{institution.address}</span>
                    </td>
                    <td>
                      {firstSchedule ? scheduleLabel(firstSchedule) : text.noSchedule}
                      {otherSchedules.map((schedule) => (
                        <span key={scheduleLabel(schedule)} className="institution-list__sub">
                          {scheduleLabel(schedule)}
                        </span>
                      ))}
                    </td>
                    <td>{next ? `${shortDateLabel(next.classDate)} ${formatTime(next.startTime)}` : text.noSchedule}</td>
                    <td className="institution-list__number">
                      {text.upcomingCount(institution.upcomingClassCount)}
                      <span className="institution-list__sub">{text.totalCount(institution.classSessionCount)}</span>
                      {institution.unassignedUpcomingCount > 0 && (
                        <span className="institution-list__sub institution-list__sub--warn">
                          {text.unassignedCount(institution.unassignedUpcomingCount)}
                        </span>
                      )}
                    </td>
                    <td>
                      <div className="institution-list__actions">
                        <button
                          type="button"
                          className="admin-form__button admin-form__button--ghost admin-form__button--sm"
                          aria-label={`${institution.name} ${text.addClass}`}
                          onClick={() => onAddClass(institution)}
                        >
                          {text.addClass}
                        </button>
                        <button
                          type="button"
                          className="admin-form__button admin-form__button--ghost admin-form__button--sm"
                          aria-label={text.editTitle(institution.name)}
                          onClick={() => onEdit(institution)}
                        >
                          {text.edit}
                        </button>
                        <button
                          type="button"
                          className="admin-form__button admin-form__button--danger admin-form__button--sm"
                          aria-label={`${institution.name} ${text.delete}`}
                          onClick={() => onDelete(institution)}
                        >
                          {text.delete}
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      <ul className="institution-list__footnotes">
        {text.footnotes.map((note) => (
          <li key={note}>{note}</li>
        ))}
      </ul>
    </div>
  )
}

export default InstitutionList
