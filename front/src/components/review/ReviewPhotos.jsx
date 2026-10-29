import { useState } from 'react'
import { reviewList } from '../../data/reviewContent'
import ReviewPhotoViewer from './ReviewPhotoViewer'
import './ReviewPhotos.css'

// 큰 사진 아래 작은 사진은 3칸까지. 넘치면 마지막 칸에 "+N"
const THUMB_COUNT = 3

// 후기 사진 영역 (SCR-012). 자동으로 넘어가지 않는다 (NFR-003)
// 데스크톱: 큰 사진 1 + 작은 사진 3, 작은 사진을 누르면 큰 사진이 바뀐다
// 모바일: 손으로 넘기는 사진 줄과 "1 / N"
// 큰 사진·모바일 사진·"+N" 칸을 누르면 크게 보기(ReviewPhotoViewer)를 연다
function ReviewPhotos({ images }) {
  const [selected, setSelected] = useState(0)
  const [slide, setSlide] = useState(0)
  // 크게 보기로 연 사진 순번. null이면 닫힘
  const [viewing, setViewing] = useState(null)

  const others = images.map((image, index) => ({ ...image, index })).filter(({ index }) => index !== selected)
  const thumbs = others.slice(0, THUMB_COUNT)
  const hiddenCount = others.length - THUMB_COUNT

  const handleScroll = (event) => {
    const { scrollLeft, clientWidth } = event.currentTarget
    setSlide(Math.round(scrollLeft / clientWidth))
  }

  return (
    <div className="review-photos">
      <div className="review-photos__desktop">
        <button
          type="button"
          className="review-photos__open"
          aria-label={reviewList.photoOpen(selected + 1)}
          onClick={() => setViewing(selected)}
        >
          <img className="review-photos__main" src={images[selected].url} alt="" loading="lazy" />
        </button>
        {thumbs.length > 0 && (
          <div className="review-photos__thumbs">
            {thumbs.map((image, position) => {
              const more = position === THUMB_COUNT - 1 && hiddenCount > 0
              return (
                <button
                  key={image.imageKey}
                  type="button"
                  className="review-photos__thumb"
                  aria-label={more ? reviewList.photoOpen(image.index + 1) : reviewList.photoLabel(image.index + 1)}
                  onClick={() => (more ? setViewing(image.index) : setSelected(image.index))}
                >
                  <img src={image.url} alt="" loading="lazy" />
                  {more && <span className="review-photos__more">+{hiddenCount + 1}</span>}
                </button>
              )
            })}
          </div>
        )}
      </div>

      <div className="review-photos__mobile">
        <div className="review-photos__track" onScroll={handleScroll}>
          {images.map((image, index) => (
            <button
              key={image.imageKey}
              type="button"
              className="review-photos__slide"
              aria-label={reviewList.photoOpen(index + 1)}
              onClick={() => setViewing(index)}
            >
              <img src={image.url} alt="" loading="lazy" />
            </button>
          ))}
        </div>
        {images.length > 1 && (
          <span className="review-photos__counter">
            {Math.min(slide + 1, images.length)} / {images.length}
          </span>
        )}
      </div>

      {viewing !== null && (
        <ReviewPhotoViewer images={images} startIndex={viewing} onClose={() => setViewing(null)} />
      )}
    </div>
  )
}

export default ReviewPhotos
