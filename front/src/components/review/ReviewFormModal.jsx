import { useEffect, useRef, useState } from 'react'
import { createReview, reviewImageRules, updateReview, uploadReviewImages } from '../../api/reviews'
import { reviewForm, reviewTypes } from '../../data/reviewContent'
import BananaRating from './BananaRating'
import './ReviewFormModal.css'

const WRITABLE_TYPES = reviewTypes.filter((type) => type.id !== 'ALL')

let nextPhotoId = 0

// 사진 칸: 이미 저장된 사진은 imageKey, 새로 고른 사진은 file을 가진다
function toPhoto({ imageKey, url, file }) {
  nextPhotoId += 1
  return { id: nextPhotoId, imageKey, url, file }
}

function isAllowedFile(file) {
  return reviewImageRules.types.includes(file.type) && file.size > 0 && file.size <= reviewImageRules.maxSize
}

// 후기 작성·수정 모달 (SCR-012, FR-013·015·016·025). 데스크톱은 모달, 모바일은 전체 화면.
// review가 있으면 그 값으로 채워 수정한다. 저장하면 onSaved(저장된 후기)를 부르고 완료 화면을 보여 준다
function ReviewFormModal({ review, onSaved, onClose }) {
  const editing = Boolean(review)
  const [type, setType] = useState(review?.type ?? null)
  const [satisfaction, setSatisfaction] = useState(review?.satisfaction ?? 0)
  const [content, setContent] = useState(review?.content ?? '')
  const [photos, setPhotos] = useState(() => (review?.images ?? []).map(toPhoto))
  const [photoError, setPhotoError] = useState(null)
  const [submitError, setSubmitError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [done, setDone] = useState(false)
  const fileInputRef = useRef(null)
  const objectUrlsRef = useRef([])

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape' && !submitting) onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [onClose, submitting])

  // 미리보기로 만든 주소는 모달을 닫을 때 정리한다
  useEffect(() => {
    const objectUrls = objectUrlsRef.current
    return () => objectUrls.forEach((url) => URL.revokeObjectURL(url))
  }, [])

  const canSubmit = Boolean(type) && satisfaction > 0 && content.trim().length > 0 && !submitting

  const addFiles = (event) => {
    const files = [...event.target.files]
    event.target.value = ''
    if (files.length === 0) return

    if (!files.every(isAllowedFile)) {
      setPhotoError(reviewForm.photoFileError)
      return
    }
    if (photos.length + files.length > reviewImageRules.maxCount) {
      setPhotoError(reviewForm.photoCountError)
      return
    }
    const added = files.map((file) => {
      const url = URL.createObjectURL(file)
      objectUrlsRef.current.push(url)
      return toPhoto({ url, file })
    })
    setPhotos((prev) => [...prev, ...added])
    setPhotoError(null)
  }

  const removePhoto = (photoId) => {
    setPhotos((prev) => prev.filter((photo) => photo.id !== photoId))
    setPhotoError(null)
  }

  const submit = async (event) => {
    event.preventDefault()
    if (!canSubmit) return
    setSubmitting(true)
    setSubmitError(null)
    try {
      const newPhotos = photos.filter((photo) => photo.file)
      const uploadedKeys = await uploadReviewImages(newPhotos.map((photo) => photo.file))
      const keyByPhotoId = new Map(newPhotos.map((photo, index) => [photo.id, uploadedKeys[index]]))
      const body = {
        type,
        satisfaction,
        content: content.trim(),
        imageKeys: photos.map((photo) => photo.imageKey ?? keyByPhotoId.get(photo.id)),
      }
      const saved = editing ? await updateReview(review.id, body) : await createReview(body)
      onSaved(saved)
      setDone(true)
    } catch (error) {
      setSubmitError(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  const close = () => {
    if (!submitting) onClose()
  }

  return (
    <div className="review-modal" onClick={close}>
      <div
        className={`review-modal__dialog ${done ? 'review-modal__dialog--done' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="review-modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="review-modal__head">
          <h2 id="review-modal-title" className="review-modal__title">
            {editing ? reviewForm.editTitle : reviewForm.createTitle}
          </h2>
          <button type="button" className="review-modal__close" aria-label={reviewForm.close} onClick={close}>
            ✕
          </button>
        </div>

        {done ? (
          <div className="review-modal__done">
            <span className="review-modal__done-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path d="M5 12l5 5 9-10" />
              </svg>
            </span>
            <p className="review-modal__done-title">{editing ? reviewForm.doneEditTitle : reviewForm.doneTitle}</p>
            <p className="review-modal__done-description">{reviewForm.doneDescription}</p>
            <button type="button" className="review-modal__submit review-modal__done-button" onClick={onClose}>
              {reviewForm.doneButton}
            </button>
          </div>
        ) : (
          <form className="review-modal__form" onSubmit={submit}>
            <fieldset className="review-modal__field">
              <legend className="review-modal__label">
                {reviewForm.typeLabel} <span className="review-modal__required">*</span>
              </legend>
              <div className="review-modal__types">
                {WRITABLE_TYPES.map((item) => (
                  <button
                    key={item.id}
                    type="button"
                    className={`review-modal__type ${type === item.id ? 'review-modal__type--active' : ''}`}
                    aria-pressed={type === item.id}
                    onClick={() => setType(item.id)}
                  >
                    {item.label}
                  </button>
                ))}
              </div>
            </fieldset>

            <fieldset className="review-modal__field">
              <legend className="review-modal__label">
                {reviewForm.ratingLabel} <span className="review-modal__required">*</span>
              </legend>
              <div className="review-modal__rating">
                <BananaRating value={satisfaction} onChange={setSatisfaction} />
                <p className="review-modal__rating-text">
                  {satisfaction > 0 ? reviewForm.ratingText(satisfaction) : reviewForm.ratingHint}
                </p>
              </div>
            </fieldset>

            <div className="review-modal__field">
              <label htmlFor="review-content" className="review-modal__label">
                {reviewForm.contentLabel} <span className="review-modal__required">*</span>
              </label>
              <textarea
                id="review-content"
                className="review-modal__textarea"
                rows={7}
                maxLength={reviewForm.contentMaxLength}
                placeholder={reviewForm.contentPlaceholder}
                value={content}
                onChange={(event) => setContent(event.target.value)}
              />
              <p className="review-modal__count">
                {content.length.toLocaleString()} / {reviewForm.contentMaxLength.toLocaleString()}자
              </p>
            </div>

            <div className="review-modal__field">
              <p className="review-modal__label">
                {reviewForm.photoLabel} <span className="review-modal__rule">{reviewForm.photoRule}</span>
              </p>
              <p className="review-modal__hint">{reviewForm.photoHint}</p>
              <div className="review-modal__photos">
                {photos.map((photo, index) => (
                  <div key={photo.id} className="review-modal__photo">
                    <img src={photo.url} alt="" />
                    <button
                      type="button"
                      className="review-modal__photo-remove"
                      aria-label={reviewForm.photoRemove(index + 1)}
                      onClick={() => removePhoto(photo.id)}
                    >
                      ✕
                    </button>
                  </div>
                ))}
                {photos.length < reviewImageRules.maxCount && (
                  <button
                    type="button"
                    className="review-modal__photo-add"
                    onClick={() => fileInputRef.current?.click()}
                  >
                    <span aria-hidden="true">＋</span>
                    {reviewForm.photoAdd}
                  </button>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept={reviewImageRules.types.join(',')}
                multiple
                hidden
                onChange={addFiles}
              />
              {photoError && (
                <p className="review-modal__error" role="alert">
                  {photoError}
                </p>
              )}
            </div>

            <ul className="review-modal__notices">
              {reviewForm.notices.map((notice) => (
                <li key={notice}>{notice}</li>
              ))}
            </ul>

            <div className="review-modal__actions">
              {submitError && (
                <p className="review-modal__error" role="alert">
                  {submitError}
                </p>
              )}
              <button type="submit" className="review-modal__submit" disabled={!canSubmit}>
                {submitting ? reviewForm.submitting : editing ? reviewForm.submitEdit : reviewForm.submit}
              </button>
              {!canSubmit && !submitting && <p className="review-modal__required-hint">{reviewForm.required}</p>}
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

export default ReviewFormModal
