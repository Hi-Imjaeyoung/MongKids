import { useEffect, useState } from 'react'
import { getInstitutionDeletePreview } from '../../api/admin'
import { institutionAdmin } from '../../data/attendanceAdminContent'
import './AdminForm.css'
import './InstitutionDeleteConfirm.css'

// 기관 삭제 확인 (EMP-FR-012). 함께 지워질 수업·기록·사진 수를 보여 주고, 동의해야 삭제 버튼이 켜진다.
// 기관 모달 안에서 등록·수정 폼 대신 보여 준다. onConfirm(), onCancel()
function InstitutionDeleteConfirm({ institution, busy, onConfirm, onCancel }) {
  const [preview, setPreview] = useState({ status: 'loading', data: null, error: '' })
  const [agreed, setAgreed] = useState(false)
  const text = institutionAdmin.deleteDialog

  useEffect(() => {
    let ignore = false
    getInstitutionDeletePreview(institution.id)
      .then((data) => {
        if (!ignore) setPreview({ status: 'ready', data, error: '' })
      })
      .catch((error) => {
        if (!ignore) setPreview({ status: 'error', data: null, error: error.message })
      })
    return () => {
      ignore = true
    }
  }, [institution.id])

  const counts = preview.data
  const title = text.title(institution.name)

  return (
    <div className="admin-card institution-delete" role="group" aria-label={title}>
      <div className="admin-card__head">
        <h2 className="admin-card__title">{title}</h2>
      </div>
      <p className="institution-delete__lead">{text.lead}</p>

      {preview.status === 'loading' && <p className="admin-callout admin-callout--info">{text.loading}</p>}
      {preview.status === 'error' && (
        <p className="admin-form__error" role="alert">
          {preview.error}
        </p>
      )}
      {counts && (
        <dl className="admin-fields institution-delete__counts">
          <dt>{text.sessions}</dt>
          <dd>{text.sessionCount(counts.classSessionCount)}</dd>
          <dt>{text.workDays}</dt>
          <dd>
            {text.workDayCount(counts.workDayCount)}
            {counts.workDayCount > 0 && <span className="institution-delete__hint">{text.workDayHint}</span>}
          </dd>
          <dt>{text.notes}</dt>
          <dd>
            {text.noteCount(counts.noteCount)}
            {counts.noteCount > 0 && <span className="institution-delete__hint">{text.noteHint}</span>}
          </dd>
          <dt>{text.images}</dt>
          <dd>{text.imageCount(counts.imageCount)}</dd>
          {counts.classRecordCount > 0 && (
            <>
              <dt>{text.legacy}</dt>
              <dd>{text.legacyCount(counts.classRecordCount)}</dd>
            </>
          )}
        </dl>
      )}

      <p className="admin-callout admin-callout--warn">{text.warning}</p>
      <label className="institution-delete__agree">
        <input
          type="checkbox"
          className="institution-delete__agree-input"
          checked={agreed}
          disabled={!counts}
          onChange={(event) => setAgreed(event.target.checked)}
        />
        {text.agree}
      </label>

      <div className="admin-form__actions">
        <button type="button" className="admin-form__button admin-form__button--ghost" onClick={onCancel}>
          {text.cancel}
        </button>
        <button
          type="button"
          className="admin-form__button admin-form__button--danger"
          disabled={!counts || !agreed || busy}
          onClick={onConfirm}
        >
          {busy ? text.deleting : text.submit}
        </button>
      </div>
    </div>
  )
}

export default InstitutionDeleteConfirm
