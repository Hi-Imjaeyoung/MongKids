import { useEffect, useState } from 'react'
import { getInstitutionDeletePreview } from '../../api/admin'
import { institutionAdmin } from '../../data/attendanceAdminContent'
import './AdminForm.css'
import './InstitutionDeleteConfirm.css'

// 기관 삭제 확인 (EMP-FR-012, EMP-SCR-006 11-18 초안). 함께 지워질 수업·기록·사진 수를 보여 주고,
// 동의해야 삭제 버튼이 켜진다. 기관 모달 안에서 보여 준다. onConfirm(), onCancel().
// fromForm이면 수정 폼에서 넘어온 것이라 취소 버튼이 "돌아가기"다
function InstitutionDeleteConfirm({ institution, busy, fromForm = false, onConfirm, onCancel }) {
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
  const rows = counts
    ? [
        { label: text.sessions, value: text.sessionCount(counts.classSessionCount) },
        { label: text.workDays, value: text.workDayCount(counts.workDayCount), hint: counts.workDayCount > 0 && text.workDayHint },
        { label: text.notes, value: text.noteCount(counts.noteCount) },
        { label: text.images, value: text.imageCount(counts.imageCount) },
        counts.classRecordCount > 0 && { label: text.legacy, value: text.legacyCount(counts.classRecordCount) },
      ].filter(Boolean)
    : []

  return (
    <div className="admin-card institution-delete" role="group" aria-label={title}>
      <h2 className="admin-card__title">{title}</h2>
      <p className="institution-delete__lead">{text.lead}</p>

      {preview.status === 'loading' && <p className="admin-callout admin-callout--info">{text.loading}</p>}
      {preview.status === 'error' && (
        <p className="admin-form__error" role="alert">
          {preview.error}
        </p>
      )}
      {counts && (
        <ul className="institution-delete__list">
          {rows.map((row) => (
            <li key={row.label} className="institution-delete__item">
              <span>
                {row.label}
                {row.hint && <span className="institution-delete__hint">{row.hint}</span>}
              </span>
              <strong>{row.value}</strong>
            </li>
          ))}
        </ul>
      )}

      <p className="institution-delete__note">{text.warning}</p>
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
          {fromForm ? text.back : text.cancel}
        </button>
        <button
          type="button"
          className="admin-form__button institution-delete__submit"
          disabled={!counts || !agreed || busy}
          onClick={onConfirm}
        >
          {busy ? text.deleting : text.submit(counts?.classSessionCount ?? 0)}
        </button>
      </div>
    </div>
  )
}

export default InstitutionDeleteConfirm
