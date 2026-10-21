import { reviewList } from '../../data/reviewContent'
import './BananaRating.css'

const LEVELS = [1, 2, 3, 4, 5]

function Banana({ filled }) {
  return (
    <svg className={`banana ${filled ? 'banana--filled' : ''}`} viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 6c1 7 6 12 15 12 1 0 1.6-.5 1-1.2C14 15 9.5 11 8 5.5 7.6 4 4 4 4 6z" strokeLinejoin="round" />
      <path d="M4.5 5.5 3.6 3.8" strokeLinecap="round" />
    </svg>
  )
}

// 바나나 5단계 만족도 (FR-016). onChange가 있으면 입력, 없으면 표시만 한다
function BananaRating({ value, onChange, size = 'md' }) {
  if (!onChange) {
    return (
      <span className={`banana-rating banana-rating--${size}`} role="img" aria-label={reviewList.ratingLabel(value)}>
        {LEVELS.map((level) => (
          <Banana key={level} filled={level <= value} />
        ))}
      </span>
    )
  }

  return (
    <span className="banana-rating banana-rating--input">
      {LEVELS.map((level) => (
        <button
          key={level}
          type="button"
          className="banana-rating__button"
          aria-label={reviewList.ratingLabel(level)}
          aria-pressed={value === level}
          onClick={() => onChange(level)}
        >
          <Banana filled={level <= value} />
        </button>
      ))}
    </span>
  )
}

export default BananaRating
