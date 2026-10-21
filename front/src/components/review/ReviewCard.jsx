import { reviewList, reviewTypes } from '../../data/reviewContent'
import { formatDate } from '../news/postFormat'
import BananaRating from './BananaRating'
import ReviewPhotos from './ReviewPhotos'
import ReviewReply from './ReviewReply'
import './ReviewCard.css'

// 후기 카드 (SCR-012): 사진 영역과 본문을 나눈다. 내 후기에는 "내 후기" 표시와 수정·삭제 (FR-025)
// canReply: 관리자면 카드 안에서 답글을 쓰고 고친다 (FR-017)
function ReviewCard({ review, mine, canReply, onEdit, onDelete, onReplySaved }) {
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
        <ReviewReply review={review} canReply={canReply} onSaved={onReplySaved} />
      </div>
    </article>
  )
}

export default ReviewCard
