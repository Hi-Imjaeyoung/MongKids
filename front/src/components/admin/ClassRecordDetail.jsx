import { classRecordAdmin, longDateLabel, startMethods } from '../../data/attendanceAdminContent'
import { formatClock, formatTimeRange } from '../attendance/attendanceFormat'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import { formatDateTime } from './adminFormat'
import StartLocation from './StartLocation'
import './AdminForm.css'
import './ClassRecordDetail.css'

// 하루 단위 적용 전 수업 한 회의 시작·종료 기록 (EMP-FR-003, 007). 읽기 전용, 하루 상세(DayDetail)에서 보여 준다.
// item: { staffName, classSession }
function ClassRecordDetail({ item }) {
  const { classSession } = item
  const { start, end } = classSession
  const { fields } = classRecordAdmin

  return (
    <section className="admin-card" aria-label={classRecordAdmin.detailLabel}>
      <div className="admin-card__head">
        <h2 className="admin-card__title">
          {item.staffName} · {classSession.target ?? classSession.institutionName}
        </h2>
        <ClassStatusBadge status={classSession.status} />
      </div>
      <dl className="admin-fields">
        <dt>{fields.institution}</dt>
        <dd>{classSession.institutionName}</dd>
        <dt>{fields.classTime}</dt>
        <dd>
          {longDateLabel(classSession.classDate)} {formatTimeRange(classSession)}
        </dd>
        <dt>{fields.start}</dt>
        <dd>
          {start ? (
            <>
              {formatClock(start.startedAt)} · {startMethods[start.method]}
              <br />
              <span className="admin-inline-list admin-card__sub">
                <StartLocation start={start} />
              </span>
            </>
          ) : (
            classRecordAdmin.noEnd
          )}
        </dd>
        {start?.reason && (
          <>
            <dt>{fields.reason}</dt>
            <dd>{start.reason}</dd>
          </>
        )}
        {start?.rejectReason && (
          <>
            <dt>{fields.rejectReason}</dt>
            <dd>{start.rejectReason}</dd>
          </>
        )}
        <dt>{fields.startNote}</dt>
        <dd>{start?.note ?? classRecordAdmin.noNote}</dd>
        <dt>{fields.end}</dt>
        <dd>
          {end ? formatClock(end.endedAt) : classRecordAdmin.noEnd}
          {end?.correctedAt && (
            <span className="admin-card__sub"> ({classRecordAdmin.corrected(formatDateTime(end.correctedAt))})</span>
          )}
        </dd>
      </dl>

      {end && (
        <>
          <div className="admin-form__field">
            <span className="admin-form__label">{classRecordAdmin.photos(end.images.length)}</span>
            <ul className="class-record-detail__photos">
              {end.images.map((image, index) => (
                <li key={image.imageKey}>
                  <a href={image.url} target="_blank" rel="noreferrer">
                    <img src={image.url} alt={classRecordAdmin.photoAlt(index + 1)} loading="lazy" />
                  </a>
                </li>
              ))}
            </ul>
          </div>
          <div className="admin-form__field">
            <span className="admin-form__label">{classRecordAdmin.note}</span>
            <p className="admin-callout admin-callout--plain">{end.note ?? classRecordAdmin.noNote}</p>
          </div>
        </>
      )}

    </section>
  )
}

export default ClassRecordDetail
