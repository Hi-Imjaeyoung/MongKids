import { useState } from 'react'
import { reviewList, reviewTypes } from '../../data/reviewContent'
import { formatDate } from '../news/postFormat'
import BananaRating from './BananaRating'
import ReviewContent from './ReviewContent'
import ReviewPhotos from './ReviewPhotos'
import ReviewReply from './ReviewReply'
import './ReviewCard.css'

// 후기 카드 (SCR-012): 위쪽은 사진 + 본문 한 묶음, 답글은 그 아래 카드 전체 폭에 둔다.
// 사진 있는 후기는 접혀 있는 동안 본문 칸을 사진 칸 높이에 맞추고 넘치는 본문은 "더보기"로 펼친다.
// 내 후기에는 "내 후기" 표시와 수정·삭제 (FR-025). canReply: 관리자면 답글을 쓰고 고친다 (FR-017)
function ReviewCard({ review, mine, canReply, onEdit, onDelete, onReplySaved }) {
  const [expanded, setExpanded] = useState(false)
  const type = reviewTypes.find((item) => item.id === review.type)
  const hasPhotos = review.images.length > 0

  return (
    <article className={`review-card-item ${hasPhotos ? 'review-card-item--photo' : ''}`}>
      <div className="review-card-item__main">
        {hasPhotos && <ReviewPhotos images={review.images} />}
        <div className={`review-card-item__body ${hasPhotos && !expanded ? 'review-card-item__body--fit' : ''}`}>
          <div className="review-card-item__meta">
            <span className={`review-card-item__type review-card-item__type--${type?.tone ?? 'blue'}`}>
              {type?.label ?? review.type}
            </span>
            <BananaRating value={review.satisfaction} />
          </div>
          <ReviewContent
            id={`review-content-${review.id}`}
            content={review.content}
            expanded={expanded}
            onToggle={() => setExpanded((open) => !open)}
          />
          <div className="review-card-item__footer">
            <p className="review-card-item__author">
              {review.authorName} · {formatDate(review.createdAt)}
            </p>
            {mine && (
              <div className="review-card-item__actions">
                <span className="review-card-item__mine">{reviewList.mine}</span>
                <button type="button" className="review-card-item__edit" onClick={() => onEdit(review)}>
                  {reviewList.edit}
                </button>
                <button type="button" className="review-card-item__delete" onClick={() => onDelete(review)}>
                  {reviewList.delete}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
      <ReviewReply review={review} canReply={canReply} onSaved={onReplySaved} />
    </article>
  )
}

export default ReviewCard
