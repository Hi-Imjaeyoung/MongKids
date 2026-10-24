import { Link } from 'react-router-dom'
import { reviewAdmin } from '../../data/adminContent'
import { reviewTypes } from '../../data/reviewContent'
import BananaRating from '../review/BananaRating'
import { formatDate } from '../news/postFormat'
import AdminBadge from './AdminBadge'
import './AdminReviewCard.css'

// 관리자 후기 카드 (FR-021). item: { review, hidden }. busy면 버튼을 잠근다
function AdminReviewCard({ item, busy, onToggleHidden }) {
  const { review, hidden } = item
  const type = reviewTypes.find((reviewType) => reviewType.id === review.type)

  return (
    <article className={`admin-review-card ${hidden ? 'admin-review-card--hidden' : ''}`}>
      <div className="admin-review-card__body">
        <div className="admin-review-card__meta">
          <AdminBadge tone={type.tone} shape="tag">
            {type.label}
          </AdminBadge>
          <span className="admin-review-card__author">{review.authorName}</span>
          <span>{formatDate(review.createdAt)}</span>
          <BananaRating value={review.satisfaction} size="sm" />
          {hidden && <AdminBadge tone="dark">{reviewAdmin.hiddenBadge}</AdminBadge>}
        </div>
        <p className="admin-review-card__content">{review.content}</p>
        <div className="admin-review-card__extra">
          {review.images.length > 0 && <span>{reviewAdmin.photos(review.images.length)}</span>}
          <span>{review.reply ? reviewAdmin.replied : reviewAdmin.noReply}</span>
          <Link to="/reviews">{reviewAdmin.viewOnPage}</Link>
        </div>
      </div>
      <button
        type="button"
        className={`admin-review-card__action ${hidden ? 'admin-review-card__action--unhide' : ''}`}
        disabled={busy}
        onClick={() => onToggleHidden(item)}
      >
        {hidden ? reviewAdmin.unhide : reviewAdmin.hide}
      </button>
    </article>
  )
}

export default AdminReviewCard
