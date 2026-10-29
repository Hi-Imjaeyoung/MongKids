import { useState } from 'react'
import { manualStart } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import AttendanceScreen from './AttendanceScreen'
import ClassSummary from './ClassSummary'
import './ManualStartForm.css'

// ④ 수업 시작 수동 등록 (EMP-FR-011). measured: 위치를 받았으면 { distance, accuracy }
function ManualStartForm({ classSession, measured, submitting, error, onSubmit, onBack }) {
  const [reason, setReason] = useState('')
  const canSubmit = reason.trim().length > 0 && !submitting

  const submit = (event) => {
    event.preventDefault()
    if (canSubmit) onSubmit(reason.trim())
  }

  return (
    <form onSubmit={submit}>
      <AttendanceScreen
        title={manualStart.title}
        onBack={onBack}
        footer={
          <AttendanceAction type="submit" disabled={!canSubmit}>
            {submitting ? manualStart.submitting : manualStart.submit}
          </AttendanceAction>
        }
      >
        <ClassSummary classSession={classSession} label={manualStart.classLabel}>
          <span className="class-summary__extra">
            {measured ? manualStart.measured(measured.distance, measured.accuracy) : manualStart.notMeasured}
          </span>
        </ClassSummary>

        <div className="attendance-field">
          <label htmlFor="manual-reason" className="attendance-field__label">
            {manualStart.reasonLabel} <span className="attendance-field__required">{manualStart.required}</span>
          </label>
          <textarea
            id="manual-reason"
            className="attendance-field__textarea"
            rows={5}
            maxLength={manualStart.reasonMaxLength}
            placeholder={manualStart.reasonPlaceholder}
            value={reason}
            onChange={(event) => setReason(event.target.value)}
            required
          />
          <span className="attendance-field__count">
            {reason.length} / {manualStart.reasonMaxLength}
          </span>
        </div>

        <p className="manual-start__notice">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
            <circle cx="12" cy="12" r="9" />
            <path d="M12 8h.01" />
            <path d="M11 12h1v5h1" />
          </svg>
          <span>{manualStart.notice}</span>
        </p>

        {error && (
          <p className="attendance-error" role="alert">
            {error}
          </p>
        )}
      </AttendanceScreen>
    </form>
  )
}

export default ManualStartForm
