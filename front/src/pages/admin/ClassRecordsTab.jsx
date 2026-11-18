import { useCallback, useState } from 'react'
import { approveDay, getAdminDays, registerDay, rejectDay } from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminNotice from '../../components/admin/AdminNotice'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import ClassFilters from '../../components/admin/ClassFilters'
import DayDetail from '../../components/admin/DayDetail'
import DayRecordTable from '../../components/admin/DayRecordTable'
import DirectStartDialog from '../../components/admin/DirectStartDialog'
import FilterChips from '../../components/admin/FilterChips'
import { addDays, dayKey, toIsoDate } from '../../components/admin/attendanceAdminFormat'
import { formatRoute } from '../../components/attendance/attendanceFormat'
import { classFilters, dayRecordAdmin } from '../../data/attendanceAdminContent'
import { dayStatuses } from '../../data/attendanceContent'
import useAdminQuery from '../useAdminQuery'
import useAttendanceOptions from './useAttendanceOptions'
import { isValidPeriod } from './attendancePeriod'

const STATUS_FILTERS = [
  { id: 'ALL', label: dayRecordAdmin.allStatuses },
  ...Object.entries(dayStatuses).map(([id, status]) => ({ id, label: status.label })),
]

// 하루 기록 (EMP-FR-003, 007, 020)과 관리자 승인·반려·직접 등록. 기본은 오늘까지 최근 7일.
// 하루를 누르면 모달로 하루 근무 상세(④)를 보여 주고, 직접 등록하면 등록 창을 닫은 뒤 바뀐 상세로 돌아온다
function ClassRecordsTab() {
  const today = toIsoDate(new Date())
  const { staffs } = useAttendanceOptions()
  const [filters, setFilters] = useState({ from: addDays(today, -6), to: today, staffId: null })
  const [status, setStatus] = useState('ALL')
  const validPeriod = isValidPeriod(filters.from, filters.to)
  const records = useAdminQuery(getAdminDays, validPeriod ? filters : null)
  const [selectedKey, setSelectedKey] = useState(null)
  // registering: 직접 등록할 'start' | 'end'
  const [registering, setRegistering] = useState(null)
  const [busy, setBusy] = useState(false)

  const items = (records.data ?? []).filter(
    (item) => status === 'ALL' || (item.day.legacyClasses.length === 0 && item.day.status === status),
  )
  const selected = records.data?.find((item) => dayKey(item) === selectedKey)
  const closeDetail = useCallback(() => setSelectedKey(null), [])
  const closeRegister = useCallback(() => setRegistering(null), [])

  const process = async (task) => {
    setBusy(true)
    try {
      const updated = await task()
      records.setData((current) => current.map((item) => (dayKey(item) === dayKey(updated) ? updated : item)))
      setRegistering(null)
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <AdminSection
      title={dayRecordAdmin.title}
      description={dayRecordAdmin.description}
      toolbar={
        <ClassFilters
          value={filters}
          staffs={staffs}
          onChange={(patch) => setFilters((current) => ({ ...current, ...patch }))}
        />
      }
    >
      <FilterChips label={dayRecordAdmin.statusLabel} options={STATUS_FILTERS} value={status} onChange={setStatus} />
      {validPeriod ? (
        <AdminQueryBody query={records} items={items} empty={dayRecordAdmin.empty}>
          {(rows) => <DayRecordTable items={rows} selectedKey={selectedKey} onSelect={setSelectedKey} />}
        </AdminQueryBody>
      ) : (
        <AdminNotice inline error message={classFilters.periodHint} />
      )}
      {selected && !registering && (
        <AdminModal label={dayRecordAdmin.detailLabel} closeLabel={dayRecordAdmin.close} onClose={closeDetail}>
          <DayDetail
            key={`${selectedKey}-${selected.day.status}`}
            item={selected}
            busy={busy}
            onApprove={(type) => process(() => approveDay(selected.staffId, selected.day.date, type))}
            onReject={(type, reason) => process(() => rejectDay(selected.staffId, selected.day.date, type, reason))}
            onRegister={setRegistering}
          />
        </AdminModal>
      )}
      {selected && registering && (
        <DirectStartDialog
          type={registering}
          item={selected}
          summary={dayRecordAdmin.route(formatRoute(selected.day), selected.day.classes.length)}
          busy={busy}
          onSubmit={(reason) => process(() => registerDay(selected.staffId, selected.day.date, registering, reason))}
          onClose={closeRegister}
        />
      )}
    </AdminSection>
  )
}

export default ClassRecordsTab
