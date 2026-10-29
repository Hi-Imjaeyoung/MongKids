import { useEffect, useRef, useState } from 'react'
import { photoViewer, reviewList } from '../../data/reviewContent'
import ChevronIcon from '../common/ChevronIcon'
import './ReviewPhotoViewer.css'

// 손으로 밀어 넘길 때 사진을 바꾸는 최소 거리(px)
const SWIPE_DISTANCE = 50

// 후기 사진 크게 보기 (SCR-012). 화살표 버튼·키보드(←, →)·손으로 밀어 넘기고 Esc·배경·닫기 버튼으로 닫는다.
// 처음과 끝에서 이어 돌지 않고, 자동으로 넘어가지 않는다 (NFR-003)
function ReviewPhotoViewer({ images, startIndex, onClose }) {
  const [index, setIndex] = useState(startIndex)
  const closeRef = useRef(null)
  const touchStartRef = useRef(null)
  const last = images.length - 1

  // 열 때 닫기 버튼으로 초점을 옮기고, 닫으면 사진을 열었던 버튼으로 돌려준다
  useEffect(() => {
    const opener = document.activeElement
    closeRef.current?.focus()
    return () => opener?.focus?.()
  }, [])

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') onClose()
      if (event.key === 'ArrowLeft') setIndex((current) => Math.max(current - 1, 0))
      if (event.key === 'ArrowRight') setIndex((current) => Math.min(current + 1, last))
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [onClose, last])

  const handleTouchEnd = (event) => {
    const startX = touchStartRef.current
    touchStartRef.current = null
    if (startX === null) return
    const distance = event.changedTouches[0].clientX - startX
    if (distance > SWIPE_DISTANCE) setIndex((current) => Math.max(current - 1, 0))
    if (distance < -SWIPE_DISTANCE) setIndex((current) => Math.min(current + 1, last))
  }

  return (
    <div
      className="photo-viewer"
      role="dialog"
      aria-modal="true"
      aria-label={photoViewer.label}
      onClick={(event) => event.target === event.currentTarget && onClose()}
      onTouchStart={(event) => {
        touchStartRef.current = event.touches[0].clientX
      }}
      onTouchEnd={handleTouchEnd}
    >
      <button type="button" className="photo-viewer__close" aria-label={photoViewer.close} ref={closeRef} onClick={onClose}>
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M6 6l12 12M18 6 6 18" />
        </svg>
      </button>

      <img className="photo-viewer__image" src={images[index].url} alt={reviewList.photoLabel(index + 1)} />

      {images.length > 1 && (
        <>
          <button
            type="button"
            className="photo-viewer__nav photo-viewer__nav--prev"
            aria-label={photoViewer.prev}
            disabled={index === 0}
            onClick={() => setIndex(index - 1)}
          >
            <ChevronIcon size={24} />
          </button>
          <button
            type="button"
            className="photo-viewer__nav photo-viewer__nav--next"
            aria-label={photoViewer.next}
            disabled={index === last}
            onClick={() => setIndex(index + 1)}
          >
            <ChevronIcon size={24} />
          </button>
          <span className="photo-viewer__counter" aria-live="polite">
            {index + 1} / {images.length}
          </span>
        </>
      )}
    </div>
  )
}

export default ReviewPhotoViewer
