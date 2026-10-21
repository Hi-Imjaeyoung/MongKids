import { useState } from 'react'
import { saveReviewReply } from '../../api/reviews'
import { reviewList, reviewReply } from '../../data/reviewContent'
import './ReviewReply.css'

// 후기 카드의 "몽키즈 답글" (FR-017). 관리자에게는 답글 쓰기·수정 칸을 보여 준다.
// 저장하면 onSaved(답글이 반영된 후기)를 부른다
function ReviewReply({ review, canReply, onSaved }) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const reply = review.reply

  if (!reply && !canReply) return null

  const startEdit = () => {
    setDraft(reply?.content ?? '')
    setError(null)
    setEditing(true)
  }

  const submit = async (event) => {
    event.preventDefault()
    if (!draft.trim() || saving) return
    setSaving(true)
    setError(null)
    try {
      onSaved(await saveReviewReply(review.id, draft.trim()))
      setEditing(false)
    } catch (saveError) {
      setError(saveError.message)
    } finally {
      setSaving(false)
    }
  }

  if (editing) {
    const inputId = `review-reply-${review.id}`
    return (
      <form className="review-reply review-reply--editing" onSubmit={submit}>
        <label htmlFor={inputId} className="review-reply__title">
          <ReplyIcon />
          {reply ? reviewReply.editTitle : reviewReply.writeTitle}
        </label>
        <textarea
          id={inputId}
          className="review-reply__textarea"
          rows={3}
          maxLength={reviewReply.maxLength}
          placeholder={reviewReply.placeholder}
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
        />
        <div className="review-reply__form-footer">
          <span className="review-reply__count">
            {draft.length.toLocaleString()} / {reviewReply.maxLength.toLocaleString()}자
          </span>
          <div className="review-reply__buttons">
            <button type="button" className="review-reply__cancel" disabled={saving} onClick={() => setEditing(false)}>
              {reviewReply.cancel}
            </button>
            <button type="submit" className="review-reply__save" disabled={!draft.trim() || saving}>
              {saving ? reviewReply.saving : reviewReply.save}
            </button>
          </div>
        </div>
        {error && (
          <p className="review-reply__error" role="alert">
            {error}
          </p>
        )}
      </form>
    )
  }

  if (!reply) {
    return (
      <button type="button" className="review-reply__write" onClick={startEdit}>
        <ReplyIcon />
        {reviewReply.write}
      </button>
    )
  }

  return (
    <div className="review-reply">
      <div className="review-reply__head">
        <p className="review-reply__title">
          <ReplyIcon />
          {reviewList.replyTitle}
        </p>
        {canReply && (
          <button type="button" className="review-reply__edit" onClick={startEdit}>
            {reviewReply.edit}
          </button>
        )}
      </div>
      <p className="review-reply__content">{reply.content}</p>
    </div>
  )
}

function ReplyIcon() {
  return (
    <svg className="review-reply__icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M9 14 4 9l5-5" />
      <path d="M4 9h10a6 6 0 0 1 6 6v5" />
    </svg>
  )
}

export default ReviewReply
