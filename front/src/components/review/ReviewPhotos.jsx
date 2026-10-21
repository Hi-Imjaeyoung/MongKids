import { useState } from 'react'
import { reviewList } from '../../data/reviewContent'
import './ReviewPhotos.css'

// 큰 사진 아래 작은 사진은 3칸까지. 넘치면 마지막 칸에 "+N"
const THUMB_COUNT = 3

// 후기 사진 영역 (SCR-012). 자동으로 넘어가지 않는다 (NFR-003)
// 데스크톱: 큰 사진 1 + 작은 사진 3, 작은 사진을 누르면 큰 사진이 바뀐다
// 모바일: 손으로 넘기는 사진 줄과 "1 / N"
function ReviewPhotos({ images }) {
  const [selected, setSelected] = useState(0)
  const [slide, setSlide] = useState(0)

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
        <img
          className="review-photos__main"
          src={images[selected].url}
          alt={reviewList.photoLabel(selected + 1)}
          loading="lazy"
        />
        {thumbs.length > 0 && (
          <div className="review-photos__thumbs">
            {thumbs.map((image, position) => {
              const more = position === THUMB_COUNT - 1 && hiddenCount > 0
              return (
                <button
                  key={image.imageKey}
                  type="button"
                  className="review-photos__thumb"
                  aria-label={reviewList.photoLabel(image.index + 1)}
                  onClick={() => setSelected(image.index)}
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
            <img
              key={image.imageKey}
              className="review-photos__slide"
              src={image.url}
              alt={reviewList.photoLabel(index + 1)}
              loading="lazy"
            />
          ))}
        </div>
        {images.length > 1 && (
          <span className="review-photos__counter">
            {Math.min(slide + 1, images.length)} / {images.length}
          </span>
        )}
      </div>
    </div>
  )
}

export default ReviewPhotos
