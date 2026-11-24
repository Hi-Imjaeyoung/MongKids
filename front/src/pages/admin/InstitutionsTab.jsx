import { useCallback, useState } from 'react'
import {
  createClassSessions,
  createInstitution,
  deleteInstitution,
  getInstitutions,
  updateInstitution,
} from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import ClassSessionForm from '../../components/admin/ClassSessionForm'
import InstitutionDeleteConfirm from '../../components/admin/InstitutionDeleteConfirm'
import InstitutionForm from '../../components/admin/InstitutionForm'
import InstitutionList from '../../components/admin/InstitutionList'
import { toIsoDate } from '../../components/admin/attendanceAdminFormat'
import { classSessionAdmin, institutionAdmin } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'
import useAttendanceOptions from './useAttendanceOptions'
import '../../components/admin/AdminForm.css'

const NO_PARAMS = {}
const CLOSED = { type: null, id: null }

// 기관 (EMP-FR-012, EMP-SCR-006 11-18 초안 "기관 (표 형태 목록)"). 표의 줄마다 수업 추가·수정·삭제를 모달로 연다.
// 모달은 입력 중 실수로 닫히지 않게 배경을 눌러도 닫히지 않는다(닫기·취소 버튼과 Esc만).
// 수정 폼에서 삭제를 누르면 같은 모달이 삭제 확인으로 바뀌고 "돌아가기"로 폼에 돌아간다
function InstitutionsTab() {
  const today = toIsoDate(new Date())
  const institutions = useAdminQuery(getInstitutions, NO_PARAMS)
  const { staffs } = useAttendanceOptions()
  // type: 'new'(새 기관) · 'edit'(수정) · 'delete'(삭제 확인) · 'addClass'(수업 추가). fromForm: 수정 폼에서 삭제로 넘어옴
  const [modal, setModal] = useState(CLOSED)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')

  const target = institutions.data?.find((institution) => institution.id === modal.id) ?? null
  const close = useCallback(() => setModal(CLOSED), [])
  const openModal = (type, institution = null, fromForm = false) => {
    setMessage('')
    setModal({ type, id: institution?.id ?? null, fromForm })
  }

  const run = async (action) => {
    setBusy(true)
    try {
      await action()
      close()
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusy(false)
    }
  }

  const save = (body) =>
    run(async () => {
      const saved = target ? await updateInstitution(target.id, body) : await createInstitution(body)
      institutions.setData((current) =>
        target ? current.map((item) => (item.id === saved.id ? saved : item)) : [...current, saved],
      )
    })

  const remove = () =>
    run(async () => {
      await deleteInstitution(target.id)
      institutions.setData((current) => current.filter((item) => item.id !== target.id))
    })

  const addClasses = (body) =>
    run(async () => {
      const created = await createClassSessions(body)
      setMessage(classSessionAdmin.created(created.length))
      institutions.reload()
    })

  const open = modal.type === 'new' || target !== null
  const labels = {
    new: institutionAdmin.createTitle,
    edit: target && institutionAdmin.editTitle(target.name),
    delete: target && institutionAdmin.deleteDialog.title(target.name),
    addClass: target && institutionAdmin.addClassTitle(target.name),
  }

  return (
    <AdminSection
      title={institutionAdmin.title}
      description={institutionAdmin.description}
      toolbar={
        <button type="button" className="admin-form__button admin-form__button--dark" onClick={() => openModal('new')}>
          {institutionAdmin.add}
        </button>
      }
    >
      {message && (
        <p className="admin-callout admin-callout--info" role="status">
          {message}
        </p>
      )}
      <AdminQueryBody query={institutions} empty={institutionAdmin.empty}>
        {(items) => (
          <InstitutionList
            institutions={items}
            selectedId={open ? modal.id : null}
            onAddClass={(item) => openModal('addClass', item)}
            onEdit={(item) => openModal('edit', item)}
            onDelete={(item) => openModal('delete', item)}
          />
        )}
      </AdminQueryBody>
      {open && (
        <AdminModal
          wide={modal.type === 'new' || modal.type === 'edit'}
          label={labels[modal.type]}
          closeLabel={institutionAdmin.close}
          closeOnBackdrop={false}
          onClose={close}
        >
          {modal.type === 'delete' && (
            <InstitutionDeleteConfirm
              institution={target}
              busy={busy}
              fromForm={modal.fromForm}
              onConfirm={remove}
              onCancel={modal.fromForm ? () => openModal('edit', target) : close}
            />
          )}
          {modal.type === 'addClass' && (
            <ClassSessionForm
              key={target.id}
              institutions={institutions.data}
              staffs={staffs}
              editing={null}
              defaultDate={today}
              defaultInstitutionId={target.id}
              busy={busy}
              onCreate={addClasses}
            />
          )}
          {(modal.type === 'new' || modal.type === 'edit') && (
            <InstitutionForm
              key={modal.id ?? 'new'}
              institution={target}
              busy={busy}
              onSave={save}
              onDelete={() => openModal('delete', target, true)}
              onCancel={close}
            />
          )}
        </AdminModal>
      )}
    </AdminSection>
  )
}

export default InstitutionsTab
