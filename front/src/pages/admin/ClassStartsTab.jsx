import { useCallback, useEffect, useState } from 'react'
import { approveStart, getPendingStarts, rejectStart } from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import ClassStartReview from '../../components/admin/ClassStartReview'
import PendingStartList from '../../components/admin/PendingStartList'
import { classStartAdmin } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'

const NO_PARAMS = {}

// 수업 시작 승인 (EMP-FR-011). 수업을 누르면 모달에서 승인·반려한다.
// 처리한 수업은 목록에서 빼고, 남은 건수를 메뉴 배지에 알린다(onPendingCountChange)
function ClassStartsTab({ onPendingCountChange }) {
  const pending = useAdminQuery(getPendingStarts, NO_PARAMS)
  const [selectedId, setSelectedId] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (pending.data) onPendingCountChange?.(pending.data.length)
  }, [pending.data, onPendingCountChange])

  const selected = pending.data?.find((item) => item.classSession.id === selectedId)
  const closeModal = useCallback(() => setSelectedId(null), [])

  const process = async (action) => {
    setBusy(true)
    try {
      await action(selectedId)
      pending.setData((items) => items.filter((item) => item.classSession.id !== selectedId))
      setSelectedId(null)
    } catch (error) {
      window.alert(error.message)
      pending.reload()
    } finally {
      setBusy(false)
    }
  }

  return (
    <AdminSection title={classStartAdmin.title} description={classStartAdmin.description}>
      <AdminQueryBody query={pending} empty={classStartAdmin.empty}>
        {(items) => (
          <>
            <p className="admin-section__description">{classStartAdmin.count(items.length)}</p>
            <PendingStartList items={items} selectedId={selectedId} onSelect={setSelectedId} />
          </>
        )}
      </AdminQueryBody>
      {selected && (
        <AdminModal label={classStartAdmin.title} closeLabel={classStartAdmin.close} onClose={closeModal}>
          <ClassStartReview
            key={selected.classSession.id}
            item={selected}
            busy={busy}
            onApprove={() => process(approveStart)}
            onReject={(reason) => process((id) => rejectStart(id, reason))}
          />
        </AdminModal>
      )}
    </AdminSection>
  )
}

export default ClassStartsTab
