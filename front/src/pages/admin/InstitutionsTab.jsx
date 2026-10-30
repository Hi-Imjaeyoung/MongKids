import { useCallback, useState } from 'react'
import { createInstitution, deleteInstitution, getInstitutions, updateInstitution } from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import InstitutionForm from '../../components/admin/InstitutionForm'
import InstitutionList from '../../components/admin/InstitutionList'
import { institutionAdmin } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'
import '../../components/admin/AdminForm.css'

const NO_PARAMS = {}
// 모달에서 새 기관을 등록하는 중
const NEW = 'new'

// 기관 위치 (EMP-FR-012). 목록에서 기관을 누르면 수정, "새 기관 등록"을 누르면 등록 폼을 모달로 연다
function InstitutionsTab() {
  const institutions = useAdminQuery(getInstitutions, NO_PARAMS)
  // null이면 모달 닫힘, NEW면 새 기관, 숫자면 그 기관 수정
  const [editingId, setEditingId] = useState(null)
  const [busy, setBusy] = useState(false)

  const editing = institutions.data?.find((institution) => institution.id === editingId) ?? null
  const close = useCallback(() => setEditingId(null), [])

  const save = async (body) => {
    setBusy(true)
    try {
      const saved = editing ? await updateInstitution(editing.id, body) : await createInstitution(body)
      institutions.setData((current) =>
        editing ? current.map((item) => (item.id === saved.id ? saved : item)) : [...current, saved],
      )
      close()
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusy(false)
    }
  }

  const remove = async () => {
    if (!window.confirm(institutionAdmin.deleteConfirm(editing.name))) return
    setBusy(true)
    try {
      await deleteInstitution(editing.id)
      institutions.setData((current) => current.filter((item) => item.id !== editing.id))
      close()
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusy(false)
    }
  }

  const open = editingId === NEW || editing !== null
  return (
    <AdminSection
      title={institutionAdmin.title}
      description={institutionAdmin.description}
      toolbar={
        <button type="button" className="admin-form__button admin-form__button--ghost" onClick={() => setEditingId(NEW)}>
          {institutionAdmin.add}
        </button>
      }
    >
      <AdminQueryBody query={institutions} empty={institutionAdmin.empty}>
        {(items) => (
          <InstitutionList institutions={items} selectedId={editingId} onSelect={(item) => setEditingId(item.id)} />
        )}
      </AdminQueryBody>
      {open && (
        <AdminModal
          wide
          label={editing ? institutionAdmin.editTitle(editing.name) : institutionAdmin.createTitle}
          closeLabel={institutionAdmin.close}
          onClose={close}
        >
          <InstitutionForm
            key={editingId}
            institution={editing}
            busy={busy}
            onSave={save}
            onDelete={remove}
            onCancel={close}
          />
        </AdminModal>
      )}
    </AdminSection>
  )
}

export default InstitutionsTab
