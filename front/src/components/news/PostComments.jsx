import { useState } from 'react'
import Button from '../common/Button'
import { formatDate } from './postFormat'
import { canDeleteComment, isMyComment } from './postPermission'
import { newsComments } from '../../data/newsContent'
import './PostComments.css'

// 게시물 댓글 목록·입력 (FR-010). comments가 null이면 불러오는 중.
// member가 없으면 입력창 대신 로그인 안내를 보여 준다. onAdd·onRemove는 실패하면 예외를 던진다
function PostComments({
  member,
  comments,
  total,
  hasNext,
  loadingMore,
  error,
  onLoadMore,
  onAdd,
  onRemove,
  onLoginClick,
}) {
  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState(null)
  const [removingId, setRemovingId] = useState(null)

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!content.trim()) {
      setFormError(newsComments.errors.required)
      return
    }
    setSubmitting(true)
    setFormError(null)
    try {
      await onAdd(content)
      setContent('')
    } catch (submitError) {
      setFormError(submitError.message || newsComments.errors.submit)
    } finally {
      setSubmitting(false)
    }
  }

  const handleRemove = async (commentId) => {
    if (!window.confirm(newsComments.deleteConfirm)) return
    setRemovingId(commentId)
    try {
      await onRemove(commentId)
    } catch (removeError) {
      window.alert(removeError.message)
    } finally {
      setRemovingId(null)
    }
  }

  return (
    <section className="post-comments" aria-labelledby="post-comments-title">
      <h2 id="post-comments-title" className="post-comments__title">
        {newsComments.title} <span className="post-comments__total">{total}</span>
      </h2>

      {member ? (
        <form className="post-comments__form" onSubmit={handleSubmit} noValidate>
          <textarea
            className="post-comments__input"
            aria-label={newsComments.title}
            rows={3}
            maxLength={newsComments.maxLength}
            placeholder={newsComments.placeholder}
            value={content}
            onChange={(event) => setContent(event.target.value)}
            aria-invalid={Boolean(formError)}
            aria-describedby={formError ? 'post-comment-error' : undefined}
          />
          <div className="post-comments__form-bottom">
            <span className="post-comments__count">
              {content.length.toLocaleString()} / {newsComments.maxLength.toLocaleString()}
            </span>
            <Button type="submit" variant="blue" disabled={submitting}>
              {submitting ? newsComments.submitting : newsComments.submit}
            </Button>
          </div>
          {formError && (
            <p id="post-comment-error" className="post-comments__error" role="alert">
              {formError}
            </p>
          )}
        </form>
      ) : (
        <div className="post-comments__login">
          <p>{newsComments.loginRequired}</p>
          <Button variant="blue" onClick={onLoginClick}>
            {newsComments.login}
          </Button>
        </div>
      )}

      {error && (
        <p className="post-comments__message post-comments__message--error" role="alert">
          {error}
        </p>
      )}
      {!error && !comments && <p className="post-comments__message">{newsComments.loading}</p>}
      {comments?.length === 0 && <p className="post-comments__message">{newsComments.empty}</p>}

      {comments?.length > 0 && (
        <ul className="post-comments__list">
          {comments.map((comment) => {
            const mine = isMyComment(member, comment)
            return (
              <li key={comment.id} className={`post-comments__item${mine ? ' post-comments__item--mine' : ''}`}>
                <div className="post-comments__meta">
                  <strong className="post-comments__author">{comment.authorName}</strong>
                  {mine && <span className="post-comments__badge">{newsComments.mineBadge}</span>}
                  <span className="post-comments__date">{formatDate(comment.createdAt)}</span>
                  {canDeleteComment(member, comment) && (
                    <button
                      type="button"
                      className="post-comments__delete"
                      onClick={() => handleRemove(comment.id)}
                      disabled={removingId === comment.id}
                    >
                      {newsComments.delete}
                    </button>
                  )}
                </div>
                <p className="post-comments__content">{comment.content}</p>
              </li>
            )
          })}
        </ul>
      )}

      {hasNext && (
        <div className="post-comments__more">
          <Button variant="ghost" onClick={onLoadMore} disabled={loadingMore}>
            {newsComments.loadMore}
          </Button>
        </div>
      )}
    </section>
  )
}

export default PostComments
