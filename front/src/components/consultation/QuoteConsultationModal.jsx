import { useEffect, useState } from 'react'
import { createQuoteConsultation } from '../../api/consultations'
import { ApiError } from '../../api/client'
import Button from '../common/Button'
import { ChipGroup, FieldError } from './ConsultationFormParts'
import {
  participantGroups,
  quoteConsultation,
  quoteConsultationDone,
  quoteConsultationFields,
  quoteConsultationRules,
  quoteFieldErrorMessages,
  quotePrivacyNotice,
  quotePrograms,
  venueTypes,
} from '../../data/consultationContent'
import './InstitutionConsultationModal.css'
import './QuoteConsultationModal.css'

const initialForm = {
  institutionName: '',
  eventDate: '',
  eventTime: '',
  scheduleUndecided: false,
  venueType: null,
  address: '',
  participantGroups: [],
  childCount: '',
  guardianCount: '',
  programs: [],
  phone: '',
  note: '',
  privacyAgreed: false,
}

function toggleValue(list, value) {
  return list.includes(value) ? list.filter((item) => item !== value) : [...list, value]
}

// 비워 둔 인원은 괜찮고, 적었다면 1~9999명
function isValidHeadcount(value) {
  if (value === '') return true
  const count = Number(value)
  return Number.isInteger(count) && count >= quoteConsultationRules.headcountMin && count <= quoteConsultationRules.headcountMax
}

function isValidPhone(value) {
  return quoteConsultationRules.phonePattern.test(value.trim())
}

function needsAddress(form) {
  return form.venueType === 'INDOOR' || form.venueType === 'OUTDOOR'
}

// 필수 항목을 모두 채우고 개인정보 수집에 동의해야 제출할 수 있다 (FR-031, FR-023)
function canSubmit(form) {
  return (
    form.institutionName.trim() !== '' &&
    (form.scheduleUndecided || (form.eventDate !== '' && form.eventTime !== '')) &&
    form.venueType !== null &&
    (!needsAddress(form) || form.address.trim() !== '') &&
    form.participantGroups.length > 0 &&
    (form.childCount !== '' || form.guardianCount !== '') &&
    isValidHeadcount(form.childCount) &&
    isValidHeadcount(form.guardianCount) &&
    isValidPhone(form.phone) &&
    form.privacyAgreed
  )
}

function toRequest(form) {
  return {
    institutionName: form.institutionName.trim(),
    scheduleUndecided: form.scheduleUndecided,
    eventDate: form.scheduleUndecided ? null : form.eventDate,
    eventTime: form.scheduleUndecided ? null : form.eventTime,
    venueType: form.venueType,
    address: needsAddress(form) ? form.address.trim() : null,
    participantGroups: form.participantGroups,
    childCount: form.childCount === '' ? null : Number(form.childCount),
    guardianCount: form.guardianCount === '' ? null : Number(form.guardianCount),
    programs: form.programs,
    phone: form.phone.trim(),
    note: form.note.trim() || null,
    privacyAgreed: form.privacyAgreed,
  }
}

