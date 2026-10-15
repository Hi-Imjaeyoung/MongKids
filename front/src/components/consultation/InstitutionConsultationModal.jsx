import { useEffect, useState } from 'react'
import { createInstitutionConsultation } from '../../api/consultations'
import { ApiError } from '../../api/client'
import Button from '../common/Button'
import { ChipGroup, FieldError } from './ConsultationFormParts'
import {
  childAgeGroups,
  consultationDone,
  consultationFields,
  consultationRules,
  fieldErrorMessages,
  institutionConsultation,
  preferredDays,
  privacyNotice,
} from '../../data/consultationContent'
import './InstitutionConsultationModal.css'

const initialForm = {
  institutionName: '',
  preferredDays: [],
  preferredTime: '',
  childAgeGroups: [],
  childCount: '',
  phone: '',
  note: '',
  privacyAgreed: false,
}

function toggleValue(list, value) {
  return list.includes(value) ? list.filter((item) => item !== value) : [...list, value]
}

function isValidChildCount(value) {
  const count = Number(value)
  return Number.isInteger(count) && count >= consultationRules.childCountMin && count <= consultationRules.childCountMax
}

function isValidPhone(value) {
  return consultationRules.phonePattern.test(value.trim())
}

// 필수 항목을 모두 채우고 개인정보 수집에 동의해야 제출할 수 있다 (FR-001, FR-023)
function canSubmit(form) {
  return (
    form.institutionName.trim() !== '' &&
    form.preferredDays.length > 0 &&
    form.preferredTime.trim() !== '' &&
    form.childAgeGroups.length > 0 &&
    isValidChildCount(form.childCount) &&
    isValidPhone(form.phone) &&
    form.privacyAgreed
  )
}

