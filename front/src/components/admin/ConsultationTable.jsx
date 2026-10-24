import { consultationAdmin, consultationStatuses, consultationTypes } from '../../data/adminContent'
import AdminBadge from './AdminBadge'
import { formatPhone, formatShortDateTime } from './adminFormat'
import './ConsultationTable.css'

const findType = (id) => consultationTypes.find((type) => type.id === id)
const findStatus = (id) => consultationStatuses.find((status) => status.id === id)

// 상담 신청 목록 (FR-004). 신청자 이름을 누르면 onSelect(id)
function ConsultationTable({ consultations, selectedId, onSelect }) {
  const { columns } = consultationAdmin

  return (
    <div className="consultation-table" role="region" aria-label={consultationAdmin.listLabel} tabIndex={0}>
      <div className="consultation-table__inner">
        <div className="consultation-table__row consultation-table__row--head">
          <span>{columns.createdAt}</span>
          <span>{columns.type}</span>
          <span>{columns.title}</span>
          <span>{columns.phone}</span>
          <span>{columns.status}</span>
        </div>
        {consultations.map((consultation) => {
          const type = findType(consultation.type)
          const status = findStatus(consultation.status)
          const selected = consultation.id === selectedId
          return (
            <div
              key={consultation.id}
              className={`consultation-table__row ${selected ? 'consultation-table__row--selected' : ''}`}
            >
              <span className="consultation-table__sub">{formatShortDateTime(consultation.createdAt)}</span>
              <span>
                <AdminBadge tone={type.tone} shape="tag">
                  {type.label}
                </AdminBadge>
              </span>
              <button
                type="button"
                className="consultation-table__title"
                aria-pressed={selected}
                onClick={() => onSelect(consultation.id)}
              >
                {consultation.title}
              </button>
              <span className="consultation-table__sub">{formatPhone(consultation.phone)}</span>
              <span>
                <AdminBadge tone={status.tone}>{status.label}</AdminBadge>
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default ConsultationTable