// 오늘 날짜('YYYY-MM-DD', 기기 시간대). 지난 날짜는 고를 수 없게 한다
function today() {
  const now = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

// 운동회 견적 요청 모달 (SCR-011, FR-031). 운동회·체육대회 상세(SCR-019)에서 연다. 비회원도 신청할 수 있다 (FR-003)
function QuoteConsultationModal({ onClose }) {
  const [form, setForm] = useState(initialForm)
  const [phoneTouched, setPhoneTouched] = useState(false)
  const [privacyOpen, setPrivacyOpen] = useState(false)
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
      await createQuoteConsultation(toRequest(form))
      setDone(true)
    } catch (caught) {
      if (caught instanceof ApiError && caught.fieldErrors.length > 0) {
        setFieldErrors(Object.fromEntries(
          caught.fieldErrors.map(({ field }) => [field, quoteFieldErrorMessages[field] ?? quoteFieldErrorMessages.default]),
        ))
      } else {
        setError(quoteConsultation.errorMessage)
      }
    } finally {
      setSubmitting(false)
    }
  }

  const phoneError = fieldErrors.phone
    ?? (phoneTouched && form.phone.trim() !== '' && !isValidPhone(form.phone) ? quoteFieldErrorMessages.phone : undefined)
  const headcountError = fieldErrors.childCount ?? fieldErrors.guardianCount
    ?? (!isValidHeadcount(form.childCount) || !isValidHeadcount(form.guardianCount) ? quoteFieldErrorMessages.headcount : undefined)
  const { optional, institutionName, schedule, venue, participants, headcount, programs, phone, note } = quoteConsultationFields

  return (
    <div className="consult-modal" onClick={onClose}>
      <div
        className={`consult-modal__dialog ${done ? 'consult-modal__dialog--done' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="quote-modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <button type="button" className="consult-modal__close" aria-label="닫기" onClick={onClose}>
          ✕
        </button>

        {done ? (
          <div className="consult-modal__done">
            <span className="consult-modal__done-icon" aria-hidden="true">✓</span>
            <h2 id="quote-modal-title" className="consult-modal__title">{quoteConsultationDone.title}</h2>
            <p className="consult-modal__description consult-modal__description--lines">
              {quoteConsultationDone.description.map((line) => (
                <span key={line}>{line}</span>
              ))}
            </p>
            <Button variant="yellow" size="lg" className="consult-modal__submit" onClick={onClose}>
              {quoteConsultationDone.confirm}
            </Button>
          </div>
        ) : (
          <>
            <div className="consult-modal__head">
              <span className="consult-modal__badge">{quoteConsultation.badge}</span>
              <h2 id="quote-modal-title" className="consult-modal__title">{quoteConsultation.title}</h2>
              <p className="consult-modal__description">{quoteConsultation.description}</p>
            </div>

            <form className="consult-modal__form" onSubmit={handleSubmit} noValidate>
              <div className="consult-modal__field">
                <label htmlFor="quote-institution-name" className="consult-modal__label">
                  {institutionName.label} <span className="consult-modal__required">*</span>
                </label>
                <input
                  id="quote-institution-name"
                  className="consult-modal__input"
                  maxLength={quoteConsultationRules.institutionNameMax}
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
                <div className="quote-modal__schedule">
                  <input
                    className="consult-modal__input"
                    type="date"
                    aria-label={schedule.dateLabel}
                    min={today()}
                    disabled={form.scheduleUndecided}
                    value={form.eventDate}
                    onChange={(event) => update('eventDate', event.target.value)}
                  />
                  <input
                    className="consult-modal__input"
                    type="time"
                    aria-label={schedule.timeLabel}
                    disabled={form.scheduleUndecided}
                    value={form.eventTime}
                    onChange={(event) => update('eventTime', event.target.value)}
                  />
                </div>
                <label className="quote-modal__check">
                  <input
                    type="checkbox"
                    checked={form.scheduleUndecided}
                    onChange={(event) => update('scheduleUndecided', event.target.checked)}
                  />
                  {schedule.undecided}
                </label>
                <FieldError message={fieldErrors.eventDate ?? fieldErrors.eventTime} />
              </fieldset>

              <fieldset className="consult-modal__field">
                <legend className="consult-modal__label">
                  {venue.label} <span className="consult-modal__required">*</span>
                </legend>
                <ChipGroup
                  options={venueTypes}
                  selected={form.venueType}
                  onToggle={(value) => update('venueType', value)}
                />
                <input
                  className="consult-modal__input"
                  aria-label={venue.addressLabel}
                  maxLength={quoteConsultationRules.addressMax}
                  placeholder={venue.addressPlaceholder}
                  disabled={!needsAddress(form)}
                  value={needsAddress(form) ? form.address : ''}
                  onChange={(event) => update('address', event.target.value)}
                />
                <FieldError message={fieldErrors.venueType ?? fieldErrors.address} />
              </fieldset>

              <fieldset className="consult-modal__field">
                <legend className="consult-modal__label">
                  {participants.label} <span className="consult-modal__required">*</span>
                </legend>
                <ChipGroup
                  options={participantGroups}
                  selected={form.participantGroups}
                  onToggle={(value) => update('participantGroups', toggleValue(form.participantGroups, value))}
                />
                <p className="consult-modal__hint">{participants.hint}</p>
                <FieldError message={fieldErrors.participantGroups} />
              </fieldset>

              <fieldset className="consult-modal__field">
                <legend className="consult-modal__label">
                  {headcount.label} <span className="consult-modal__required">*</span>
                </legend>
                <div className="quote-modal__headcount">
                  {[
                    { field: 'childCount', label: headcount.childLabel },
                    { field: 'guardianCount', label: headcount.guardianLabel },
                  ].map(({ field, label }) => (
                    <div key={field} className="consult-modal__count">
                      <label htmlFor={`quote-${field}`} className="consult-modal__count-label">{label}</label>
                      <input
                        id={`quote-${field}`}
                        className="consult-modal__input consult-modal__input--count"
                        type="number"
                        inputMode="numeric"
                        min={quoteConsultationRules.headcountMin}
                        max={quoteConsultationRules.headcountMax}
                        placeholder="0"
                        value={form[field]}
                        onChange={(event) => update(field, event.target.value)}
                      />
                      <span>{headcount.unit}</span>
                    </div>
                  ))}
                </div>
                <p className="consult-modal__hint">{headcount.hint}</p>
                <FieldError message={headcountError} />
              </fieldset>

              <fieldset className="consult-modal__field">
                <legend className="consult-modal__label">
                  {programs.label} <span className="consult-modal__optional">{optional}</span>
                </legend>
                <ChipGroup
                  options={quotePrograms}
                  selected={form.programs}
                  onToggle={(value) => update('programs', toggleValue(form.programs, value))}
                />
                <p className="consult-modal__hint">{programs.hint}</p>
                <FieldError message={fieldErrors.programs} />
              </fieldset>

              <div className="consult-modal__field">
                <label htmlFor="quote-phone" className="consult-modal__label">
                  {phone.label} <span className="consult-modal__required">*</span>
                </label>
                <input
                  id="quote-phone"
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
                <label htmlFor="quote-note" className="consult-modal__label">
                  {note.label} <span className="consult-modal__optional">{optional}</span>
                </label>
                <textarea
                  id="quote-note"
                  className="consult-modal__input consult-modal__textarea"
                  rows={3}
                  maxLength={quoteConsultationRules.noteMax}
                  placeholder={note.placeholder}
                  value={form.note}
                  onChange={(event) => update('note', event.target.value)}
                />
                <FieldError message={fieldErrors.note} />
              </div>

              <div className="consult-modal__privacy">
                <div className="quote-modal__privacy-head">
                  <strong className="consult-modal__privacy-title">{quotePrivacyNotice.title}</strong>
                  <button
                    type="button"
                    className="quote-modal__privacy-toggle"
                    aria-expanded={privacyOpen}
                    aria-controls="quote-privacy-list"
                    onClick={() => setPrivacyOpen((open) => !open)}
                  >
                    {quotePrivacyNotice.toggle}
                  </button>
                </div>
                {privacyOpen && (
                  <dl id="quote-privacy-list" className="consult-modal__privacy-list">
                    {quotePrivacyNotice.items.map((item) => (
                      <div key={item.term} className="consult-modal__privacy-item">
                        <dt>{item.term}</dt>
                        <dd>{item.description}</dd>
                      </div>
                    ))}
                  </dl>
                )}
                <p className="consult-modal__hint">{quotePrivacyNotice.refusal}</p>
                <label className="consult-modal__agree">
                  <input
                    type="checkbox"
                    checked={form.privacyAgreed}
                    onChange={(event) => update('privacyAgreed', event.target.checked)}
                  />
                  {quotePrivacyNotice.agree} <span className="consult-modal__required">{quotePrivacyNotice.required}</span>
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
                {submitting ? quoteConsultation.submitting : quoteConsultation.submit}
              </Button>
            </form>
          </>
        )}
      </div>
    </div>
  )
}

export default QuoteConsultationModal
