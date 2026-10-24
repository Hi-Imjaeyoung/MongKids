import { useState } from 'react'
import { changeConsultationStatus, getConsultation, getConsultations } from '../../api/admin'
import AdminListBody from '../../components/admin/AdminListBody'
import AdminNotice from '../../components/admin/AdminNotice'
import AdminSection from '../../components/admin/AdminSection'
import AdminSplit from '../../components/admin/AdminSplit'
import ConsultationDetail from '../../components/admin/ConsultationDetail'
import ConsultationTable from '../../components/admin/ConsultationTable'
import FilterChips from '../../components/admin/FilterChips'
import { toApiFilter } from '../../components/admin/adminFormat'
import Pagination from '../../components/lab/Pagination'
import { consultationAdmin, consultationStatuses, consultationTypes } from '../../data/adminContent'
import useAdminList from '../useAdminList'

const STATUS_FILTERS = [{ id: 'ALL', label: consultationAdmin.allStatuses }, ...consultationStatuses]

// 상담 신청 (FR-004)과 처리 상태 (FR-026)
function ConsultationsTab() {
  const [type, setType] = useState('ALL')
  const [status, setStatus] = useState('ALL')
  const [page, setPage] = useState(1)
  const list = useAdminList(getConsultations, {
    type: toApiFilter(type),
    status: toApiFilter(status),
    page: page - 1,
  })

  const [selectedId, setSelectedId] = useState(null)
  const [detail, setDetail] = useState(null)
  const [saving, setSaving] = useState(false)

  const changeType = (value) => {
    setType(value)
    setPage(1)
  }
  const changeStatusFilter = (value) => {
    setStatus(value)
    setPage(1)
  }

  const select = async (id) => {
    setSelectedId(id)
    try {
      setDetail(await getConsultation(id))
    } catch (error) {
      window.alert(error.message)
    }
  }

  const changeStatus = async (nextStatus) => {
    setSaving(true)
    try {
      const updated = await changeConsultationStatus(detail.id, nextStatus)
      setDetail(updated)
      list.updateItem(
        (item) => item.id === updated.id,
        (item) => ({ ...item, status: updated.status }),
      )
    } catch (error) {
      window.alert(error.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <AdminSection
      title={consultationAdmin.title}
      description={consultationAdmin.description}
      toolbar={
        <>
          <FilterChips label={consultationAdmin.columns.type} options={consultationTypes} value={type} onChange={changeType} />
          <FilterChips
            label={consultationAdmin.statusLabel}
            options={STATUS_FILTERS}
            value={status}
            onChange={changeStatusFilter}
          />
        </>
      }
    >
      <AdminSplit
        main={
          <>
            <AdminListBody list={list} empty={consultationAdmin.empty}>
              {(result) => (
                <ConsultationTable consultations={result.content} selectedId={selectedId} onSelect={select} />
              )}
            </AdminListBody>
            {list.page && <Pagination page={page} totalPages={list.page.totalPages} onChange={setPage} />}
          </>
        }
        side={
          detail ? (
            <ConsultationDetail detail={detail} saving={saving} onStatusChange={changeStatus} />
          ) : (
            <AdminNotice inline message={consultationAdmin.selectHint} />
          )
        }
      />
    </AdminSection>
  )
}

export default ConsultationsTab
