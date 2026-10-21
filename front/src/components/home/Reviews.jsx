import { useState } from 'react'
import { reviews, reviewsCopy } from '../../data/homeContent'
import Button from '../common/Button'
import './Reviews.css'

const VISIBLE_COUNT = 3

function Reviews() {
  const [startIndex, setStartIndex] = useState(0)
  const hasReviews = reviews.length > 0

  // 마지막 후기 다음에는 처음 후기로 이어지도록 순환한다.
  const visibleReviews = Array.from(
    { length: Math.min(VISIBLE_COUNT, reviews.length) },
    (_, offset) => reviews[(startIndex + offset) % reviews.length],
  )

  const showNext = () => setStartIndex((index) => (index + 1) % reviews.length)

  return (
    <section className="reviews container" aria-labelledby="reviews-title">
      <div className={`reviews__panel ${hasReviews ? '' : 'reviews__panel--empty'}`}>
        <h2 id="reviews-title" className="reviews__title">
          {reviewsCopy.titleLines.map((line) => (
            <span key={line} className="reviews__title-line">{line} </span>
          ))}
          <span className="reviews__bubble" aria-hidden="true">💬</span>
        </h2>

        {hasReviews ? (
          <>
            <ul className="reviews__list">
              {visibleReviews.map((review) => (
                <li key={review.id} className="review-card">
                  <span className="review-card__stars" aria-label={`별점 ${review.rating}점`}>
                    {'★'.repeat(review.rating)}
                  </span>
                  <p className="review-card__content">{review.content}</p>
                  <p className="review-card__author">- {review.author} -</p>
                </li>
              ))}
            </ul>

            <button type="button" className="reviews__next" aria-label="다음 후기" onClick={showNext}>
              ›
            </button>
          </>
        ) : (
          <div className="reviews__empty">
            <p className="reviews__empty-title">{reviewsCopy.empty}</p>
            <p className="reviews__empty-sub">{reviewsCopy.emptySub}</p>
            <Button variant="ghost" to={reviewsCopy.more.to} className="reviews__more">
              {reviewsCopy.more.label}
            </Button>
          </div>
        )}
      </div>
    </section>
  )
}

export default Reviews
