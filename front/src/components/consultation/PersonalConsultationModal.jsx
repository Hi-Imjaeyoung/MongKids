import { useEffect, useRef, useState } from 'react'
import { createPersonalConsultation } from '../../api/consultations'
import { ApiError } from '../../api/client'
import Button from '../common/Button'
import { ChipGroup, FieldError } from './ConsultationFormParts'
import {
  childGenders,
  consultationDone,
  daycareOptions,
  healthNotice,
  personalConsultation,
  personalConsultationFields,
  personalConsultationRules,
  personalFieldErrorMessages,
  personalPrivacyNotice,
  personalStoryFields,
} from '../../data/consultationContent'
import './InstitutionConsultationModal.css'
import './PersonalConsultationModal.css'

const initialForm = {
  guardianName: '',
  phoneMiddle: '',
  phoneLast: '',
  childName: '',
  childGender: '',
  ageByBirth: true,
  childBirthDate: '',
  childAgeMonths: '',
  address: '',
  attendsDaycare: null,
  temperament: '',
  favoritePlay: '',
  favoriteCharacter: '',
  healthNote: '',
  motivation: '',
  privacyAgreed: false,
  healthAgreed: false,
}

// 서버 필드 오류가 이 항목이면 1단계로 돌아가 보여 준다
const STEP_ONE_FIELDS = ['guardianName', 'phone', 'childName', 'childGender', 'childBirthDate', 'childAgeMonths', 'address', 'attendsDaycare']

const PHONE_PART = /^\d{4}$/

function today() {
  // YYYY-MM-DD (로컬 날짜)
  return new Date().toLocaleDateString('sv-SE')
}

function onlyDigits(value, max) {
  return value.replace(/\D/g, '').slice(0, max)
}

function isValidAgeMonths(value) {
  const months = Number(value)
  return value !== '' && Number.isInteger(months)
    && months >= personalConsultationRules.ageMonthsMin && months <= personalConsultationRules.ageMonthsMax
}

function isValidAge(form) {
  if (form.ageByBirth) return form.childBirthDate !== '' && form.childBirthDate <= today()
  return isValidAgeMonths(form.childAgeMonths)
}

function hasHealthNote(form) {
  return form.healthNote.trim() !== ''
}

// 1단계 필수 항목 (FR-002)
function canGoNext(form) {
  return (
    form.guardianName.trim() !== '' &&
    PHONE_PART.test(form.phoneMiddle) &&
    PHONE_PART.test(form.phoneLast) &&
    form.childName.trim() !== '' &&
    form.childGender !== '' &&
    isValidAge(form) &&
    form.address.trim() !== '' &&
    form.attendsDaycare !== null
  )
}

// 개인정보 동의는 필수, 건강 메모를 적었으면 건강정보 동의도 필수 (FR-023)
function canSubmit(form) {
  return canGoNext(form) && form.privacyAgreed && (!hasHealthNote(form) || form.healthAgreed)
}

function toRequest(form) {
  const text = (value) => value.trim() || null
  return {
    guardianName: form.guardianName.trim(),
    phone: `${personalConsultationFields.phone.prefix}${form.phoneMiddle}${form.phoneLast}`,
    childName: form.childName.trim(),
    childGender: form.childGender,
    childBirthDate: form.ageByBirth ? form.childBirthDate : null,
    childAgeMonths: form.ageByBirth ? null : Number(form.childAgeMonths),
    address: form.address.trim(),
    attendsDaycare: form.attendsDaycare,
    temperament: text(form.temperament),
    favoritePlay: text(form.favoritePlay),
    favoriteCharacter: text(form.favoriteCharacter),
    healthNote: text(form.healthNote),
    motivation: text(form.motivation),
    privacyAgreed: form.privacyAgreed,
    healthAgreed: hasHealthNote(form) && form.healthAgreed,
  }
}

function NoticeBox({ notice, checked, onChange, className = '' }) {
  return (
    <div className={`consult-modal__privacy ${className}`}>
      <strong className="consult-modal__privacy-title">{notice.title}</strong>
      <dl className="consult-modal__privacy-list">
        {notice.items.map((item) => (
          <div key={item.term} className="consult-modal__privacy-item">
            <dt>{item.term}</dt>
            <dd>{item.description}</dd>
          </div>
        ))}
      </dl>
      <p className="consult-modal__hint">{notice.refusal}</p>
      <label className="consult-modal__agree">
        <input type="checkbox" checked={checked} onChange={(event) => onChange(event.target.checked)} />
        {notice.agree} <span className="consult-modal__required">{notice.required}</span>
      </label>
    </div>
  )
}

