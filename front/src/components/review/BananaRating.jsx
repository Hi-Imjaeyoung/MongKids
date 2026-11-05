import bananaIcon from '../../assets/rating-banana.png'
import { reviewList } from '../../data/reviewContent'
import './BananaRating.css'

const LEVELS = [1, 2, 3, 4, 5]

// 만족도 바나나 아이콘 (SCR-012, 2026-11-05 받은 그림). 채우지 않은 칸은 같은 그림을 흐린 회색으로 보여 준다
function Banana({ filled }) {
  return <img className={`banana ${filled ? 'banana--filled' : ''}`} src={bananaIcon} alt="" draggable="false" />
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
