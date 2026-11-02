import { useCallback, useState } from 'react'
import { createClassSessions, deleteClassSession, getClassRecords, updateClassSession } from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminNotice from '../../components/admin/AdminNotice'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import ClassFilters from '../../components/admin/ClassFilters'
import ClassSessionForm from '../../components/admin/ClassSessionForm'
import ClassSessionTable from '../../components/admin/ClassSessionTable'
import { addDays, toIsoDate } from '../../components/admin/attendanceAdminFormat'
import { formatTime } from '../../components/attendance/attendanceFormat'
import { classFilters, classSessionAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'
import { isValidPeriod } from './attendancePeriod'
import useAttendanceOptions from './useAttendanceOptions'
import '../../components/admin/AdminForm.css'

// 모달에서 새 수업을 등록하는 중
const NEW = 'new'

// 수업 할당 (EMP-FR-004). 기본은 오늘부터 2주.
// "수업 등록"을 누르면 반복 등록 폼을, 목록의 수정을 누르면 그 수업 수정 폼을 모달로 연다
function ClassSessionsTab() {
  const today = toIsoDate(new Date())
  const { staffs, institutions } = useAttendanceOptions()
  const [filters, setFilters] = useState({ from: today, to: addDays(today, 13), staffId: null, institutionId: null })
  const validPeriod = isValidPeriod(filters.from, filters.to)
  const sessions = useAdminQuery(getClassRecords, validPeriod ? filters : null)
  // null이면 모달 닫힘, NEW면 반복 등록, AdminClassSessionResponse면 그 수업 수정
  const [editing, setEditing] = useState(null)
  const [busy, setBusy] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [message, setMessage] = useState('')

  const close = useCallback(() => setEditing(null), [])
  const openForm = (target) => {
    setMessage('')
    setEditing(target)
  }

  const save = async (action, doneMessage) => {
    setBusy(true)
    try {
      const result = await action()
      setEditing(null)
      setMessage(doneMessage(result))
      sessions.reload()
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusy(false)
    }
  }

  const remove = async (item) => {
    const { classSession } = item
    const label = `${shortDateLabel(classSession.classDate)} ${formatTime(classSession.startTime)} ${item.staffName}`
    if (!window.confirm(classSessionAdmin.deleteConfirm(label))) return
    setBusyId(classSession.id)
    try {
      await deleteClassSession(classSession.id)
      sessions.setData((current) => current.filter((row) => row.classSession.id !== classSession.id))
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusyId(null)
    }
  }

  const editingSession = editing === NEW ? null : editing
  return (
    <AdminSection
      title={classSessionAdmin.title}
      description={classSessionAdmin.description}
      toolbar={
        <ClassFilters
          value={filters}
          staffs={staffs}
          institutions={institutions}
          onChange={(patch) => setFilters((current) => ({ ...current, ...patch }))}
        >
          <button type="button" className="admin-form__button admin-form__button--dark" onClick={() => openForm(NEW)}>
            {classSessionAdmin.add}
          </button>
        </ClassFilters>
      }
    >
      {message && (
        <p className="admin-callout admin-callout--info" role="status">
          {message}
        </p>
      )}
      {validPeriod ? (
        <AdminQueryBody query={sessions} empty={classSessionAdmin.empty}>
          {(rows) => (
            <ClassSessionTable mode="sessions" items={rows} busyId={busyId} onEdit={openForm} onDelete={remove} />
          )}
        </AdminQueryBody>
      ) : (
        <AdminNotice inline error message={classFilters.periodHint} />
      )}
      <p className="admin-section__description">{classSessionAdmin.lockedHint}</p>

      {editing && (
        <AdminModal
          label={editingSession ? classSessionAdmin.editTitle : classSessionAdmin.createTitle}
          closeLabel={classSessionAdmin.close}
          onClose={close}
        >
          <ClassSessionForm
            key={editingSession ? editingSession.classSession.id : NEW}
            institutions={institutions}
            staffs={staffs}
            editing={editingSession}
            defaultDate={today}
            busy={busy}
            onCreate={(body) =>
              save(() => createClassSessions(body), (created) => classSessionAdmin.created(created.length))
            }
            onUpdate={(id, body) => save(() => updateClassSession(id, body), () => classSessionAdmin.updated)}
            onCancelEdit={close}
          />
        </AdminModal>
      )}
    </AdminSection>
  )
}

export default ClassSessionsTab
