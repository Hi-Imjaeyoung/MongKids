import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { getReviews } from '../../api/reviews'
import { reviewsCopy } from '../../data/homeContent'
import { reviewTypes } from '../../data/reviewContent'
import Button from '../common/Button'
import { formatDate } from '../news/postFormat'
import BananaRating from '../review/BananaRating'
import './Reviews.css'

// 홈에 싣는 최신 후기 수. 비로그인은 서버가 최신 4개만 준다 (FR-012)
const HOME_REVIEW_COUNT = 10

// 홈 후기 섹션 (SCR-001). 최신 후기를 2개씩 보여 주고 화살표·손으로 옆으로 넘긴다.
// 자동으로 넘어가지 않는다 (NFR-003)
function Reviews() {
  const [reviews, setReviews] = useState(null)
  const [edges, setEdges] = useState({ start: true, end: true })
  const trackRef = useRef(null)

  useEffect(() => {
    let ignore = false
    getReviews({ size: HOME_REVIEW_COUNT })
      .then((page) => {
        if (!ignore) setReviews(page.content)
      })
      .catch(() => {
        if (!ignore) setReviews([])
      })
    return () => {
      ignore = true
    }
  }, [])

  // 맨 앞·맨 끝이면 그쪽 화살표를 끈다
  const updateEdges = () => {
    const track = trackRef.current
    if (!track) return
    const { scrollLeft, scrollWidth, clientWidth } = track
    setEdges({ start: scrollLeft <= 1, end: scrollLeft + clientWidth >= scrollWidth - 1 })
  }

  useEffect(() => {
    updateEdges()
    window.addEventListener('resize', updateEdges)
    return () => window.removeEventListener('resize', updateEdges)
  }, [reviews])

  // 한 번에 보이는 카드 수(데스크톱 2개, 모바일 1개)만큼 넘긴다
  const slide = (direction) => {
    const track = trackRef.current
    track?.scrollBy({ left: direction * track.clientWidth, behavior: 'smooth' })
  }

  const hasReviews = reviews?.length > 0

  return (
    <section className="reviews container" aria-labelledby="reviews-title">
      <div className={`reviews__panel ${reviews && !hasReviews ? 'reviews__panel--empty' : ''}`}>
        <div className="reviews__intro">
          <h2 id="reviews-title" className="reviews__title">
            {reviewsCopy.titleLines.map((line) => (
              <span key={line} className="reviews__title-line">{line} </span>
            ))}
            <span className="reviews__bubble" aria-hidden="true">💬</span>
          </h2>
          {hasReviews && (
            <Link to={reviewsCopy.more.to} className="reviews__all">
              {reviewsCopy.more.shortLabel} ›
            </Link>
          )}
        </div>

        {reviews === null && <p className="reviews__loading">{reviewsCopy.loading}</p>}

        {hasReviews && (
          <>
            <ul className="reviews__track" ref={trackRef} onScroll={updateEdges}>
              {reviews.map((review) => {
                const type = reviewTypes.find((item) => item.id === review.type)
                const thumbnail = review.images[0]?.url
                return (
                  <li key={review.id} className="reviews__slide">
                    <Link to={reviewsCopy.more.to} className="review-card">
                      <div className="review-card__meta">
                        <span className={`review-card__type review-card__type--${type?.tone ?? 'blue'}`}>
                          {type?.label}
                        </span>
                        <BananaRating value={review.satisfaction} size="sm" />
                      </div>
                      <div className="review-card__main">
                        <p className="review-card__content">{review.content}</p>
                        {thumbnail && <img className="review-card__photo" src={thumbnail} alt="" loading="lazy" />}
                      </div>
                      <p className="review-card__author">
                        {review.authorName} · {formatDate(review.createdAt)}
                      </p>
                    </Link>
                  </li>
                )
              })}
            </ul>

            <div className="reviews__controls">
              <button
                type="button"
                className="reviews__arrow"
                aria-label={reviewsCopy.prev}
                disabled={edges.start}
                onClick={() => slide(-1)}
              >
                ‹
              </button>
              <button
                type="button"
                className="reviews__arrow"
                aria-label={reviewsCopy.next}
                disabled={edges.end}
                onClick={() => slide(1)}
              >
                ›
              </button>
            </div>
          </>
        )}

        {reviews && !hasReviews && (
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
