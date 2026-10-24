import { useEffect, useRef } from 'react'
import {
  consultationAdmin,
  consultationStatuses,
  consultationTypes,
  institutionFields,
  personalFields,
} from '../../data/adminContent'
import AdminBadge from './AdminBadge'
import { formatDateTime, formatPhone } from './adminFormat'
import './ConsultationDetail.css'

// 유형에 맞는 항목만 골라 보여 준다. 비어 있는 선택 항목은 뺀다
function toRows(detail) {
  const [fields, values] =
    detail.type === 'INSTITUTION' ? [institutionFields, detail.institution] : [personalFields, detail.personal]
  return fields
    .filter((field) => values[field.key] !== null && values[field.key] !== undefined && values[field.key] !== '')
    .map((field) => ({
      label: field.label,
      value: field.format ? field.format(values[field.key]) : values[field.key],
    }))
}

// 상담 신청 상세 (FR-004)와 처리 상태 버튼 (FR-026). saving이면 상태 버튼을 잠근다
function ConsultationDetail({ detail, saving, onStatusChange }) {
  const sectionRef = useRef(null)

  // 모바일에서는 상세가 목록 아래에 있어 고른 뒤 보이도록 옮겨 준다
  useEffect(() => {
    sectionRef.current?.scrollIntoView?.({ block: 'nearest', behavior: 'smooth' })
  }, [detail.id])

  const type = consultationTypes.find((item) => item.id === detail.type)
  const healthAgreedAt = detail.personal?.healthAgreedAt

  return (
    <section ref={sectionRef} className="consultation-detail" aria-label={consultationAdmin.detailLabel}>
      <div className="consultation-detail__head">
        <div>
          <AdminBadge tone={type.tone} shape="tag">
            {type.label}
            {consultationAdmin.typeSuffix}
          </AdminBadge>
          <h2 className="consultation-detail__title">
            {detail.institution?.institutionName ?? detail.personal?.guardianName}
          </h2>
          <p className="consultation-detail__meta">
            {consultationAdmin.receivedAt} {formatDateTime(detail.createdAt)}
          </p>
        </div>
        <a className="consultation-detail__call" href={`tel:${detail.phone}`}>
          <PhoneIcon />
          {consultationAdmin.call}
        </a>
      </div>

      <div className="consultation-detail__status" role="group" aria-label={consultationAdmin.statusLabel}>
        {consultationStatuses.map((status) => (
          <button
            key={status.id}
            type="button"
            className={`consultation-detail__status-button consultation-detail__status-button--${status.tone} ${
              detail.status === status.id ? 'consultation-detail__status-button--active' : ''
            }`}
            aria-pressed={detail.status === status.id}
            disabled={saving}
            onClick={() => detail.status !== status.id && onStatusChange(status.id)}
          >
            {status.label}
          </button>
        ))}
      </div>

      <dl className="consultation-detail__fields">
        <div className="consultation-detail__field">
          <dt>{consultationAdmin.columns.phone}</dt>
          <dd>{formatPhone(detail.phone)}</dd>
        </div>
        {toRows(detail).map((row) => (
          <div key={row.label} className="consultation-detail__field">
            <dt>{row.label}</dt>
            <dd>{row.value}</dd>
          </div>
        ))}
      </dl>

      <ul className="consultation-detail__consents">
        <li>
          {consultationAdmin.privacyAgreedAt} {formatDateTime(detail.privacyAgreedAt)}
        </li>
        {healthAgreedAt && (
          <li>
            {consultationAdmin.healthAgreedAt} {formatDateTime(healthAgreedAt)}
          </li>
        )}
        {detail.completedAt && (
          <li>
            {consultationAdmin.completedAt} {formatDateTime(detail.completedAt)}
          </li>
        )}
      </ul>
    </section>
  )
}

function PhoneIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1 1 .4 1.9.7 2.8a2 2 0 0 1-.5 2.1L8 9.9a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.4c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2z" />
    </svg>
  )
}

export default ConsultationDetail
