import { useCallback, useState } from 'react'
import Button from '../common/Button'
import PhotoUploader from '../common/PhotoUploader'
import { toPhoto } from '../common/photoItem'
import PostContentEditor from './PostContentEditor'
import { MAX_CARD_PHOTOS, parsePhotoOnly, toPhotoOnlyHtml } from './photoOnlyContent'
import { newsCategories, newsEditor } from '../../data/newsContent'
import { postImageRules, uploadPostImages } from '../../api/posts'
import './PostEditorForm.css'

// categories: 분류 목록 (첫 항목 '전체'는 고를 수 없다)
function writableOf(categories) {
  return categories.filter((category) => category.id !== 'ALL')
}

// post: 수정할 게시물 (작성이면 null). onSubmit({ category, title, content })는 Promise를 돌려준다.
// categories: 분류 목록 (기본은 수업 현장). notice: 분류 아래 작성자 안내 (몽키즈 연구소)
// uploadImages(files): 묶음의 사진 업로드 (기본은 수업 현장)
// allowPhotoOnly: 형식(글과 사진 / 사진만) 선택을 보여 준다. 사진만은 상세에서 카드뉴스로 보인다 (수업 현장)
const PHOTO_RULES = { ...postImageRules, maxCount: MAX_CARD_PHOTOS }

function PostEditorForm({
  post = null,
  onSubmit,
  onCancel,
  categories = newsCategories,
  notice,
  uploadImages = uploadPostImages,
  allowPhotoOnly = false,
}) {
  const writableCategories = writableOf(categories)
  // 수정할 게시물이 사진만이면 사진만 형식으로 연다
  const [initialPhotos] = useState(() => (allowPhotoOnly ? parsePhotoOnly(post?.content) : null))
  const [format, setFormat] = useState(initialPhotos ? 'photos' : 'text')
  const [photos, setPhotos] = useState(() => (initialPhotos ?? []).map(toPhoto))
  const [category, setCategory] = useState(post?.category ?? writableCategories[0].id)
  const [title, setTitle] = useState(post?.title ?? '')
  const [content, setContent] = useState({ html: post?.content ?? '', hasContent: Boolean(post) })
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const handleContentChange = useCallback((next) => setContent(next), [])

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (uploading) {
      setError(newsEditor.errors.uploading)
      return
    }
    const photoOnly = format === 'photos'
    if (photoOnly ? !title.trim() || photos.length === 0 : !title.trim() || !content.hasContent) {
      setError(photoOnly ? newsEditor.errors.photosRequired : newsEditor.errors.required)
      return
    }
    setError(null)
    setSubmitting(true)
    try {
      await onSubmit({
        category,
        title: title.trim(),
        content: photoOnly ? toPhotoOnlyHtml(await uploadPhotos()) : content.html,
      })
    } catch (submitError) {
      setError(submitError.message || newsEditor.errors.submit)
      setSubmitting(false)
    }
  }

  // 사진만 형식: 새로 고른 사진만 올리고, 저장된 사진은 키를 그대로 쓴다. 고른 순서대로 키를 돌려준다
  const uploadPhotos = async () => {
    const files = photos.filter((photo) => !photo.imageKey).map((photo) => photo.file)
    let uploadedKeys
    try {
      uploadedKeys = files.length > 0 ? await uploadImages(files) : []
    } catch {
      throw new Error(newsEditor.errors.upload)
    }
    let next = 0
    return photos.map((photo) => photo.imageKey ?? uploadedKeys[next++])
  }

  return (
    <form className="post-editor container" onSubmit={handleSubmit} noValidate>
      <h1 className="post-editor__heading">{post ? newsEditor.editTitle : newsEditor.createTitle}</h1>

      <fieldset className="post-editor__field">
        <legend className="post-editor__label">{newsEditor.category}</legend>
        <div className="post-editor__categories">
          {writableCategories.map((option) => (
            <label key={option.id} className="post-editor__category">
              <input
                type="radio"
                name="category"
                value={option.id}
                checked={category === option.id}
                onChange={() => setCategory(option.id)}
                disabled={submitting}
              />
              <span>{option.label}</span>
            </label>
          ))}
        </div>
        {notice && <p className="post-editor__notice">{notice}</p>}
      </fieldset>

      <div className="post-editor__field">
        <label htmlFor="post-title" className="post-editor__label">
          {newsEditor.title}
        </label>
        <input
          id="post-title"
          className="post-editor__input"
          value={title}
          maxLength={100}
          placeholder={newsEditor.titlePlaceholder}
          onChange={(event) => setTitle(event.target.value)}
          disabled={submitting}
          required
        />
      </div>

      {allowPhotoOnly && (
        <fieldset className="post-editor__field">
          <legend className="post-editor__label">{newsEditor.format}</legend>
          <div className="post-editor__categories">
            {newsEditor.formats.map((option) => (
              <label key={option.id} className="post-editor__category">
                <input
                  type="radio"
                  name="format"
                  value={option.id}
                  checked={format === option.id}
                  onChange={() => {
                    setFormat(option.id)
                    setError(null)
                  }}
                  disabled={submitting}
                />
                <span>{option.label}</span>
              </label>
            ))}
          </div>
        </fieldset>
      )}

      <div className="post-editor__field" hidden={format !== 'photos'}>
        <div className="post-editor__photos-head">
          <span className="post-editor__label">{newsEditor.photosLabel}</span>
          <span className="post-editor__count">
            {photos.length} / {MAX_CARD_PHOTOS}
          </span>
        </div>
        <p className="post-editor__hint">{newsEditor.photosHint}</p>
        <PhotoUploader
          photos={photos}
          rules={PHOTO_RULES}
          labels={newsEditor.photoUploader}
          disabled={submitting}
          reorderable
          onChange={setPhotos}
          onError={setError}
        />
      </div>

      {/* 형식을 바꿔도 쓰던 본문이 남도록 편집기는 숨기기만 한다 */}
      <div className="post-editor__field" hidden={format !== 'text'}>
        <span className="post-editor__label">{newsEditor.content}</span>
        <p className="post-editor__hint">{newsEditor.contentHint}</p>
        <PostContentEditor
          initialContent={post?.content ?? ''}
          onChange={handleContentChange}
          onUploadingChange={setUploading}
          onError={setError}
          uploadImages={uploadImages}
          disabled={submitting}
        />
      </div>

      {error && (
        <p className="post-editor__error" role="alert">
          {error}
        </p>
      )}

      <div className="post-editor__actions">
        <button type="button" className="post-editor__cancel" onClick={onCancel} disabled={submitting}>
          {newsEditor.cancel}
        </button>
        <Button type="submit" variant="blue" size="lg" disabled={submitting || uploading}>
          {submitting ? newsEditor.submitting : post ? newsEditor.submitEdit : newsEditor.submitCreate}
        </Button>
      </div>
    </form>
  )
}

export default PostEditorForm
