import { useCallback, useState } from 'react'
import { createInstitution, deleteInstitution, getInstitutions, updateInstitution } from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import InstitutionDeleteConfirm from '../../components/admin/InstitutionDeleteConfirm'
import InstitutionForm from '../../components/admin/InstitutionForm'
import InstitutionList from '../../components/admin/InstitutionList'
import { institutionAdmin } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'
import '../../components/admin/AdminForm.css'

const NO_PARAMS = {}
// 모달에서 새 기관을 등록하는 중
const NEW = 'new'

// 기관 위치 (EMP-FR-012). 목록에서 기관을 누르면 수정, "새 기관 등록"을 누르면 등록 폼을 모달로 연다.
// 모달은 입력 중 실수로 닫히지 않게 배경을 눌러도 닫히지 않는다(닫기·취소 버튼과 Esc만). 삭제는 같은 모달에서 확인 화면으로 바꾼다
function InstitutionsTab() {
  const institutions = useAdminQuery(getInstitutions, NO_PARAMS)
  // null이면 모달 닫힘, NEW면 새 기관, 숫자면 그 기관 수정
  const [editingId, setEditingId] = useState(null)
  const [busy, setBusy] = useState(false)
  // 수정 모달 안에서 삭제 확인 화면을 보여 주는 중
  const [confirmingDelete, setConfirmingDelete] = useState(false)

  const editing = institutions.data?.find((institution) => institution.id === editingId) ?? null
  const close = useCallback(() => {
    setEditingId(null)
    setConfirmingDelete(false)
  }, [])

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
          label={
            confirmingDelete
              ? institutionAdmin.deleteDialog.title(editing.name)
              : editing
                ? institutionAdmin.editTitle(editing.name)
                : institutionAdmin.createTitle
          }
          closeLabel={institutionAdmin.close}
          closeOnBackdrop={false}
          onClose={close}
        >
          {confirmingDelete ? (
            <InstitutionDeleteConfirm
              institution={editing}
              busy={busy}
              onConfirm={remove}
              onCancel={() => setConfirmingDelete(false)}
            />
          ) : (
            <InstitutionForm
              key={editingId}
              institution={editing}
              busy={busy}
              onSave={save}
              onDelete={() => setConfirmingDelete(true)}
              onCancel={close}
            />
          )}
        </AdminModal>
      )}
    </AdminSection>
  )
}

export default InstitutionsTab
