import { classRecordAdmin, classSessionAdmin, locationText, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatClock, formatTime, formatTimeRange } from '../attendance/attendanceFormat'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import { canRegisterStart, isEditableSession } from './attendanceAdminFormat'
import './AdminForm.css'
import './ClassSessionTable.css'

function startText(start) {
  const distance = start.distanceMeters === null ? locationText.none : `${start.distanceMeters}m`
  return `${formatClock(start.startedAt)} · ${distance}`
}

function endText(end) {
  return `${formatClock(end.endedAt)} · ${classRecordAdmin.photoCount(end.images.length)}`
}

// 근태 관리자 수업 표. items는 AdminClassSessionResponse[].
// mode 'records'(EMP-FR-003): 수업을 고르면 onSelect(id), 시작 전·반려 수업에 직접 등록 onRegister(item).
// mode 'sessions'(EMP-FR-004): 수업마다 수정 onEdit(item)·삭제 onDelete(item). busyId인 줄은 버튼을 잠근다
function ClassSessionTable({ mode, items, selectedId, busyId, onSelect, onRegister, onEdit, onDelete }) {
  const records = mode === 'records'
  const text = records ? classRecordAdmin : classSessionAdmin
  const { columns } = text

  return (
    <div className="class-session-table" role="region" aria-label={text.listLabel} tabIndex={0}>
      <table>
        <thead>
          <tr>
            {records ? (
              <>
                <th scope="col">{columns.date}</th>
                <th scope="col">{columns.institution}</th>
                <th scope="col">{columns.staff}</th>
                <th scope="col">{columns.status}</th>
                <th scope="col">{columns.start}</th>
                <th scope="col">{columns.end}</th>
              </>
            ) : (
              <>
                <th scope="col">{columns.date}</th>
                <th scope="col">{columns.time}</th>
                <th scope="col">{columns.institution}</th>
                <th scope="col">{columns.staff}</th>
                <th scope="col">{columns.status}</th>
                <th scope="col">{columns.actions}</th>
              </>
            )}
          </tr>
        </thead>
        <tbody>
          {items.map((item) => {
            const { classSession } = item
            const selected = classSession.id === selectedId
            const title = [classSession.institutionName, classSession.target].filter(Boolean).join(' ')
            if (records) {
              return (
                <tr key={classSession.id} className={selected ? 'class-session-table__row--selected' : undefined}>
                  <td>
                    {shortDateLabel(classSession.classDate)} {formatTime(classSession.startTime)}
                  </td>
                  <td>
                    <button
                      type="button"
                      className="class-session-table__title"
                      aria-pressed={selected}
                      onClick={() => onSelect(classSession.id)}
                    >
                      {title}
                    </button>
                  </td>
                  <td>{item.staffName}</td>
                  <td>
                    <ClassStatusBadge status={classSession.status} />
                  </td>
                  <td>
                    {canRegisterStart(classSession) ? (
                      <button
                        type="button"
                        className="admin-form__button admin-form__button--ghost admin-form__button--sm"
                        onClick={() => onRegister(item)}
                      >
                        {classRecordAdmin.directRegister}
                      </button>
                    ) : (
                      startText(classSession.start)
                    )}
                  </td>
                  <td>{classSession.end ? endText(classSession.end) : classRecordAdmin.noEnd}</td>
                </tr>
              )
            }
            const editable = isEditableSession(classSession)
            const busy = busyId === classSession.id
            return (
              <tr key={classSession.id}>
                <td>{shortDateLabel(classSession.classDate)}</td>
                <td>{formatTimeRange(classSession)}</td>
                <td>{title}</td>
                <td>{item.staffName}</td>
                <td>
                  <ClassStatusBadge status={classSession.status} />
                </td>
                <td>
                  <div className="class-session-table__actions">
                    <button
                      type="button"
                      className="admin-form__button admin-form__button--ghost admin-form__button--sm"
                      disabled={!editable || busy}
                      title={editable ? undefined : classSessionAdmin.lockedTitle}
                      onClick={() => onEdit(item)}
                    >
                      {classSessionAdmin.edit}
                    </button>
                    <button
                      type="button"
                      className="admin-form__button admin-form__button--danger admin-form__button--sm"
                      disabled={!editable || busy}
                      title={editable ? undefined : classSessionAdmin.lockedTitle}
                      onClick={() => onDelete(item)}
                    >
                      {classSessionAdmin.delete}
                    </button>
                  </div>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default ClassSessionTable
