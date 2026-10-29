import { useState } from 'react'
import { attendanceImageRules } from '../../api/attendance'
import { classCard, classEndForm } from '../../data/attendanceContent'
import PhotoUploader from '../common/PhotoUploader'
import { toPhoto } from '../common/photoItem'
import AttendanceAction from './AttendanceAction'
import AttendanceScreen from './AttendanceScreen'
import ClassSummary from './ClassSummary'
import { formatClock, formatStart } from './attendanceFormat'
import './ClassEndForm.css'

const PHOTO_LABELS = {
  add: classEndForm.photoAdd,
  remove: classEndForm.photoRemove,
  countError: classEndForm.photoCountError,
  fileError: classEndForm.photoFileError,
}

// ⑥ 수업 종료(EMP-FR-010), ⑦ 수업 기록 정정(EMP-FR-014). editing이면 제출한 사진·특이사항으로 채운다.
// onSubmit({ note, photos })는 업로드·저장을 맡고, 실패하면 error로 알려 준다
function ClassEndForm({ classSession, editing = false, submitting, error, onSubmit, onBack }) {
  const { start, end } = classSession
  const [photos, setPhotos] = useState(() => (editing ? end.images.map(toPhoto) : []))
  const [note, setNote] = useState(editing ? (end.note ?? '') : '')
  const [photoError, setPhotoError] = useState(null)

  const submit = (event) => {
    event.preventDefault()
    if (submitting) return
    if (photos.length < attendanceImageRules.minCount) {
      setPhotoError(classEndForm.photoRequired)
      return
    }
    onSubmit({ note: note.trim(), photos })
  }

  const times = [formatStart(start)]
  if (editing) times.push(classCard.ended(formatClock(end.endedAt)))

  return (
    <form onSubmit={submit}>
      <AttendanceScreen
        title={editing ? classEndForm.editTitle : classEndForm.endTitle}
        onBack={onBack}
        footer={
          <AttendanceAction variant={editing ? 'primary' : 'dark'} type="submit" disabled={submitting}>
            {submitting ? classEndForm.submitting : editing ? classEndForm.submitEdit : classEndForm.submitEnd}
          </AttendanceAction>
        }
      >
        <ClassSummary classSession={classSession} showStatus>
          <span className="class-summary__extra">{times.join(' · ')}</span>
          {editing && (
            <span className="class-summary__extra">
              {classEndForm.lastCorrected(end.correctedAt && formatClock(end.correctedAt))}
            </span>
          )}
        </ClassSummary>

        <div className="attendance-field">
          <div className="class-end__photo-head">
            <span className="attendance-field__label">
              {classEndForm.photoLabel} <span className="attendance-field__required">*</span>
            </span>
            <span className="attendance-field__count">
              {photos.length} / {attendanceImageRules.maxCount}
            </span>
          </div>
          <p className="attendance-hint">{classEndForm.photoRule}</p>
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

        <div className="attendance-field">
          <label htmlFor="class-end-note" className="attendance-field__label">
            {classEndForm.noteLabel}
          </label>
          <textarea
            id="class-end-note"
            className="attendance-field__textarea"
            rows={5}
            maxLength={classEndForm.noteMaxLength}
            placeholder={classEndForm.notePlaceholder}
            value={note}
            onChange={(event) => setNote(event.target.value)}
          />
        </div>

        <p className="attendance-hint">{editing ? classEndForm.editNotice : classEndForm.endNotice}</p>

        {error && (
          <p className="attendance-error" role="alert">
            {error}
          </p>
        )}
      </AttendanceScreen>
    </form>
  )
}

export default ClassEndForm
