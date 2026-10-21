import { useEffect, useRef } from 'react'
import { reviewGate, reviewList, reviewTypes, reviewWriteBox } from '../../data/reviewContent'
import NewsTabs from '../news/NewsTabs'
import ReviewCard from './ReviewCard'
import './ReviewList.css'

// 화면 아래에 닿기 이만큼 전에 다음 후기를 불러온다
const PRELOAD_MARGIN = '400px'

// 수업 후기 목록 (SCR-012): 회원 후기 쓰기 칸, 유형 탭, 카드 목록(무한 스크롤), 비로그인 안내 (FR-012)
// reviews: null이면 불러오는 중. member: undefined면 로그인 상태 확인 중, null이면 비로그인
function ReviewList({
  member,
  type,
  onTypeChange,
  reviews,
  total,
  hasNext,
  loadingMore,
  error,
  onLoadMore,
  onWrite,
  onEdit,
  onDelete,
  onReplySaved,
  onLoginClick,
}) {
  const sentinelRef = useRef(null)

  useEffect(() => {
    const sentinel = sentinelRef.current
    if (!sentinel || !hasNext) return undefined
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) onLoadMore()
      },
      { rootMargin: PRELOAD_MARGIN },
    )
    observer.observe(sentinel)
    return () => observer.disconnect()
  }, [hasNext, onLoadMore])

  const guestLimited = member === null && reviews !== null && total > reviews.length
  const ended = member && reviews !== null && reviews.length > 0 && !hasNext && !error

  return (
    <section className="review-list container" aria-label="후기 목록">
      {member && (
        <div className="review-write-box">
          <div className="review-write-box__head">
            <p className="review-write-box__title">
              <span className="review-write-box__name">{member.name}</span>
              {reviewWriteBox.titleSuffix}
            </p>
            <p className="review-write-box__hint">{reviewWriteBox.hint}</p>
          </div>
          <button type="button" className="review-write-box__field" onClick={onWrite}>
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 20h9" />
              <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z" />
            </svg>
            <span className="review-write-box__placeholder">{reviewWriteBox.placeholder}</span>
            <span className="review-write-box__button">{reviewWriteBox.button}</span>
          </button>
        </div>
      )}

      <div className="review-list__toolbar">
        <NewsTabs active={type} onChange={onTypeChange} categories={reviewTypes} />
        <p className="review-list__sort">{reviewList.sort}</p>
      </div>

      {reviews === null && !error && (
        <p className="review-list__status" role="status">
          {reviewList.loading}
        </p>
      )}
      {reviews?.length === 0 && <p className="review-list__status">{reviewList.empty}</p>}

      {reviews?.length > 0 && (
        <div className="review-list__items">
          {reviews.map((review) => (
            <ReviewCard
              key={review.id}
              review={review}
              mine={member?.id === review.authorId}
              canReply={member?.role === 'ADMIN'}
              onEdit={onEdit}
              onDelete={onDelete}
              onReplySaved={onReplySaved}
            />
          ))}
        </div>
      )}

      {guestLimited && (
        <div className="review-gate">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <rect x="5" y="11" width="14" height="10" rx="2" />
            <path d="M8 11V8a4 4 0 0 1 8 0v3" />
          </svg>
          <p className="review-gate__title">{reviewGate.title}</p>
          <p className="review-gate__description">{reviewGate.description}</p>
          <button type="button" className="review-gate__button" onClick={onLoginClick}>
            {reviewGate.button}
          </button>
        </div>
      )}

      {error && (
        <p className="review-list__status review-list__status--error" role="alert">
          {error}
        </p>
      )}
      {hasNext && <div ref={sentinelRef} className="review-list__sentinel" aria-hidden="true" />}
      {loadingMore && (
        <div className="review-list__loading" role="status">
          <span className="review-list__spinner" aria-hidden="true" />
          {reviewList.loadingMore}
        </div>
      )}
      {ended && <p className="review-list__ended">{reviewList.ended}</p>}
    </section>
  )
}

export default ReviewList
