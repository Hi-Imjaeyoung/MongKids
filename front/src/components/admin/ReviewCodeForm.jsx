import { useState } from 'react'
import { reviewCodeAdmin as text, reviewCodeKinds } from '../../data/adminContent'
import { addDays, toIsoDate } from './attendanceAdminFormat'
import './AdminForm.css'
import './ReviewCodeForm.css'

// 후기 작성 코드 발급 (FR-029). 사용 횟수·배포처 메모는 묶음만 받는다.
// onIssue(body)는 발급한 코드(ReviewCodeResponse)를 돌려주는 Promise. 실패하면 오류 문구를 보여 준다
function ReviewCodeForm({ onIssue }) {
  const today = toIsoDate(new Date())
  const [form, setForm] = useState(() => ({
    kind: 'SINGLE',
    maxUses: String(text.defaultMaxUses),
    expiresOn: addDays(today, text.defaultExpiryDays),
    memo: '',
  }))
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const [issued, setIssued] = useState(null)
  const [copied, setCopied] = useState(false)
  const bundle = form.kind === 'BUNDLE'

  const change = (patch) => setForm((current) => ({ ...current, ...patch }))

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const code = await onIssue({
        kind: form.kind,
        expiresOn: form.expiresOn,
        ...(bundle && { maxUses: Number(form.maxUses), memo: form.memo.trim() || null }),
      })
      setIssued(code.code)
      setCopied(false)
      change({ memo: '' })
    } catch (issueError) {
      setError(issueError.message)
    } finally {
      setBusy(false)
    }
  }

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(issued)
      setCopied(true)
    } catch {
      setCopied(false)
    }
  }

  return (
    <form className="admin-card review-code-form" aria-labelledby="review-code-form-title" onSubmit={submit}>
      <h2 id="review-code-form-title" className="admin-card__title">
        {text.formTitle}
      </h2>

      <fieldset className="review-code-form__kinds">
        <legend className="admin-form__label review-code-form__legend">{text.kind}</legend>
        {reviewCodeKinds.map((kind) => (
          <label
            key={kind.id}
            className={`review-code-form__kind ${form.kind === kind.id ? 'review-code-form__kind--active' : ''}`}
          >
            <input
              type="radio"
              name="review-code-kind"
              value={kind.id}
              checked={form.kind === kind.id}
              onChange={() => change({ kind: kind.id })}
            />
            <span className="review-code-form__kind-text">
              <span className="review-code-form__kind-label">{kind.label}</span>
              <span className="admin-form__hint">{kind.description}</span>
            </span>
          </label>
        ))}
      </fieldset>

      <div className="review-code-form__row">
        {bundle && (
          <div className="admin-form__field">
            <label className="admin-form__label" htmlFor="review-code-max-uses">
              {text.maxUses}
            </label>
            <input
              id="review-code-max-uses"
              className="admin-form__input"
              type="number"
              min="1"
              max={text.maxMaxUses}
              required
              value={form.maxUses}
              onChange={(event) => change({ maxUses: event.target.value })}
            />
          </div>
        )}
        <div className="admin-form__field">
          <label className="admin-form__label" htmlFor="review-code-expires-on">
            {text.expiresOn}
          </label>
          <input
            id="review-code-expires-on"
            className="admin-form__input"
            type="date"
            min={today}
            max={addDays(today, text.maxExpiryDays)}
            required
            aria-describedby="review-code-expires-hint"
            value={form.expiresOn}
            onChange={(event) => change({ expiresOn: event.target.value })}
          />
        </div>
      </div>
      <span id="review-code-expires-hint" className="admin-form__hint">
        {text.expiresHint}
      </span>

      {bundle && (
        <div className="admin-form__field">
          <label className="admin-form__label" htmlFor="review-code-memo">
            {text.memo}
          </label>
          <input
            id="review-code-memo"
            className="admin-form__input"
            type="text"
            maxLength={text.memoMaxLength}
            placeholder={text.memoPlaceholder}
            aria-describedby="review-code-memo-hint"
            value={form.memo}
            onChange={(event) => change({ memo: event.target.value })}
          />
          <span id="review-code-memo-hint" className="admin-form__hint">
            {text.memoHint}
          </span>
        </div>
      )}

      {error && (
        <p className="admin-form__error" role="alert">
          {error}
        </p>
      )}
      <button type="submit" className="admin-form__button admin-form__button--blue" disabled={busy}>
        {busy ? text.issuing : text.issue}
      </button>

      <div role="status">
        {issued && (
          <div className="review-code-form__issued">
            <span className="review-code-form__issued-text">{text.issued}</span>
            <div className="review-code-form__issued-row">
              <code className="review-code-form__issued-code">{issued}</code>
              <button type="button" className="admin-form__button admin-form__button--ghost" onClick={copy}>
                {copied ? text.copied : text.copy}
              </button>
            </div>
          </div>
        )}
      </div>
    </form>
  )
}

export default ReviewCodeForm
