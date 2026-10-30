import { useCallback, useState } from 'react'
import { getClassRecords, registerStart } from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminNotice from '../../components/admin/AdminNotice'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import ClassFilters from '../../components/admin/ClassFilters'
import ClassRecordDetail from '../../components/admin/ClassRecordDetail'
import ClassSessionTable from '../../components/admin/ClassSessionTable'
import DirectStartDialog from '../../components/admin/DirectStartDialog'
import FilterChips from '../../components/admin/FilterChips'
import { addDays, toIsoDate } from '../../components/admin/attendanceAdminFormat'
import { classFilters, classRecordAdmin } from '../../data/attendanceAdminContent'
import { classStatuses } from '../../data/attendanceContent'
import useAdminQuery from '../useAdminQuery'
import useAttendanceOptions from './useAttendanceOptions'
import { isValidPeriod } from './attendancePeriod'

const STATUS_FILTERS = [
  { id: 'ALL', label: classRecordAdmin.allStatuses },
  ...Object.entries(classStatuses).map(([id, status]) => ({ id, label: status.label })),
]

// 수업 기록 (EMP-FR-003, 007)과 관리자 직접 등록 (EMP-FR-011). 기본은 오늘까지 최근 7일.
// 수업을 누르면 모달로 상세를 보여 주고, 상세에서 직접 등록하면 등록 창을 닫은 뒤 바뀐 상세로 돌아온다
function ClassRecordsTab() {
  const today = toIsoDate(new Date())
  const { staffs } = useAttendanceOptions()
  const [filters, setFilters] = useState({ from: addDays(today, -6), to: today, staffId: null })
  const [status, setStatus] = useState('ALL')
  const validPeriod = isValidPeriod(filters.from, filters.to)
  const records = useAdminQuery(getClassRecords, validPeriod ? filters : null)
  const [selectedId, setSelectedId] = useState(null)
  const [registering, setRegistering] = useState(null)
  const [busy, setBusy] = useState(false)

  const items = (records.data ?? []).filter((item) => status === 'ALL' || item.classSession.status === status)
  const selected = records.data?.find((item) => item.classSession.id === selectedId)
  const closeDetail = useCallback(() => setSelectedId(null), [])
  const closeRegister = useCallback(() => setRegistering(null), [])

  const register = async (reason) => {
    setBusy(true)
    try {
      const updated = await registerStart(registering.classSession.id, reason)
      records.setData((current) =>
        current.map((item) => (item.classSession.id === updated.classSession.id ? updated : item)),
      )
      setRegistering(null)
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <AdminSection
      title={classRecordAdmin.title}
      description={classRecordAdmin.description}
      toolbar={
        <ClassFilters
          value={filters}
          staffs={staffs}
          onChange={(patch) => setFilters((current) => ({ ...current, ...patch }))}
        />
      }
    >
      <FilterChips label={classRecordAdmin.statusLabel} options={STATUS_FILTERS} value={status} onChange={setStatus} />
      {validPeriod ? (
        <AdminQueryBody query={records} items={items} empty={classRecordAdmin.empty}>
          {(rows) => (
            <ClassSessionTable
              mode="records"
              items={rows}
              selectedId={selectedId}
              onSelect={setSelectedId}
              onRegister={setRegistering}
            />
          )}
        </AdminQueryBody>
      ) : (
        <AdminNotice inline error message={classFilters.periodHint} />
      )}
      {selected && !registering && (
        <AdminModal label={classRecordAdmin.detailLabel} closeLabel={classRecordAdmin.close} onClose={closeDetail}>
          <ClassRecordDetail item={selected} onRegister={() => setRegistering(selected)} />
        </AdminModal>
      )}
      {registering && (
        <DirectStartDialog item={registering} busy={busy} onSubmit={register} onClose={closeRegister} />
      )}
    </AdminSection>
  )
}

export default ClassRecordsTab
