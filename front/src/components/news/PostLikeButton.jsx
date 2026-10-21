import { newsLike } from '../../data/newsContent'
import './PostLikeButton.css'

// 공감 버튼 (FR-011). 비로그인이면 누를 때 로그인 창을 연다
function PostLikeButton({ member, liked, likeCount, disabled, onToggle, onLoginClick }) {
  return (
    <div className="post-like">
      <button
        type="button"
        className={`post-like__button${liked ? ' post-like__button--on' : ''}`}
        aria-pressed={liked}
        title={member ? undefined : newsLike.loginRequired}
        onClick={member ? onToggle : onLoginClick}
        disabled={disabled}
      >
        <span className="post-like__icon" aria-hidden="true">
          {liked ? '♥' : '♡'}
        </span>
        {newsLike.label} <span className="post-like__count">{likeCount}</span>
      </button>
    </div>
  )
}

export default PostLikeButton
