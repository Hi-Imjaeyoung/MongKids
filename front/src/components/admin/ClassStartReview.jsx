import { useState } from 'react'
import { classStartAdmin, longDateLabel } from '../../data/attendanceAdminContent'
import { formatClock, formatTimeRange } from '../attendance/attendanceFormat'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import StartLocation from './StartLocation'
import './AdminForm.css'

// 승인 대기 수업 시작 한 건의 승인·반려 (EMP-FR-011). busy면 버튼을 잠근다.
// onApprove(), onReject(reason)
function ClassStartReview({ item, busy, onApprove, onReject }) {
  const { classSession } = item
  const { start } = classSession
  const [rejectReason, setRejectReason] = useState('')
  const [error, setError] = useState('')

  const reject = () => {
    if (!rejectReason.trim()) {
      setError(classStartAdmin.rejectRequired)
      return
    }
    onReject(rejectReason.trim())
  }

  return (
    <section className="admin-card" aria-label={`${item.staffName} ${classSession.target ?? ''}`}>
      <div className="admin-card__head">
        <h2 className="admin-card__title">
          {item.staffName} · {classSession.target ?? classSession.institutionName}
        </h2>
        <ClassStatusBadge status={classSession.status} />
      </div>
      <dl className="admin-fields">
        <dt>{classStartAdmin.fields.institution}</dt>
        <dd>
          {classSession.institutionName}
          <br />
          <span className="admin-card__sub">{classSession.institutionAddress}</span>
        </dd>
        <dt>{classStartAdmin.fields.classTime}</dt>
        <dd>
          {longDateLabel(classSession.classDate)} {formatTimeRange(classSession)}
        </dd>
        <dt>{classStartAdmin.fields.registeredAt}</dt>
        <dd>{formatClock(start.startedAt)}</dd>
        <dt>{classStartAdmin.fields.distance}</dt>
        <dd className="admin-inline-list">
          <StartLocation start={start} detail />
        </dd>
      </dl>
      <div className="admin-form__field">
        <span className="admin-form__label">{classStartAdmin.reason}</span>
        <p className="admin-callout admin-callout--plain">{start.reason}</p>
      </div>
      <button type="button" className="admin-form__button admin-form__button--blue" disabled={busy} onClick={onApprove}>
        {classStartAdmin.approve}
      </button>
      <div className="admin-card__divider" />
      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="reject-reason">
          {classStartAdmin.rejectReason}
        </label>
        <textarea
          id="reject-reason"
          className="admin-form__input"
          maxLength={200}
          placeholder={classStartAdmin.rejectPlaceholder}
          value={rejectReason}
          onChange={(event) => {
            setRejectReason(event.target.value)
            setError('')
          }}
        />
        <span className="admin-form__hint">{classStartAdmin.rejectHint}</span>
        {error && (
          <p className="admin-form__error" role="alert">
            {error}
          </p>
        )}
      </div>
      <button type="button" className="admin-form__button admin-form__button--danger" disabled={busy} onClick={reject}>
        {classStartAdmin.reject}
      </button>
    </section>
  )
}

export default ClassStartReview
