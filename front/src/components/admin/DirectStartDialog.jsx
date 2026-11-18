import { useEffect, useState } from 'react'
import { directStartDialog, longDateLabel } from '../../data/attendanceAdminContent'
import './AdminForm.css'
import './DirectStartDialog.css'

// 관리자 하루 시작·종료 직접 등록 (EMP-FR-020). type: 'start' | 'end', item: AdminWorkDayResponse.
// 사유를 적어 onSubmit(reason). busy면 버튼을 잠근다
function DirectStartDialog({ type, item, summary, busy, onSubmit, onClose }) {
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [onClose])

  const submit = (event) => {
    event.preventDefault()
    if (!reason.trim()) {
      setError(directStartDialog.required)
      return
    }
    onSubmit(reason.trim())
  }

  return (
    <div className="direct-start-dialog" onClick={onClose}>
      <form
        className="direct-start-dialog__box"
        role="dialog"
        aria-modal="true"
        aria-labelledby="direct-start-title"
        onClick={(event) => event.stopPropagation()}
        onSubmit={submit}
      >
        <h2 id="direct-start-title" className="direct-start-dialog__title">
          {directStartDialog.titles[type]}
        </h2>
        <dl className="admin-fields">
          <dt>{directStartDialog.staff}</dt>
          <dd>{item.staffName}</dd>
          <dt>{directStartDialog.day}</dt>
          <dd>
            {longDateLabel(item.day.date)}
            <br />
            {summary}
          </dd>
        </dl>
        <div className="admin-form__field">
          <label className="admin-form__label" htmlFor="direct-start-reason">
            {directStartDialog.reason}
          </label>
          <textarea
            id="direct-start-reason"
            className="admin-form__input"
            maxLength={200}
            placeholder={directStartDialog.placeholder}
            value={reason}
            autoFocus
            onChange={(event) => {
              setReason(event.target.value)
              setError('')
            }}
          />
          <span className="admin-form__hint">{directStartDialog.hint}</span>
          {error && (
            <p className="admin-form__error" role="alert">
              {error}
            </p>
          )}
        </div>
        <div className="admin-form__actions">
          <button type="button" className="admin-form__button admin-form__button--ghost" onClick={onClose}>
            {directStartDialog.cancel}
          </button>
          <button type="submit" className="admin-form__button admin-form__button--dark" disabled={busy}>
            {directStartDialog.submit}
          </button>
        </div>
      </form>
    </div>
  )
}

export default DirectStartDialog
