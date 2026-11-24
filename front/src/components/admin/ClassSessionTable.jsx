import { classSessionAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatTimeRange } from '../attendance/attendanceFormat'
import ClassStatusBadge from '../attendance/ClassStatusBadge'
import AdminBadge from './AdminBadge'
import { isEditableSession } from './attendanceAdminFormat'
import './AdminForm.css'
import './ClassSessionTable.css'

// 수업 할당 표 (EMP-FR-004). items는 AdminClassSessionResponse[].
// 수업마다 수정 onEdit(item)·삭제 onDelete(item). busyId인 줄은 버튼을 잠근다.
// 상태는 하루 단위 적용 전 수업 단위 기록이 있을 때만 보여 준다 (하루 기록은 수업 기록 메뉴에서).
// 직원 미정 수업(EMP-FR-021)은 직원 칸에 "미정"(지난 수업은 빨강)을 보여 주고, 고르기 칸으로 일괄 배정할 수업을 고른다.
// selectedIds: 고른 수업 id Set, onToggle(id), onToggleAll(ids, checked), today: 'YYYY-MM-DD'
function ClassSessionTable({ items, busyId, today, selectedIds, onToggle, onToggleAll, onEdit, onDelete }) {
  const { columns } = classSessionAdmin
  const unassignedIds = items.filter((item) => item.staffId === null).map((item) => item.classSession.id)
  const allSelected = unassignedIds.length > 0 && unassignedIds.every((id) => selectedIds.has(id))

  return (
    <div className="class-session-table" role="region" aria-label={classSessionAdmin.listLabel} tabIndex={0}>
      <table>
        <thead>
          <tr>
            <th scope="col" className="class-session-table__check">
              <input
                type="checkbox"
                className="class-session-table__checkbox"
                aria-label={classSessionAdmin.selectAll}
                checked={allSelected}
                disabled={unassignedIds.length === 0}
                onChange={(event) => onToggleAll(unassignedIds, event.target.checked)}
              />
            </th>
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
            const unassigned = item.staffId === null
            const past = classSession.classDate < today
            const rowLabel = `${shortDateLabel(classSession.classDate)} ${formatTimeRange(classSession)} ${title}`
            return (
              <tr
                key={classSession.id}
                className={selectedIds.has(classSession.id) ? 'class-session-table__row--selected' : undefined}
              >
                <td className="class-session-table__check">
                  {unassigned && (
                    <input
                      type="checkbox"
                      className="class-session-table__checkbox"
                      aria-label={classSessionAdmin.selectRow(rowLabel)}
                      checked={selectedIds.has(classSession.id)}
                      onChange={() => onToggle(classSession.id)}
                    />
                  )}
                </td>
                <td>{shortDateLabel(classSession.classDate)}</td>
                <td>{formatTimeRange(classSession)}</td>
                <td>{title}</td>
                <td>
                  {unassigned ? (
                    <AdminBadge tone={past ? 'red' : 'yellow'}>
                      {past ? classSessionAdmin.unassignedPast : classSessionAdmin.unassigned}
                    </AdminBadge>
                  ) : (
                    item.staffName
                  )}
                </td>
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
