import { reviewList, reviewTypes } from '../../data/reviewContent'
import { formatDate } from '../news/postFormat'
import BananaRating from './BananaRating'
import ReviewPhotos from './ReviewPhotos'
import './ReviewCard.css'

// 후기 카드 (SCR-012): 사진 영역과 본문을 나눈다. 내 후기에는 "내 후기" 표시와 수정·삭제 (FR-025)
function ReviewCard({ review, mine, onEdit, onDelete }) {
  const type = reviewTypes.find((item) => item.id === review.type)

  return (
    <article className="review-card-item">
      {review.images.length > 0 && <ReviewPhotos images={review.images} />}
      <div className="review-card-item__body">
        <div className="review-card-item__meta">
          <span className={`review-card-item__type review-card-item__type--${type?.tone ?? 'blue'}`}>
            {type?.label ?? review.type}
          </span>
          <BananaRating value={review.satisfaction} />
        </div>
        <p className="review-card-item__content">{review.content}</p>
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
        {review.reply && (
          <div className="review-card-item__reply">
            <p className="review-card-item__reply-title">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M9 14 4 9l5-5" />
                <path d="M4 9h10a6 6 0 0 1 6 6v5" />
              </svg>
              {reviewList.replyTitle}
            </p>
            <p className="review-card-item__reply-content">{review.reply.content}</p>
          </div>
        )}
      </div>
    </article>
  )
}

export default ReviewCard