// 기관 상담 신청 모달 (SCR-009, FR-001). 비회원도 신청할 수 있다 (FR-003)
function InstitutionConsultationModal({ onClose }) {
  const [form, setForm] = useState(initialForm)
  const [phoneTouched, setPhoneTouched] = useState(false)
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)

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

  const update = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => ({ ...current, [field]: undefined }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!canSubmit(form) || submitting) return

    setSubmitting(true)
    setError('')
    try {
      await createInstitutionConsultation({
        ...form,
        institutionName: form.institutionName.trim(),
        preferredTime: form.preferredTime.trim(),
        childCount: Number(form.childCount),
        phone: form.phone.trim(),
        note: form.note.trim() || null,
      })
      setDone(true)
    } catch (caught) {
      if (caught instanceof ApiError && caught.fieldErrors.length > 0) {
        setFieldErrors(Object.fromEntries(
          caught.fieldErrors.map(({ field }) => [field, fieldErrorMessages[field] ?? fieldErrorMessages.default]),
        ))
      } else {
        setError(institutionConsultation.errorMessage)
      }
    } finally {
      setSubmitting(false)
    }
  }

  const phoneError = fieldErrors.phone
    ?? (phoneTouched && form.phone.trim() !== '' && !isValidPhone(form.phone) ? fieldErrorMessages.phone : undefined)
  const childCountError = fieldErrors.childCount
    ?? (form.childCount !== '' && !isValidChildCount(form.childCount) ? fieldErrorMessages.childCount : undefined)
  const { institutionName, schedule, children, phone, note } = consultationFields

  return (
    <div className="consult-modal" onClick={onClose}>
      <div
        className={`consult-modal__dialog ${done ? 'consult-modal__dialog--done' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="consult-modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <button type="button" className="consult-modal__close" aria-label="닫기" onClick={onClose}>
          ✕
        </button>

        {done ? (
          <div className="consult-modal__done">
            <span className="consult-modal__done-icon" aria-hidden="true">✓</span>
            <h2 id="consult-modal-title" className="consult-modal__title">{consultationDone.title}</h2>
            <p className="consult-modal__description consult-modal__description--lines">
              {consultationDone.description.map((line) => (
                <span key={line}>{line}</span>
              ))}
            </p>
            <Button variant="yellow" size="lg" className="consult-modal__submit" onClick={onClose}>
              {consultationDone.confirm}
            </Button>
          </div>
        ) : (
          <>
            <div className="consult-modal__head">
              <span className="consult-modal__badge">{institutionConsultation.badge}</span>
              <h2 id="consult-modal-title" className="consult-modal__title">{institutionConsultation.title}</h2>
              <p className="consult-modal__description">{institutionConsultation.description}</p>
            </div>

            <form className="consult-modal__form" onSubmit={handleSubmit} noValidate>
              <div className="consult-modal__field">
                <label htmlFor="consult-institution-name" className="consult-modal__label">
                  {institutionName.label} <span className="consult-modal__required">*</span>
                </label>
                <input
                  id="consult-institution-name"
                  className="consult-modal__input"
                  maxLength={consultationRules.institutionNameMax}
                  placeholder={institutionName.placeholder}
                  value={form.institutionName}
                  onChange={(event) => update('institutionName', event.target.value)}
                />
                <FieldError message={fieldErrors.institutionName} />
              </div>

              <fieldset className="consult-modal__field">
                <legend className="consult-modal__label">
                  {schedule.label} <span className="consult-modal__required">*</span>
                </legend>
                <ChipGroup
                  options={preferredDays}
                  selected={form.preferredDays}
                  onToggle={(value) => update('preferredDays', toggleValue(form.preferredDays, value))}
                />
                <input
                  className="consult-modal__input"
                  aria-label={schedule.timeLabel}
                  maxLength={consultationRules.preferredTimeMax}
                  placeholder={schedule.timePlaceholder}
                  value={form.preferredTime}
                  onChange={(event) => update('preferredTime', event.target.value)}
                />
                <FieldError message={fieldErrors.preferredDays ?? fieldErrors.preferredTime} />
              </fieldset>

              <fieldset className="consult-modal__field">
                <legend className="consult-modal__label">
                  {children.label} <span className="consult-modal__required">*</span>
                </legend>
                <ChipGroup
                  options={childAgeGroups}
                  selected={form.childAgeGroups}
                  onToggle={(value) => update('childAgeGroups', toggleValue(form.childAgeGroups, value))}
                />
                <div className="consult-modal__count">
                  <label htmlFor="consult-child-count" className="consult-modal__count-label">{children.countLabel}</label>
                  <input
                    id="consult-child-count"
                    className="consult-modal__input consult-modal__input--count"
                    type="number"
                    inputMode="numeric"
                    min={consultationRules.childCountMin}
                    max={consultationRules.childCountMax}
                    placeholder="0"
                    value={form.childCount}
                    onChange={(event) => update('childCount', event.target.value)}
                  />
                  <span>{children.countUnit}</span>
                </div>
                <FieldError message={fieldErrors.childAgeGroups ?? childCountError} />
              </fieldset>

              <div className="consult-modal__field">
                <label htmlFor="consult-phone" className="consult-modal__label">
                  {phone.label} <span className="consult-modal__required">*</span>
                </label>
                <input
                  id="consult-phone"
                  className={`consult-modal__input ${phoneError ? 'consult-modal__input--error' : ''}`}
                  type="tel"
                  inputMode="tel"
                  autoComplete="tel"
                  placeholder={phone.placeholder}
                  value={form.phone}
                  aria-invalid={Boolean(phoneError)}
                  onChange={(event) => update('phone', event.target.value)}
                  onBlur={() => setPhoneTouched(true)}
                />
                <FieldError message={phoneError} />
              </div>

              <div className="consult-modal__field">
                <label htmlFor="consult-note" className="consult-modal__label">
                  {note.label} <span className="consult-modal__optional">{note.optional}</span>
                </label>
                <textarea
                  id="consult-note"
                  className="consult-modal__input consult-modal__textarea"
                  rows={3}
                  maxLength={consultationRules.noteMax}
                  placeholder={note.placeholder}
                  value={form.note}
                  onChange={(event) => update('note', event.target.value)}
                />
                <p className="consult-modal__hint">{note.hint}</p>
                <FieldError message={fieldErrors.note} />
              </div>

              <div className="consult-modal__privacy">
                <strong className="consult-modal__privacy-title">{privacyNotice.title}</strong>
                <dl className="consult-modal__privacy-list">
                  {privacyNotice.items.map((item) => (
                    <div key={item.term} className="consult-modal__privacy-item">
                      <dt>{item.term}</dt>
                      <dd>{item.description}</dd>
                    </div>
                  ))}
                </dl>
                <p className="consult-modal__hint">{privacyNotice.refusal}</p>
                <label className="consult-modal__agree">
                  <input
                    type="checkbox"
                    checked={form.privacyAgreed}
                    onChange={(event) => update('privacyAgreed', event.target.checked)}
                  />
                  {privacyNotice.agree} <span className="consult-modal__required">{privacyNotice.required}</span>
                </label>
                <FieldError message={fieldErrors.privacyAgreed} />
              </div>

              {error && <p className="consult-modal__error" role="alert">{error}</p>}

              <Button
                variant="yellow"
                size="lg"
                type="submit"
                className="consult-modal__submit"
                disabled={!canSubmit(form) || submitting}
              >
                {submitting ? institutionConsultation.submitting : institutionConsultation.submit}
              </Button>
            </form>
          </>
        )}
      </div>
    </div>
  )
}

export default InstitutionConsultationModal