// 개인 상담 신청 모달 (SCR-010, FR-002). 비회원도 신청할 수 있다 (FR-003)
function PersonalConsultationModal({ onClose }) {
  const [form, setForm] = useState(initialForm)
  const [step, setStep] = useState(1)
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)
  const overlayRef = useRef(null)
  const phoneLastRef = useRef(null)

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

  // 단계가 바뀌면 모달 맨 위부터 보여 준다
  useEffect(() => {
    overlayRef.current?.scrollTo(0, 0)
  }, [step, done])

  const update = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => ({ ...current, [field]: undefined }))
  }

  const updatePhone = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => ({ ...current, phone: undefined }))
  }

  // 가운데 칸에 번호 전체를 붙여 넣어도 두 칸으로 나눠 담는다
  const handlePhoneMiddle = (value) => {
    let digits = value.replace(/\D/g, '')
    if (digits.length === 11 && digits.startsWith(personalConsultationFields.phone.prefix)) digits = digits.slice(3)
    const { phonePartLength } = personalConsultationRules
    updatePhone('phoneMiddle', digits.slice(0, phonePartLength))
    if (digits.length > phonePartLength) updatePhone('phoneLast', digits.slice(phonePartLength, phonePartLength * 2))
    if (digits.length >= phonePartLength) phoneLastRef.current?.focus()
  }

  const toggleAgeMode = () => {
    setForm((current) => ({ ...current, ageByBirth: !current.ageByBirth }))
    setFieldErrors((current) => ({ ...current, childBirthDate: undefined, childAgeMonths: undefined }))
  }

  const handleNext = (event) => {
    event.preventDefault()
    if (canGoNext(form)) setStep(2)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!canSubmit(form) || submitting) return

    setSubmitting(true)
    setError('')
    try {
      await createPersonalConsultation(toRequest(form))
      setDone(true)
    } catch (caught) {
      if (caught instanceof ApiError && caught.fieldErrors.length > 0) {
        setFieldErrors(Object.fromEntries(
          caught.fieldErrors.map(({ field }) => [field, personalFieldErrorMessages[field] ?? personalFieldErrorMessages.default]),
        ))
        if (caught.fieldErrors.some(({ field }) => STEP_ONE_FIELDS.includes(field))) setStep(1)
      } else {
        setError(personalConsultation.errorMessage)
      }
    } finally {
      setSubmitting(false)
    }
  }

  const { optional, guardianName, phone, childName, childGender, childAge, address, attendsDaycare } = personalConsultationFields
  const monthsError = fieldErrors.childAgeMonths
    ?? (!form.ageByBirth && form.childAgeMonths !== '' && !isValidAgeMonths(form.childAgeMonths)
      ? personalFieldErrorMessages.childAgeMonths : undefined)
  const showHealthNotice = hasHealthNote(form)

  return (
    <div className="consult-modal" ref={overlayRef} onClick={onClose}>
      <div
        className={`consult-modal__dialog consult-modal__dialog--personal ${done ? 'consult-modal__dialog--done' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="personal-consult-title"
        onClick={(event) => event.stopPropagation()}
      >
        <button type="button" className="consult-modal__close" aria-label="닫기" onClick={onClose}>
          ✕
        </button>

        {done ? (
          <div className="consult-modal__done">
            <span className="consult-modal__done-icon" aria-hidden="true">✓</span>
            <h2 id="personal-consult-title" className="consult-modal__title">{consultationDone.title}</h2>
            <p className="consult-modal__description consult-modal__description--lines">
              {consultationDone.description.map((line) => (
                <span key={line}>{line}</span>
              ))}
            </p>
            <Button variant="blue" size="lg" className="consult-modal__submit" onClick={onClose}>
              {consultationDone.confirm}
            </Button>
          </div>
        ) : (
          <>
            <div className="consult-modal__head">
              <span className="consult-modal__badge">{personalConsultation.badge}</span>
              <h2 id="personal-consult-title" className="consult-modal__title">
                {step === 1 ? personalConsultation.title : personalConsultation.storyTitle}
              </h2>
              <p className="consult-modal__description">
                {step === 1 ? personalConsultation.description : personalConsultation.storyDescription}
              </p>
            </div>

            <ol className="consult-steps" aria-label="신청 단계">
              {personalConsultation.steps.map((label, index) => (
                <li
                  key={label}
                  className={`consult-steps__item ${index + 1 <= step ? 'consult-steps__item--reached' : ''} ${index + 1 === step ? 'consult-steps__item--current' : ''}`}
                  aria-current={index + 1 === step ? 'step' : undefined}
                >
                  <span className="consult-steps__bar" aria-hidden="true" />
                  {index + 1} {label}
                </li>
              ))}
            </ol>

            {step === 1 ? (
              <form className="consult-modal__form" onSubmit={handleNext} noValidate>
                <div className="consult-modal__row">
                  <div className="consult-modal__field">
                    <label htmlFor="personal-guardian" className="consult-modal__label">
                      {guardianName.label} <span className="consult-modal__required">*</span>
                    </label>
                    <input
                      id="personal-guardian"
                      className="consult-modal__input"
                      maxLength={personalConsultationRules.nameMax}
                      autoComplete="name"
                      placeholder={guardianName.placeholder}
                      value={form.guardianName}
                      onChange={(event) => update('guardianName', event.target.value)}
                    />
                    <FieldError message={fieldErrors.guardianName} />
                  </div>

                  <fieldset className="consult-modal__field">
                    <legend className="consult-modal__label">
                      {phone.label} <span className="consult-modal__required">*</span>
                    </legend>
                    <div className="consult-phone">
                      <span className="consult-phone__prefix">{phone.prefix}</span>
                      <span className="consult-phone__dash" aria-hidden="true">-</span>
                      <input
                        className={`consult-modal__input consult-phone__part ${fieldErrors.phone ? 'consult-modal__input--error' : ''}`}
                        aria-label={phone.middleLabel}
                        inputMode="numeric"
                        placeholder={phone.middlePlaceholder}
                        value={form.phoneMiddle}
                        aria-invalid={Boolean(fieldErrors.phone)}
                        onChange={(event) => handlePhoneMiddle(event.target.value)}
                      />
                      <span className="consult-phone__dash" aria-hidden="true">-</span>
                      <input
                        ref={phoneLastRef}
                        className={`consult-modal__input consult-phone__part ${fieldErrors.phone ? 'consult-modal__input--error' : ''}`}
                        aria-label={phone.lastLabel}
                        inputMode="numeric"
                        placeholder={phone.lastPlaceholder}
                        value={form.phoneLast}
                        aria-invalid={Boolean(fieldErrors.phone)}
                        onChange={(event) => updatePhone('phoneLast', onlyDigits(event.target.value, personalConsultationRules.phonePartLength))}
                      />
                    </div>
                    <p className="consult-modal__hint">{phone.hint}</p>
                    <FieldError message={fieldErrors.phone} />
                  </fieldset>
                </div>

                <div className="consult-modal__row">
                  <div className="consult-modal__field">
                    <label htmlFor="personal-child" className="consult-modal__label">
                      {childName.label} <span className="consult-modal__required">*</span>
                    </label>
                    <input
                      id="personal-child"
                      className="consult-modal__input"
                      maxLength={personalConsultationRules.nameMax}
                      placeholder={childName.placeholder}
                      value={form.childName}
                      onChange={(event) => update('childName', event.target.value)}
                    />
                    <FieldError message={fieldErrors.childName} />
                  </div>

                  <fieldset className="consult-modal__field">
                    <legend className="consult-modal__label">
                      {childGender.label} <span className="consult-modal__required">*</span>
                    </legend>
                    <ChipGroup
                      options={childGenders}
                      selected={form.childGender}
                      onToggle={(value) => update('childGender', value)}
                    />
                    <FieldError message={fieldErrors.childGender} />
                  </fieldset>
                </div>

                <div className="consult-modal__field">
                  <div className="consult-age__head">
                    <label htmlFor="personal-age" className="consult-modal__label">
                      {form.ageByBirth ? childAge.birthLabel : childAge.monthsLabel} <span className="consult-modal__required">*</span>
                    </label>
                    <button type="button" className="consult-age__switch" onClick={toggleAgeMode}>
                      {form.ageByBirth ? childAge.toMonths : childAge.toBirth}
                    </button>
                  </div>
                  {form.ageByBirth ? (
                    <input
                      id="personal-age"
                      className="consult-modal__input"
                      type="date"
                      max={today()}
                      value={form.childBirthDate}
                      onChange={(event) => update('childBirthDate', event.target.value)}
                    />
                  ) : (
                    <div className="consult-modal__count">
                      <input
                        id="personal-age"
                        className="consult-modal__input consult-modal__input--count"
                        type="number"
                        inputMode="numeric"
                        min={personalConsultationRules.ageMonthsMin}
                        max={personalConsultationRules.ageMonthsMax}
                        placeholder="0"
                        value={form.childAgeMonths}
                        onChange={(event) => update('childAgeMonths', event.target.value)}
                      />
                      <span>{childAge.monthsUnit}</span>
                    </div>
                  )}
                  <p className="consult-modal__hint">{childAge.hint}</p>
                  <FieldError message={form.ageByBirth ? fieldErrors.childBirthDate : monthsError} />
                </div>

                <div className="consult-modal__field">
                  <label htmlFor="personal-address" className="consult-modal__label">
                    {address.label} <span className="consult-modal__required">*</span>
                  </label>
                  <input
                    id="personal-address"
                    className="consult-modal__input"
                    maxLength={personalConsultationRules.addressMax}
                    autoComplete="street-address"
                    placeholder={address.placeholder}
                    value={form.address}
                    onChange={(event) => update('address', event.target.value)}
                  />
                  <p className="consult-modal__hint">{address.hint}</p>
                  <FieldError message={fieldErrors.address} />
                </div>

                <fieldset className="consult-modal__field">
                  <legend className="consult-modal__label">
                    {attendsDaycare.label} <span className="consult-modal__required">*</span>
                  </legend>
                  <ChipGroup
                    options={daycareOptions}
                    selected={form.attendsDaycare}
                    onToggle={(value) => update('attendsDaycare', value)}
                  />
                  <FieldError message={fieldErrors.attendsDaycare} />
                </fieldset>

                <Button
                  variant="blue"
                  size="lg"
                  type="submit"
                  className="consult-modal__submit"
                  disabled={!canGoNext(form)}
                >
                  {personalConsultation.next}
                </Button>
              </form>
            ) : (
              <form className="consult-modal__form" onSubmit={handleSubmit} noValidate>
                {personalStoryFields.map((field) => (
                  <div key={field.name} className="consult-modal__field">
                    <label htmlFor={`personal-${field.name}`} className="consult-modal__label">
                      {field.label} <span className="consult-modal__optional">{optional}</span>
                    </label>
                    <textarea
                      id={`personal-${field.name}`}
                      className="consult-modal__input consult-modal__textarea"
                      rows={field.rows}
                      maxLength={personalConsultationRules.storyMax}
                      placeholder={field.placeholder}
                      value={form[field.name]}
                      onChange={(event) => update(field.name, event.target.value)}
                    />
                    {field.hint && <p className="consult-modal__hint">{field.hint}</p>}
                    <FieldError message={fieldErrors[field.name]} />
                  </div>
                ))}

                <NoticeBox
                  notice={personalPrivacyNotice}
                  checked={form.privacyAgreed}
                  onChange={(checked) => update('privacyAgreed', checked)}
                />
                <FieldError message={fieldErrors.privacyAgreed} />

                {showHealthNotice && (
                  <NoticeBox
                    notice={healthNotice}
                    className="consult-modal__privacy--health"
                    checked={form.healthAgreed}
                    onChange={(checked) => update('healthAgreed', checked)}
                  />
                )}

                {error && <p className="consult-modal__error" role="alert">{error}</p>}

                <div className="consult-modal__actions">
                  <button type="button" className="consult-modal__prev" onClick={() => setStep(1)}>
                    {personalConsultation.prev}
                  </button>
                  <Button
                    variant="blue"
                    size="lg"
                    type="submit"
                    className="consult-modal__submit"
                    disabled={!canSubmit(form) || submitting}
                  >
                    {submitting ? personalConsultation.submitting : personalConsultation.submit}
                  </Button>
                </div>
              </form>
            )}
          </>
        )}
      </div>
    </div>
  )
}

export default PersonalConsultationModal
