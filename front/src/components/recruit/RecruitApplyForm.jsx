import { useState } from 'react'
import { Link } from 'react-router-dom'
import Button from '../common/Button'
import {
  recruitDone,
  recruitFileRules,
  recruitPrivacyNotice,
  recruitSubmit,
  recruitTemplate,
  recruitUpload,
} from '../../data/recruitContent'
import './RecruitApplyForm.css'

function DownloadIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 4v11" />
      <path d="M7 10l5 5 5-5" />
      <path d="M5 20h14" />
    </svg>
  )
}

function UploadIcon() {
  return (
    <svg className="recruit-apply__drop-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 16V5" />
      <path d="M7 10l5-5 5 5" />
      <path d="M5 19h14" />
    </svg>
  )
}

function CheckIcon({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M5 12.5l4.5 4.5L19 7.5" />
    </svg>
  )
}

// 형식·용량이 맞지 않으면 안내 문구, 맞으면 null
function validateFile(file) {
  const extension = file.name.split('.').pop()?.toLowerCase() ?? ''
  if (!recruitFileRules.extensions.includes(extension)) return recruitUpload.invalidType
  if (file.size > recruitFileRules.maxBytes) return recruitUpload.tooLarge
  return null
}

// 강사 지원 (SCR-007): 양식 내려받기 → 작성한 지원서 올리기 → 개인정보 동의(FR-023) → 제출(FR-007)
// onSubmit(file, privacyAgreed)는 Promise를 돌려준다. 실패하면 오류 안내를 띄운다
function RecruitApplyForm({ onSubmit }) {
  const [file, setFile] = useState(null)
  const [fileError, setFileError] = useState('')
  const [agreed, setAgreed] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [done, setDone] = useState(false)

  const ready = file !== null && agreed && !submitting

  const handleFileChange = (event) => {
    const picked = event.target.files?.[0]
    setSubmitError('')
    if (!picked) {
      setFile(null)
      setFileError('')
      return
    }
    const error = validateFile(picked)
    setFile(error ? null : picked)
    setFileError(error ?? '')
    // 같은 파일을 다시 골라도 change가 일어나도록 비운다
    if (error) event.target.value = ''
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!ready) return
    setSubmitting(true)
    setSubmitError('')
    try {
      await onSubmit(file, agreed)
      setDone(true)
    } catch {
      setSubmitError(recruitSubmit.errorMessage)
    } finally {
      setSubmitting(false)
    }
  }

  if (done) {
    return (
      <div className="recruit-apply__done" role="status">
        <span className="recruit-apply__done-icon">
          <CheckIcon size={34} />
        </span>
        <h3 className="recruit-apply__done-title">{recruitDone.title}</h3>
        <p className="recruit-apply__done-description">
          {recruitDone.description.map((line) => (
            <span key={line}>{line}</span>
          ))}
        </p>
        <Button variant="ghost" size="lg" to="/">
          {recruitDone.home}
        </Button>
      </div>
    )
  }

  return (
    <form className="recruit-apply" onSubmit={handleSubmit} noValidate>
      <div className="recruit-apply__steps">
        <div className="recruit-apply__card">
          <span className="recruit-apply__step">{recruitTemplate.step}</span>
          <h3 className="recruit-apply__card-title">{recruitTemplate.title}</h3>
          <p className="recruit-apply__card-description">{recruitTemplate.description}</p>
          {recruitTemplate.href ? (
            <>
              <a className="recruit-apply__download" href={recruitTemplate.href} download>
                <DownloadIcon />
                {recruitTemplate.button}
              </a>
              {recruitTemplate.fileLabel && <span className="recruit-apply__meta">{recruitTemplate.fileLabel}</span>}
            </>
          ) : (
            <p className="recruit-apply__meta">{recruitTemplate.pending}</p>
          )}
        </div>

        <div className="recruit-apply__card">
          <span className="recruit-apply__step">{recruitUpload.step}</span>
          <h3 className="recruit-apply__card-title">
            <label htmlFor="recruit-file">{recruitUpload.title}</label>{' '}
            <span className="recruit-apply__required">{recruitUpload.required}</span>
          </h3>
          <p className="recruit-apply__card-description">{recruitUpload.description}</p>
          <div className="recruit-apply__drop">
            <UploadIcon />
            <input
              id="recruit-file"
              className="recruit-apply__file"
              type="file"
              accept={recruitFileRules.accept}
              aria-describedby="recruit-file-message"
              aria-invalid={fileError ? true : undefined}
              onChange={handleFileChange}
            />
          </div>
          <div id="recruit-file-message" aria-live="polite">
            {file && (
              <p className="recruit-apply__file-name">
                <CheckIcon />
                {file.name}
              </p>
            )}
            {fileError && <p className="recruit-apply__error">{fileError}</p>}
          </div>
        </div>
      </div>

      <div className="recruit-apply__card recruit-apply__privacy">
        <h3 className="recruit-apply__privacy-title">{recruitPrivacyNotice.title}</h3>
        <dl className="recruit-apply__privacy-list">
          {recruitPrivacyNotice.items.map((item) => (
            <div key={item.term} className="recruit-apply__privacy-item">
              <dt className="recruit-apply__privacy-term">{item.term}</dt>
              <dd className="recruit-apply__privacy-description">{item.description}</dd>
            </div>
          ))}
        </dl>
        <p className="recruit-apply__hint">
          {recruitPrivacyNotice.refusal}{' '}
          <Link className="recruit-apply__link" to={recruitPrivacyNotice.policyLink.to}>
            {recruitPrivacyNotice.policyLink.label}
          </Link>
        </p>
        <label className="recruit-apply__agree">
          <input type="checkbox" checked={agreed} onChange={(event) => setAgreed(event.target.checked)} />
          <span>
            {recruitPrivacyNotice.agree}{' '}
            <span className="recruit-apply__required">{recruitPrivacyNotice.required}</span>
          </span>
        </label>
      </div>

      <div className="recruit-apply__actions">
        <Button
          type="submit"
          variant="blue"
          size="lg"
          className="recruit-apply__submit"
          disabled={!ready}
          aria-describedby={ready ? undefined : 'recruit-submit-hint'}
        >
          {submitting ? recruitSubmit.submitting : recruitSubmit.label}
        </Button>
        {!ready && !submitting && (
          <p id="recruit-submit-hint" className="recruit-apply__hint">
            {recruitSubmit.hint}
          </p>
        )}
        {submitError && (
          <p className="recruit-apply__error" role="alert">
            {submitError}
          </p>
        )}
      </div>
    </form>
  )
}

export default RecruitApplyForm
