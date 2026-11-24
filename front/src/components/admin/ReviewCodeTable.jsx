import { reviewCodeAdmin as text, reviewCodeKinds, reviewCodeStatuses } from '../../data/adminContent'
import { formatDate } from '../news/postFormat'
import AdminBadge from './AdminBadge'
import './ReviewCodeTable.css'

const findKind = (id) => reviewCodeKinds.find((kind) => kind.id === id)

function usageLabel(code) {
  if (code.kind === 'BUNDLE') return text.bundleUsage(code.usedCount, code.maxUses)
  return code.usedCount > 0 ? text.singleUsed : text.singleUnused
}

// 발급한 후기 작성 코드 목록 (FR-029). 사용 가능한 코드에만 폐기 버튼을 둔다. busyId인 행은 버튼을 잠근다
function ReviewCodeTable({ codes, busyId, onRevoke }) {
  const { columns } = text

  return (
    <div className="review-code-table" role="region" aria-label={text.listLabel} tabIndex={0}>
      <div className="review-code-table__inner">
        <div className="review-code-table__row review-code-table__row--head">
          <span>{columns.code}</span>
          <span>{columns.kind}</span>
          <span>{columns.usage}</span>
          <span>{columns.expiresOn}</span>
          <span>{columns.status}</span>
          <span />
        </div>
        {codes.map((code) => {
          const kind = findKind(code.kind)
          const status = reviewCodeStatuses[code.status]
          const active = code.status === 'ACTIVE'
          return (
            <div
              key={code.id}
              className={`review-code-table__row ${active ? '' : 'review-code-table__row--ended'}`}
            >
              <span className="review-code-table__code-cell">
                <code
                  className={`review-code-table__code ${code.status === 'REVOKED' ? 'review-code-table__code--revoked' : ''}`}
                >
                  {code.code}
                </code>
                <span className="review-code-table__sub">
                  {text.issuedAt} {formatDate(code.createdAt)}
                  {code.memo && ` · ${code.memo}`}
                </span>
              </span>
              <span>
                <AdminBadge tone={kind.tone} shape="tag">
                  {kind.label}
                </AdminBadge>
              </span>
              <span>{usageLabel(code)}</span>
              <span className="review-code-table__sub">{formatDate(code.expiresOn)}</span>
              <span>
                <AdminBadge tone={status.tone}>{status.label}</AdminBadge>
              </span>
              <span className="review-code-table__action">
                {active && (
                  <button
                    type="button"
                    className="admin-form__button admin-form__button--danger admin-form__button--sm"
                    aria-label={text.revokeLabel(code.code)}
                    disabled={busyId === code.id}
                    onClick={() => onRevoke(code)}
                  >
                    {text.revoke}
                  </button>
                )}
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default ReviewCodeTable
