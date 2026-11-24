import { useState } from 'react'
import { classSessionAdmin, longDateLabel, workCalendarAdmin } from '../../data/attendanceAdminContent'
import { formatTimeRange } from '../attendance/attendanceFormat'
import './AdminForm.css'
import './UnassignedDayList.css'

// 근무 캘린더 "미정" 칸을 눌렀을 때 그날 직원 미정 수업 (EMP-FR-021). 수업마다 직원을 골라 onAssign(classSessionId, staffId).
// items: 그날 미정 AdminClassSessionResponse[], busyId인 줄은 버튼을 잠근다
function UnassignedDayList({ date, items, staffs, busyId, onAssign }) {
  const [choices, setChoices] = useState({})
  const title = workCalendarAdmin.unassignedTitle(longDateLabel(date))

  return (
    <div className="admin-card unassigned-day" role="group" aria-label={title}>
      <h2 className="admin-card__title">{title}</h2>
      <p className="unassigned-day__lead">{workCalendarAdmin.unassignedLead}</p>
      {items.length === 0 ? (
        <p className="admin-callout admin-callout--plain">{workCalendarAdmin.unassignedEmpty}</p>
      ) : (
        <ul className="unassigned-day__list">
          {items.map(({ classSession }) => {
            const id = classSession.id
            const label = [classSession.institutionName, classSession.target].filter(Boolean).join(' ')
            const selectId = `unassigned-staff-${id}`
            return (
              <li key={id} className="unassigned-day__item">
                <div className="unassigned-day__class">
                  <strong>{formatTimeRange(classSession)}</strong>
                  <span>{label}</span>
                </div>
                <div className="unassigned-day__assign">
                  <label className="unassigned-day__sr-only" htmlFor={selectId}>
                    {`${label} ${classSessionAdmin.assignStaff}`}
                  </label>
                  <select
                    id={selectId}
                    className="admin-form__input"
                    value={choices[id] ?? ''}
                    onChange={(event) => setChoices((current) => ({ ...current, [id]: event.target.value }))}
                  >
                    <option value="">{classSessionAdmin.choose}</option>
                    {staffs.map((staff) => (
                      <option key={staff.id} value={staff.id}>
                        {staff.name}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    className="admin-form__button admin-form__button--dark"
                    disabled={!choices[id] || busyId === id}
                    onClick={() => onAssign(id, Number(choices[id]))}
                  >
                    {workCalendarAdmin.unassignedAssign}
                  </button>
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}

export default UnassignedDayList
