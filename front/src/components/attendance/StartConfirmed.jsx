import { useState } from 'react'
import { classStatuses, startConfirmed } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import AttendanceScreen from './AttendanceScreen'
import { formatClassMeta, formatClock } from './attendanceFormat'
import './LocationResult.css'
import './StartConfirmed.css'

// ⑤ 수업 시작 확정. 거리·상태 요약과 특이사항(선택, EMP-FR-002) 저장
function StartConfirmed({ classSession, saving, error, onSave, onBack }) {
  const { start } = classSession
  const [note, setNote] = useState(start.note ?? '')

  const submit = (event) => {
    event.preventDefault()
    if (!saving) onSave(note.trim())
  }

  return (
    <form onSubmit={submit}>
      <AttendanceScreen
        title={startConfirmed.title}
        onBack={onBack}
        footer={
          <>
            <AttendanceAction type="submit" disabled={saving || note.trim().length === 0}>
              {saving ? startConfirmed.saving : startConfirmed.save}
            </AttendanceAction>
            <AttendanceAction variant="outline" onClick={onBack} disabled={saving}>
              {startConfirmed.skip}
            </AttendanceAction>
          </>
        }
      >
        <div className="location-result">
          <div className="location-result__icon location-result__icon--ok">
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M5 12.5l4.5 4.5L19 7.5" />
            </svg>
          </div>
          <h2 className="location-result__heading">{startConfirmed.heading}</h2>
          <p className="location-result__message">
            {classSession.institutionName} · {formatClock(start.startedAt)}
          </p>
        </div>

        <dl className="start-confirmed__summary">
          <div className="start-confirmed__row">
            <dt>{startConfirmed.classLabel}</dt>
            <dd>{formatClassMeta(classSession)}</dd>
          </div>
          {start.distanceMeters != null && (
            <div className="start-confirmed__row">
              <dt>{startConfirmed.distanceLabel}</dt>
              <dd>{start.distanceMeters}m</dd>
            </div>
          )}
          <div className="start-confirmed__row">
            <dt>{startConfirmed.statusLabel}</dt>
            <dd className="start-confirmed__status">{classStatuses[classSession.status].label}</dd>
          </div>
        </dl>

        <div className="attendance-field">
          <label htmlFor="start-note" className="attendance-field__label">
            {startConfirmed.noteLabel} <span className="attendance-field__optional">{startConfirmed.optional}</span>
          </label>
          <textarea
            id="start-note"
            className="attendance-field__textarea"
            rows={4}
            maxLength={startConfirmed.noteMaxLength}
            placeholder={startConfirmed.notePlaceholder}
            value={note}
            onChange={(event) => setNote(event.target.value)}
          />
        </div>

        {error && (
          <p className="attendance-error" role="alert">
            {error}
          </p>
        )}
      </AttendanceScreen>
    </form>
  )
}

export default StartConfirmed
