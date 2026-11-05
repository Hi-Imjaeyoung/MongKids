import { useRef, useState } from 'react'
import ChevronIcon from '../common/ChevronIcon'
import ReviewPhotoViewer from '../review/ReviewPhotoViewer'
import { newsDetail } from '../../data/newsContent'
import './PostCardNews.css'

// 사진만 게시물의 카드뉴스 (SCR-006). 1:1 칸에 꽉 채워 보여 주고 손으로·화살표·←→ 키로 넘긴다.
// 처음과 끝에서 이어 돌지 않고, 자동으로 넘어가지 않는다 (NFR-003). 사진을 누르면 크게 보기
// images: [{ imageKey, url }]
function PostCardNews({ images }) {
  const trackRef = useRef(null)
  const [index, setIndex] = useState(0)
  const [viewing, setViewing] = useState(null)
  const last = images.length - 1
  const single = images.length === 1

  const goTo = (next) => {
    const track = trackRef.current
    const target = Math.max(0, Math.min(last, next))
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    track.scrollTo({ left: target * track.clientWidth, behavior: reduce ? 'auto' : 'smooth' })
    setIndex(target)
  }

  const handleScroll = (event) => {
    const { scrollLeft, clientWidth } = event.currentTarget
    setIndex(Math.min(last, Math.round(scrollLeft / clientWidth)))
  }

  const handleKeyDown = (event) => {
    if (event.key === 'ArrowLeft') {
      event.preventDefault()
      goTo(index - 1)
    }
    if (event.key === 'ArrowRight') {
      event.preventDefault()
      goTo(index + 1)
    }
  }

  // 크게 보기에서 넘긴 사진은 알 수 없으므로 닫으면 연 사진 그대로 둔다
  return (
    <section
      className={`post-card-news ${single ? 'post-card-news--single' : ''}`}
      aria-roledescription="carousel"
      aria-label={newsDetail.cardNews}
    >
      <div className="post-card-news__viewport">
        <div
          ref={trackRef}
          className="post-card-news__track"
          tabIndex={single ? undefined : 0}
          onScroll={handleScroll}
          onKeyDown={handleKeyDown}
        >
          {images.map((image, position) => (
            <button
              key={image.imageKey ?? position}
              type="button"
              className="post-card-news__slide"
              aria-label={newsDetail.photoOpen(position + 1, images.length)}
              onClick={() => setViewing(position)}
            >
              <img src={image.url} alt="" loading={position === 0 ? undefined : 'lazy'} draggable="false" />
            </button>
          ))}
        </div>
        {!single && (
          <>
            <span className="post-card-news__count" aria-live="polite">
              {index + 1} / {images.length}
            </span>
            <button
              type="button"
              className="post-card-news__arrow post-card-news__arrow--prev"
              aria-label={newsDetail.cardPrev}
              onClick={() => goTo(index - 1)}
              disabled={index === 0}
            >
              <ChevronIcon size={18} />
            </button>
            <button
              type="button"
              className="post-card-news__arrow post-card-news__arrow--next"
              aria-label={newsDetail.cardNext}
              onClick={() => goTo(index + 1)}
              disabled={index === last}
            >
              <ChevronIcon size={18} />
            </button>
          </>
        )}
      </div>

      {!single && (
        <div className="post-card-news__dots">
          {images.map((image, position) => (
            <button
              key={image.imageKey ?? position}
              type="button"
              className={`post-card-news__dot ${position === index ? 'post-card-news__dot--active' : ''}`}
              aria-label={newsDetail.cardGoTo(position + 1)}
              aria-current={position === index ? 'true' : undefined}
              onClick={() => goTo(position)}
            />
          ))}
        </div>
      )}

      {viewing !== null && (
        <ReviewPhotoViewer
          images={images}
          startIndex={viewing}
          photoLabel={(position) => newsDetail.photoAlt(position, images.length)}
          onClose={() => setViewing(null)}
        />
      )}
    </section>
  )
}

export default PostCardNews
