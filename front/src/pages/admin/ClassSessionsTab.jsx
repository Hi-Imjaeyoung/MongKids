import { useCallback, useState } from 'react'
import {
  assignClassSessionsStaff,
  createClassSessions,
  deleteClassSession,
  getClassRecords,
  updateClassSession,
} from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminNotice from '../../components/admin/AdminNotice'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import ClassAssignBar from '../../components/admin/ClassAssignBar'
import ClassFilters from '../../components/admin/ClassFilters'
import ClassSessionForm from '../../components/admin/ClassSessionForm'
import ClassSessionTable from '../../components/admin/ClassSessionTable'
import FilterChips from '../../components/admin/FilterChips'
import { addDays, toIsoDate } from '../../components/admin/attendanceAdminFormat'
import { formatTime } from '../../components/attendance/attendanceFormat'
import { classFilters, classSessionAdmin, seriesAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'
import { isValidPeriod } from './attendancePeriod'
import UnassignedSeriesView from './UnassignedSeriesView'
import useAttendanceOptions from './useAttendanceOptions'
import '../../components/admin/AdminForm.css'

// 모달에서 새 수업을 등록하는 중
const NEW = 'new'
const VIEWS = [
  { id: 'sessions', label: seriesAdmin.views.sessions },
  { id: 'series', label: seriesAdmin.views.series },
]

// 수업 할당 (EMP-FR-004). 기본은 오늘부터 2주.
// "수업 등록"을 누르면 반복 등록 폼을, 목록의 수정을 누르면 그 수업 수정 폼을 모달로 연다.
// 직원 미정 수업(EMP-FR-021)은 고르기 칸으로 골라 직원 한 명에게 한꺼번에 배정한다.
// "미정 수업 배정" 보기(EMP-SCR-007)에서는 정기 수업 묶음마다 추천 직원을 보고 오늘 이후 회차를 배정한다
function ClassSessionsTab() {
  const [view, setView] = useState('sessions')
  const viewSwitch = <FilterChips label={seriesAdmin.viewsLabel} options={VIEWS} value={view} onChange={setView} />
  if (view === 'series') {
    return (
      <AdminSection title={classSessionAdmin.title} description={classSessionAdmin.description} toolbar={viewSwitch}>
        <UnassignedSeriesView />
      </AdminSection>
    )
  }
  return <SessionsView viewSwitch={viewSwitch} />
}

function SessionsView({ viewSwitch }) {
  const today = toIsoDate(new Date())
  const { staffs, institutions } = useAttendanceOptions()
  const [filters, setFilters] = useState({
    from: today,
    to: addDays(today, 13),
    staffId: null,
    unassigned: false,
    institutionId: null,
  })
  const validPeriod = isValidPeriod(filters.from, filters.to)
  const sessions = useAdminQuery(getClassRecords, validPeriod ? filters : null)
  // null이면 모달 닫힘, NEW면 반복 등록, AdminClassSessionResponse면 그 수업 수정
  const [editing, setEditing] = useState(null)
  const [busy, setBusy] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [message, setMessage] = useState('')
  // 일괄 배정할 직원 미정 수업 id
  const [selectedIds, setSelectedIds] = useState(() => new Set())

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
      setSelectedIds(new Set())
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

  const toggle = (id) =>
    setSelectedIds((current) => {
      const next = new Set(current)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  const toggleAll = (ids, checked) =>
    setSelectedIds((current) => {
      const next = new Set(current)
      ids.forEach((id) => (checked ? next.add(id) : next.delete(id)))
      return next
    })
  const changeFilters = (patch) => {
    setSelectedIds(new Set())
    setFilters((current) => ({ ...current, ...patch }))
  }
  const assign = (staffId) => {
    setMessage('')
    const name = staffs.find((staff) => staff.id === staffId)?.name ?? ''
    return save(
      () => assignClassSessionsStaff(visibleSelected, staffId),
      (assigned) => classSessionAdmin.assigned(assigned.length, name),
    )
  }
  // 보이는 목록에 남은 것만 (필터를 바꾸면 고른 것을 비운다)
  const visibleSelected = [...selectedIds].filter((id) => sessions.data?.some((row) => row.classSession.id === id))

  const editingSession = editing === NEW ? null : editing
  return (
    <AdminSection
      title={classSessionAdmin.title}
      description={classSessionAdmin.description}
      toolbar={
        <>
          {viewSwitch}
          <ClassFilters
            value={filters}
            staffs={staffs}
            institutions={institutions}
            allowUnassigned
            onChange={changeFilters}
          >
            <button type="button" className="admin-form__button admin-form__button--dark" onClick={() => openForm(NEW)}>
              {classSessionAdmin.add}
            </button>
          </ClassFilters>
        </>
      }
    >
      {message && (
        <p className="admin-callout admin-callout--info" role="status">
          {message}
        </p>
      )}
      {visibleSelected.length > 0 && (
        <ClassAssignBar
          count={visibleSelected.length}
          staffs={staffs}
          busy={busy}
          onAssign={assign}
          onClear={() => setSelectedIds(new Set())}
        />
      )}
      {validPeriod ? (
        <AdminQueryBody query={sessions} empty={classSessionAdmin.empty}>
          {(rows) => (
            <ClassSessionTable
              items={rows}
              busyId={busyId}
              today={today}
              selectedIds={selectedIds}
              onToggle={toggle}
              onToggleAll={toggleAll}
              onEdit={openForm}
              onDelete={remove}
            />
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
