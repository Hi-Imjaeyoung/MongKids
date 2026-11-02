import { useState } from 'react'
import { classSessionAdmin, daysOfWeek, longDateLabel } from '../../data/attendanceAdminContent'
import { repeatDates, toTimeInput } from './attendanceAdminFormat'
import './AdminForm.css'
import './ClassSessionForm.css'

function initialForm(editing, defaultDate) {
  if (editing) {
    const { classSession } = editing
    return {
      institutionId: String(editing.institutionId),
      staffId: String(editing.staffId),
      target: classSession.target ?? '',
      firstDate: classSession.classDate,
      lastDate: classSession.classDate,
      days: [],
      startTime: toTimeInput(classSession.startTime),
      endTime: toTimeInput(classSession.endTime),
    }
  }
  return {
    institutionId: '',
    staffId: '',
    target: '',
    firstDate: defaultDate,
    lastDate: defaultDate,
    days: [],
    startTime: '10:00',
    endTime: '10:50',
  }
}

// 수업 반복 등록과 한 회 수정 (EMP-FR-004). editing(AdminClassSessionResponse)이 있으면 그 수업 수정.
// 등록은 onCreate(body), 수정은 onUpdate(classSessionId, body). 부모가 key로 폼을 새로 만든다
function ClassSessionForm({ institutions, staffs, editing, defaultDate, busy, onCreate, onUpdate, onCancelEdit }) {
  const [form, setForm] = useState(() => initialForm(editing, defaultDate))
  const [error, setError] = useState('')
  const text = classSessionAdmin

  const change = (patch) => {
    setForm((current) => ({ ...current, ...patch }))
    setError('')
  }
  const toggleDay = (id) =>
    change({ days: form.days.includes(id) ? form.days.filter((day) => day !== id) : [...form.days, id] })

  const dayIndexes = daysOfWeek.filter((day) => form.days.includes(day.id)).map((day) => day.index)
  const dates = editing ? [form.firstDate] : repeatDates(form.firstDate, form.lastDate, dayIndexes)
  const dayLabels = daysOfWeek
    .filter((day) => form.days.includes(day.id))
    .map((day) => day.label)
    .join('·')

  const submit = (event) => {
    event.preventDefault()
    if (form.startTime >= form.endTime) {
      setError(text.invalidTime)
      return
    }
    const common = {
      institutionId: Number(form.institutionId),
      staffId: Number(form.staffId),
      startTime: form.startTime,
      endTime: form.endTime,
      target: form.target.trim() || null,
    }
    if (editing) {
      onUpdate(editing.classSession.id, { ...common, classDate: form.firstDate })
      return
    }
    if (dates.length === 0) {
      setError(text.noDates)
      return
    }
    onCreate({ ...common, firstDate: form.firstDate, lastDate: form.lastDate, daysOfWeek: form.days })
  }

  return (
    <form className="admin-card class-session-form" aria-label={text.formLabel} onSubmit={submit}>
      <h2 className="admin-card__title">{editing ? text.editTitle : text.createTitle}</h2>
      {institutions.length === 0 && <p className="admin-callout admin-callout--warn">{text.noInstitution}</p>}
      {staffs.length === 0 && <p className="admin-callout admin-callout--warn">{text.noStaff}</p>}

      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="session-institution">
          {text.institution}
        </label>
        <select
          id="session-institution"
          className="admin-form__input"
          required
          value={form.institutionId}
          onChange={(event) => change({ institutionId: event.target.value })}
        >
          <option value="">{text.choose}</option>
          {institutions.map((institution) => (
            <option key={institution.id} value={institution.id}>
              {institution.name}
            </option>
          ))}
        </select>
      </div>
      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="session-staff">
          {text.staff}
        </label>
        <select
          id="session-staff"
          className="admin-form__input"
          required
          value={form.staffId}
          onChange={(event) => change({ staffId: event.target.value })}
        >
          <option value="">{text.choose}</option>
          {staffs.map((staff) => (
            <option key={staff.id} value={staff.id}>
              {staff.name}
            </option>
          ))}
        </select>
        <span className="admin-form__hint">{text.staffHint}</span>
      </div>
      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="session-target">
          {text.target}
        </label>
        <input
          id="session-target"
          className="admin-form__input"
          maxLength={50}
          value={form.target}
          onChange={(event) => change({ target: event.target.value })}
        />
      </div>

      {editing ? (
        <div className="admin-form__field">
          <label className="admin-form__label" htmlFor="session-date">
            {text.classDate}
          </label>
          <input
            id="session-date"
            className="admin-form__input"
            type="date"
            required
            value={form.firstDate}
            onChange={(event) => change({ firstDate: event.target.value })}
          />
        </div>
      ) : (
        <>
          <fieldset className="class-session-form__group">
            <legend className="admin-form__label">{text.period}</legend>
            <div className="admin-form__pair">
              <input
                className="admin-form__input"
                type="date"
                required
                aria-label={text.firstDate}
                value={form.firstDate}
                onChange={(event) => change({ firstDate: event.target.value })}
              />
              <input
                className="admin-form__input"
                type="date"
                required
                min={form.firstDate}
                aria-label={text.lastDate}
                value={form.lastDate}
                onChange={(event) => change({ lastDate: event.target.value })}
              />
            </div>
          </fieldset>
          <fieldset className="class-session-form__group">
            <legend className="admin-form__label">{text.days}</legend>
            <div className="class-session-form__days">
              {daysOfWeek.map((day) => {
                const on = form.days.includes(day.id)
                return (
                  <button
                    key={day.id}
                    type="button"
                    className={`class-session-form__day ${on ? 'class-session-form__day--on' : ''}`}
                    aria-pressed={on}
                    onClick={() => toggleDay(day.id)}
                  >
                    {day.label}
                  </button>
                )
              })}
            </div>
          </fieldset>
        </>
      )}

      <fieldset className="class-session-form__group">
        <legend className="admin-form__label">{text.time}</legend>
        <div className="admin-form__pair">
          <input
            className="admin-form__input"
            type="time"
            required
            aria-label={text.startTime}
            value={form.startTime}
            onChange={(event) => change({ startTime: event.target.value })}
          />
          <input
            className="admin-form__input"
            type="time"
            required
            aria-label={text.endTime}
            value={form.endTime}
            onChange={(event) => change({ endTime: event.target.value })}
          />
        </div>
      </fieldset>

      {!editing && dates.length > 0 && (
        <p className="admin-callout admin-callout--info">
          {text.summary(longDateLabel(form.firstDate), longDateLabel(form.lastDate), dayLabels, dates.length)}
        </p>
      )}
      {error && (
        <p className="admin-form__error" role="alert">
          {error}
        </p>
      )}

      <div className="admin-form__actions">
        {editing && (
          <button type="button" className="admin-form__button admin-form__button--ghost" onClick={onCancelEdit}>
            {text.cancelEdit}
          </button>
        )}
        <button
          type="submit"
          className="admin-form__button admin-form__button--dark"
          disabled={busy || (!editing && dates.length === 0)}
        >
          {editing ? text.submitEdit : text.submitCreate(dates.length)}
        </button>
      </div>
    </form>
  )
}

export default ClassSessionForm
