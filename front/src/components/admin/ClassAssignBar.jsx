import { useState } from 'react'
import { classSessionAdmin } from '../../data/attendanceAdminContent'
import './AdminForm.css'
import './ClassAssignBar.css'

// 고른 직원 미정 수업을 직원 한 명에게 한꺼번에 배정 (EMP-FR-021). count: 고른 수업 수.
// onAssign(staffId), onClear(). busy면 버튼을 잠근다
function ClassAssignBar({ count, staffs, busy, onAssign, onClear }) {
  const [staffId, setStaffId] = useState('')
  const text = classSessionAdmin

  return (
    <div className="class-assign-bar" role="group" aria-label={text.assignTitle(count)}>
      <strong className="class-assign-bar__title">{text.assignTitle(count)}</strong>
      <div className="admin-form__field class-assign-bar__field">
        <label className="admin-form__label" htmlFor="class-assign-staff">
          {text.assignStaff}
        </label>
        <select
          id="class-assign-staff"
          className="admin-form__input"
          value={staffId}
          onChange={(event) => setStaffId(event.target.value)}
        >
          <option value="">{text.choose}</option>
          {staffs.map((staff) => (
            <option key={staff.id} value={staff.id}>
              {staff.name}
            </option>
          ))}
        </select>
      </div>
      <div className="class-assign-bar__actions">
        <button type="button" className="admin-form__button admin-form__button--ghost" onClick={onClear}>
          {text.assignClear}
        </button>
        <button
          type="button"
          className="admin-form__button admin-form__button--dark"
          disabled={!staffId || busy}
          onClick={() => onAssign(Number(staffId))}
        >
          {text.assignSubmit}
        </button>
      </div>
      <p className="class-assign-bar__hint">{text.assignHint}</p>
    </div>
  )
}

export default ClassAssignBar
