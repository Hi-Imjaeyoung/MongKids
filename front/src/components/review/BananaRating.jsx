import { reviewList } from '../../data/reviewContent'
import './BananaRating.css'

const LEVELS = [1, 2, 3, 4, 5]

// 임시 바나나 아이콘 (2026-11-05). 새 만족도 아이콘 파일을 받으면 교체한다 (SCR-012)
function Banana({ filled }) {
  return (
    <svg className={`banana ${filled ? 'banana--filled' : ''}`} viewBox="0 0 24 24" aria-hidden="true">
      <path
        className="banana__body"
        d="M5.2 5C4.6 11.6 9 18.6 18.6 19.7c1.6.2 2.2-1.2.8-1.8C13 15.4 9.4 11 8.1 5c-.4-1.6-2.8-1.6-2.9 0z"
        strokeLinejoin="round"
      />
      <path className="banana__ridge" d="M6.8 7.4c.9 4.6 4.2 8.6 9.6 10.6" strokeLinecap="round" />
      <path className="banana__stem" d="M5.4 4.6 5 2.6c0-.4.3-.7.7-.7h1c.4 0 .7.3.7.7l.4 2" strokeLinejoin="round" />
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
