import { useState } from 'react'
import { attendanceImageRules } from '../../api/attendance'
import { dayEndForm, manualStart, todayLabel } from '../../data/attendanceContent'
import PhotoUploader from '../common/PhotoUploader'
import { toPhoto } from '../common/photoItem'
import AttendanceAction from './AttendanceAction'
import AttendanceScreen from './AttendanceScreen'
import ClassStatusBadge from './ClassStatusBadge'
import { formatClock, formatRoute, fromDateKey } from './attendanceFormat'
import './ClassSummary.css'
import './DayEndForm.css'

const PHOTO_LABELS = {
  add: dayEndForm.photoAdd,
  remove: dayEndForm.photoRemove,
  countError: dayEndForm.photoCountError,
  fileError: dayEndForm.photoFileError,
}

// 그날 수업한 기관마다 (수업 순서, 같은 기관은 한 번) 대상(반)을 모은다
function institutionsOf(day) {
  const institutions = new Map()
  day.classes.forEach((dayClass) => {
    const current = institutions.get(dayClass.institutionId) ?? {
      institutionId: dayClass.institutionId,
      institutionName: dayClass.institutionName,
      targets: [],
    }
    if (dayClass.target && !current.targets.includes(dayClass.target)) current.targets.push(dayClass.target)
    institutions.set(dayClass.institutionId, current)
  })
  return [...institutions.values()]
}

// ② 하루 수업 종료(EMP-FR-020), 수동 종료, 수업 기록 정정(EMP-FR-014 규칙).
// mode: 'end'(마지막 기관 반경 안 확인됨, check: { institutionName, distanceMeters, radiusMeters }) | 'manual'(사유 필수, measured: { distance, accuracy } | null) | 'edit'(제출한 사진·특이사항으로 채움)
// onSubmit({ reason, photos, notes })는 업로드·저장을 맡고, 실패하면 error로 알려 준다
function DayEndForm({ day, mode, check, measured, submitting, error, onSubmit, onBack }) {
  const editing = mode === 'edit'
  const manual = mode === 'manual'
  const institutions = institutionsOf(day)
  const [photos, setPhotos] = useState(() => (editing ? day.images.map(toPhoto) : []))
  const [notes, setNotes] = useState(() =>
    Object.fromEntries(
      institutions.map(({ institutionId }) => [
        institutionId,
        editing ? (day.notes.find((note) => note.institutionId === institutionId)?.note ?? '') : '',
      ]),
    ),
  )
  const [reason, setReason] = useState('')
  const [photoError, setPhotoError] = useState(null)

  const submit = (event) => {
    event.preventDefault()
    if (submitting) return
    if (manual && !reason.trim()) return
    if (photos.length < attendanceImageRules.minCount) {
      setPhotoError(dayEndForm.photoRequired)
      return
    }
    onSubmit({
      reason: reason.trim(),
      photos,
      notes: institutions.map(({ institutionId }) => ({ institutionId, note: notes[institutionId].trim() })),
    })
  }

  let title = dayEndForm.endTitle
  let submitLabel = dayEndForm.submitEnd
  let notice = dayEndForm.endNotice
  if (editing) {
    title = dayEndForm.editTitle
    submitLabel = dayEndForm.submitEdit
    notice = dayEndForm.editNotice
  } else if (manual) {
    title = dayEndForm.manualTitle
    submitLabel = dayEndForm.submitManual
    notice = dayEndForm.manualNotice
  }

  return (
    <form onSubmit={submit}>
      <AttendanceScreen
        title={title}
        onBack={onBack}
        footer={
          <AttendanceAction variant={editing ? 'primary' : 'dark'} type="submit" disabled={submitting || (manual && !reason.trim())}>
            {submitting ? dayEndForm.submitting : submitLabel}
          </AttendanceAction>
        }
      >
        {mode === 'end' && check && (
          <section className="day-end__in-range" aria-label={dayEndForm.inRange(check.institutionName)}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <path d="M5 12l5 5 9-10" />
            </svg>
            <div className="day-end__in-range-text">
              <strong>{dayEndForm.inRange(check.institutionName)}</strong>
              <span>{dayEndForm.inRangeDetail(check.distanceMeters, check.radiusMeters)}</span>
            </div>
          </section>
        )}

        <section className="class-summary">
          <div className="class-summary__head">
            <strong className="class-summary__name">{todayLabel(fromDateKey(day.date))}</strong>
            <ClassStatusBadge status={day.status} day />
          </div>
          <span className="class-summary__extra">
            {dayEndForm.summary(day.classes.length, formatRoute(day), day.start && formatClock(day.start.checkedAt))}
          </span>
          {manual && (
            <span className="class-summary__extra">
              {measured ? manualStart.measured(measured.distance, measured.accuracy) : manualStart.notMeasured}
            </span>
          )}
          {editing && (
            <span className="class-summary__extra">
              {dayEndForm.lastCorrected(day.correctedAt && formatClock(day.correctedAt))}
            </span>
          )}
        </section>

        {manual && (
          <div className="attendance-field">
            <label htmlFor="day-end-reason" className="attendance-field__label">
              {dayEndForm.reasonLabel} <span className="attendance-field__required">{manualStart.required}</span>
            </label>
            <textarea
              id="day-end-reason"
              className="attendance-field__textarea"
              rows={3}
              maxLength={manualStart.reasonMaxLength}
              placeholder={dayEndForm.reasonPlaceholder}
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              required
            />
            <span className="attendance-field__count">
              {reason.length} / {manualStart.reasonMaxLength}
            </span>
          </div>
        )}

        <div className="attendance-field">
          <div className="day-end__photo-head">
            <span className="attendance-field__label">
              {dayEndForm.photoLabel} <span className="attendance-field__required">*</span>
            </span>
            <span className="attendance-field__count">
              {photos.length} / {attendanceImageRules.maxCount}
            </span>
          </div>
          <p className="attendance-hint">
            {institutions.length > 1 && `${dayEndForm.photoManyInstitutions} `}
            {dayEndForm.photoRule}
          </p>
          <PhotoUploader
            photos={photos}
            rules={attendanceImageRules}
            labels={PHOTO_LABELS}
            disabled={submitting}
            onChange={setPhotos}
            onError={setPhotoError}
          />
          {photoError && (
            <p className="attendance-error" role="alert">
              {photoError}
            </p>
          )}
        </div>

        <fieldset className="day-end__notes">
          <legend className="attendance-field__label">{dayEndForm.notesLabel}</legend>
          {institutions.map((institution, index) => (
            <div key={institution.institutionId} className="attendance-field">
              <label htmlFor={`day-end-note-${institution.institutionId}`} className="day-end__note-label">
                <span className="day-end__note-name">{institution.institutionName}</span>
                <span className="day-end__note-targets">{institution.targets.join(' · ')}</span>
              </label>
              <textarea
                id={`day-end-note-${institution.institutionId}`}
                className="attendance-field__textarea"
                rows={4}
                maxLength={dayEndForm.noteMaxLength}
                placeholder={dayEndForm.notePlaceholders[index % dayEndForm.notePlaceholders.length]}
                value={notes[institution.institutionId]}
                onChange={(event) =>
                  setNotes((current) => ({ ...current, [institution.institutionId]: event.target.value }))
                }
              />
            </div>
          ))}
        </fieldset>

        <p className="attendance-hint">{notice}</p>

        {error && (
          <p className="attendance-error" role="alert">
            {error}
          </p>
        )}
      </AttendanceScreen>
    </form>
  )
}

export default DayEndForm
