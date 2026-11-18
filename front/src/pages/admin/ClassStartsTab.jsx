import { useCallback, useEffect, useState } from 'react'
import { approveDay, approveStart, getPendingDays, getPendingStarts, rejectDay, rejectStart } from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import ClassStartReview from '../../components/admin/ClassStartReview'
import DayDetail from '../../components/admin/DayDetail'
import PendingDayList from '../../components/admin/PendingDayList'
import { dayKey } from '../../components/admin/attendanceAdminFormat'
import PendingStartList from '../../components/admin/PendingStartList'
import { classStartAdmin } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'

const NO_PARAMS = {}

function stillPending(item) {
  return item.day.start?.status === 'PENDING' || item.day.end?.status === 'PENDING'
}

// 수업 시작·종료 승인 (EMP-FR-020). 하루를 누르면 모달(④ 하루 근무 상세)에서 시작·종료를 각각 승인·반려한다.
// 하루 단위 적용 전 수업 단위 승인 대기(EMP-FR-011)가 남아 있으면 아래에 따로 보여 준다.
// 처리가 끝난 항목은 목록에서 빼고, 남은 건수를 메뉴 배지에 알린다(onPendingCountChange)
function ClassStartsTab({ onPendingCountChange }) {
  const days = useAdminQuery(getPendingDays, NO_PARAMS)
  const legacy = useAdminQuery(getPendingStarts, NO_PARAMS)
  // selected: { dayKey } | { legacyId } | null
  const [selected, setSelected] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (days.data && legacy.data) onPendingCountChange?.(days.data.length + legacy.data.length)
  }, [days.data, legacy.data, onPendingCountChange])

  const selectedDay = selected?.dayKey ? days.data?.find((item) => dayKey(item) === selected.dayKey) : null
  const selectedLegacy =
    selected?.legacyId != null ? legacy.data?.find((item) => item.classSession.id === selected.legacyId) : null
  const closeModal = useCallback(() => setSelected(null), [])

  const run = async (task, onDone, reload) => {
    setBusy(true)
    try {
      onDone(await task())
    } catch (error) {
      window.alert(error.message)
      reload()
    } finally {
      setBusy(false)
    }
  }

  // 처리한 하루가 아직 다른 쪽 승인 대기면 목록에 남기고 상세도 그대로 둔다
  const processDay = (task) =>
    run(
      task,
      (updated) => {
        const key = dayKey(updated)
        days.setData((items) =>
          stillPending(updated)
            ? items.map((item) => (dayKey(item) === key ? updated : item))
            : items.filter((item) => dayKey(item) !== key),
        )
        if (!stillPending(updated)) setSelected(null)
      },
      days.reload,
    )

  const processLegacy = (action) =>
    run(
      () => action(selected.legacyId),
      () => {
        legacy.setData((items) => items.filter((item) => item.classSession.id !== selected.legacyId))
        setSelected(null)
      },
      legacy.reload,
    )

  return (
    <AdminSection title={classStartAdmin.title} description={classStartAdmin.description}>
      <AdminQueryBody query={days} empty={classStartAdmin.empty}>
        {(items) => (
          <>
            <p className="admin-section__description">{classStartAdmin.count(items.length)}</p>
            <PendingDayList
              items={items}
              selectedKey={selected?.dayKey}
              onSelect={(key) => setSelected({ dayKey: key })}
            />
          </>
        )}
      </AdminQueryBody>
      {legacy.data?.length > 0 && (
        <>
          <h3 className="admin-section__description">{classStartAdmin.legacyTitle}</h3>
          <PendingStartList
            items={legacy.data}
            selectedId={selected?.legacyId}
            onSelect={(id) => setSelected({ legacyId: id })}
          />
        </>
      )}
      {selectedDay && (
        <AdminModal label={classStartAdmin.title} closeLabel={classStartAdmin.close} onClose={closeModal}>
          <DayDetail
            key={`${dayKey(selectedDay)}-${selectedDay.day.status}`}
            item={selectedDay}
            busy={busy}
            onApprove={(type) => processDay(() => approveDay(selectedDay.staffId, selectedDay.day.date, type))}
            onReject={(type, reason) =>
              processDay(() => rejectDay(selectedDay.staffId, selectedDay.day.date, type, reason))
            }
          />
        </AdminModal>
      )}
      {selectedLegacy && (
        <AdminModal label={classStartAdmin.title} closeLabel={classStartAdmin.close} onClose={closeModal}>
          <ClassStartReview
            key={selectedLegacy.classSession.id}
            item={selectedLegacy}
            busy={busy}
            onApprove={() => processLegacy(approveStart)}
            onReject={(reason) => processLegacy((id) => rejectStart(id, reason))}
          />
        </AdminModal>
      )}
    </AdminSection>
  )
}

export default ClassStartsTab
