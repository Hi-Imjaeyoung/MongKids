import { useState } from 'react'
import { dayDetailAdmin, longDateLabel, startMethods } from '../../data/attendanceAdminContent'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import { countInstitutions, formatClassMeta, formatClock, groupByInstitution } from '../attendance/attendanceFormat'
import AdminBadge from './AdminBadge'
import { formatDateTime } from './adminFormat'
import { canRegisterDay } from './attendanceAdminFormat'
import ClassRecordDetail from './ClassRecordDetail'
import StartLocation from './StartLocation'
import './AdminForm.css'
import './ClassRecordDetail.css'
import './DayDetail.css'

const CHECK_TONES = { CONFIRMED: 'green', PENDING: 'yellow', REJECTED: 'gray' }

// 하루 시작·종료 한쪽. 승인 대기면 승인·반려, 기록이 없거나 반려면 직접 등록
function CheckSection({ type, day, busy, onApprove, onReject, onRegister }) {
  const check = day[type]
  const title = dayDetailAdmin.sections[type]
  const { fields } = dayDetailAdmin
  const [rejectReason, setRejectReason] = useState('')
  const [error, setError] = useState('')

  const reject = () => {
    if (!rejectReason.trim()) {
      setError(dayDetailAdmin.rejectRequired)
      return
    }
    onReject(rejectReason.trim())
  }

  return (
    <section className="day-detail__section" aria-label={title}>
      <div className="day-detail__section-head">
        <h3 className="day-detail__section-title">{title}</h3>
        {check && (
          <AdminBadge tone={CHECK_TONES[check.status]}>
            {dayDetailAdmin.methodStatus(startMethods[check.method], dayDetailAdmin.checkStatuses[check.status])}
          </AdminBadge>
        )}
      </div>
      {check ? (
        <dl className="admin-fields">
          <dt>{fields.time}</dt>
          <dd>{formatClock(check.checkedAt)}</dd>
          <dt>{fields.institution}</dt>
          <dd>
            {check.institutionName}{' '}
            <span className="admin-card__sub">{type === 'start' ? dayDetailAdmin.first : dayDetailAdmin.last}</span>
          </dd>
          <dt>{fields.location}</dt>
          <dd className="admin-inline-list">
            <StartLocation start={check} detail />
          </dd>
          {check.reason && (
            <>
              <dt>{fields.reason}</dt>
              <dd>{check.reason}</dd>
            </>
          )}
          {check.status === 'REJECTED' && (
            <>
              <dt>{fields.rejectReason}</dt>
              <dd>{check.rejectReason}</dd>
            </>
          )}
          {check.processedAt && (
            <>
              <dt>{fields.processedAt}</dt>
              <dd>{formatDateTime(check.processedAt)}</dd>
            </>
          )}
        </dl>
      ) : (
        <p className="admin-card__sub">{dayDetailAdmin.none}</p>
      )}

      {check?.status === 'PENDING' && (
        <>
          <div className="admin-form__field">
            <label className="admin-form__label" htmlFor={`reject-${type}`}>
              {dayDetailAdmin.rejectLabel(title)}
            </label>
            <textarea
              id={`reject-${type}`}
              className="admin-form__input"
              maxLength={200}
              placeholder={dayDetailAdmin.rejectPlaceholder}
              value={rejectReason}
              onChange={(event) => {
                setRejectReason(event.target.value)
                setError('')
              }}
            />
            <span className="admin-form__hint">{dayDetailAdmin.rejectHint}</span>
            {error && (
              <p className="admin-form__error" role="alert">
                {error}
              </p>
            )}
          </div>
          <div className="admin-form__actions">
            <button type="button" className="admin-form__button admin-form__button--danger" disabled={busy} onClick={reject}>
              {dayDetailAdmin.reject}
            </button>
            <button type="button" className="admin-form__button admin-form__button--dark" disabled={busy} onClick={onApprove}>
              {dayDetailAdmin.approve[type]}
            </button>
          </div>
        </>
      )}

      {onRegister && canRegisterDay(day, type) && (
        <button type="button" className="admin-form__button admin-form__button--ghost" disabled={busy} onClick={onRegister}>
          {dayDetailAdmin.register[type]}
        </button>
      )}
      {onRegister && type === 'end' && !check && !canRegisterDay(day, 'end') && (
        <p className="admin-form__hint">{dayDetailAdmin.endNeedsStart}</p>
      )}
    </section>
  )
}

