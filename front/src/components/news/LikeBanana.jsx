import bananaIcon from '../../assets/rating-banana.png'
import './LikeBanana.css'

// 공감 아이콘 (SCR-006·024, 2026-11-12 하트에서 변경). 후기 만족도(SCR-012)와 같은 바나나 그림을 쓴다.
// filled가 false면 공감하지 않은 상태로 흐린 회색
function LikeBanana({ filled = true, size = 'md' }) {
  return (
    <img
      className={`like-banana like-banana--${size}${filled ? '' : ' like-banana--off'}`}
      src={bananaIcon}
      alt=""
      aria-hidden="true"
      draggable="false"
    />
  )
}

export default LikeBanana
