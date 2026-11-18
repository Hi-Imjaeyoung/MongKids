import { classSessionAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatTimeRange } from '../attendance/attendanceFormat'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import { isEditableSession } from './attendanceAdminFormat'
import './AdminForm.css'
import './ClassSessionTable.css'

// 수업 할당 표 (EMP-FR-004). items는 AdminClassSessionResponse[].
// 수업마다 수정 onEdit(item)·삭제 onDelete(item). busyId인 줄은 버튼을 잠근다.
// 상태는 하루 단위 적용 전 수업 단위 기록이 있을 때만 보여 준다 (하루 기록은 수업 기록 메뉴에서)
function ClassSessionTable({ items, busyId, onEdit, onDelete }) {
  const { columns } = classSessionAdmin

  return (
    <div className="class-session-table" role="region" aria-label={classSessionAdmin.listLabel} tabIndex={0}>
      <table>
        <thead>
          <tr>
            <th scope="col">{columns.date}</th>
            <th scope="col">{columns.time}</th>
            <th scope="col">{columns.institution}</th>
            <th scope="col">{columns.staff}</th>
            <th scope="col">{columns.status}</th>
            <th scope="col">{columns.actions}</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => {
            const { classSession } = item
            const title = [classSession.institutionName, classSession.target].filter(Boolean).join(' ')
            const editable = isEditableSession(classSession)
            const busy = busyId === classSession.id
            return (
              <tr key={classSession.id}>
                <td>{shortDateLabel(classSession.classDate)}</td>
                <td>{formatTimeRange(classSession)}</td>
                <td>{title}</td>
                <td>{item.staffName}</td>
                <td>{classSession.status === 'NOT_STARTED' ? '-' : <ClassStatusBadge status={classSession.status} />}</td>
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