// 하루 근무 상세 (EMP-SCR-006 ④, EMP-FR-020). item: AdminWorkDayResponse.
// 시작·종료 각각 승인 onApprove(type)·반려 onReject(type, reason), 직접 등록 onRegister(type)(없으면 버튼을 숨김).
// 하루 단위 적용 전 날짜는 수업별 기록을 읽기 전용으로 보여 준다
function DayDetail({ item, busy, onApprove, onReject, onRegister }) {
  const { day } = item
  const legacy = day.legacyClasses.length > 0

  return (
    <section className="admin-card day-detail" aria-label={dayDetailAdmin.label}>
      <div className="admin-card__head">
        <div className="day-detail__heading">
          <span className="admin-card__sub">{dayDetailAdmin.kicker}</span>
          <h2 className="admin-card__title">{dayDetailAdmin.title(item.staffName, longDateLabel(day.date))}</h2>
          <span className="admin-card__sub">
            {dayDetailAdmin.summary(day.classes.length, countInstitutions(day.classes))}
          </span>
        </div>
        {!legacy && <ClassStatusBadge status={day.status} day />}
      </div>

      {legacy ? (
        <section className="day-detail__section" aria-label={dayDetailAdmin.legacyTitle}>
          <h3 className="day-detail__section-title">{dayDetailAdmin.legacyTitle}</h3>
          {day.legacyClasses.map((classSession) => (
            <ClassRecordDetail key={classSession.id} item={{ staffName: item.staffName, classSession }} />
          ))}
        </section>
      ) : (
        <>
          {['start', 'end'].map((type) => (
            <CheckSection
              key={type}
              type={type}
              day={day}
              busy={busy}
              onApprove={() => onApprove(type)}
              onReject={(reason) => onReject(type, reason)}
              onRegister={onRegister && (() => onRegister(type))}
            />
          ))}

          <section className="day-detail__section" aria-label={dayDetailAdmin.classesTitle(day.classes.length)}>
            <h3 className="day-detail__section-title">{dayDetailAdmin.classesTitle(day.classes.length)}</h3>
            {groupByInstitution(day.classes).map((group) => (
              <div key={`${group.institutionId}-${group.classes[0].id}`} className="day-detail__institution">
                <strong>{group.institutionName}</strong>
                {group.classes.map((dayClass) => (
                  <span key={dayClass.id} className="admin-card__sub">
                    {formatClassMeta(dayClass)}
                  </span>
                ))}
              </div>
            ))}
          </section>

          {day.images.length > 0 && (
            <section className="day-detail__section" aria-label={dayDetailAdmin.photos(day.images.length)}>
              <h3 className="day-detail__section-title">
                {dayDetailAdmin.photos(day.images.length)}{' '}
                <span className="admin-card__sub">· {dayDetailAdmin.photosSub}</span>
              </h3>
              <ul className="class-record-detail__photos">
                {day.images.map((image, index) => (
                  <li key={image.imageKey}>
                    <a href={image.url} target="_blank" rel="noreferrer">
                      <img src={image.url} alt={dayDetailAdmin.photoAlt(index + 1)} loading="lazy" />
                    </a>
                  </li>
                ))}
              </ul>
              {day.correctedAt && (
                <span className="admin-card__sub">{dayDetailAdmin.corrected(formatDateTime(day.correctedAt))}</span>
              )}
            </section>
          )}

          {day.notes.length > 0 && (
            <section className="day-detail__section" aria-label={dayDetailAdmin.notes}>
              <h3 className="day-detail__section-title">{dayDetailAdmin.notes}</h3>
              {day.notes.map((note) => (
                <div key={note.institutionId} className="admin-form__field">
                  <span className="admin-form__label">{note.institutionName}</span>
                  <p className="admin-callout admin-callout--plain">{note.note ?? dayDetailAdmin.noNote}</p>
                </div>
              ))}
            </section>
          )}
        </>
      )}
    </section>
  )
}

export default DayDetail
