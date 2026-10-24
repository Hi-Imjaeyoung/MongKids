import { useCallback, useState } from 'react'
import Button from '../common/Button'
import PostContentEditor from './PostContentEditor'
import { newsCategories, newsEditor } from '../../data/newsContent'
import './PostEditorForm.css'

// categories: 분류 목록 (첫 항목 '전체'는 고를 수 없다)
function writableOf(categories) {
  return categories.filter((category) => category.id !== 'ALL')
}

// post: 수정할 게시물 (작성이면 null). onSubmit({ category, title, content })는 Promise를 돌려준다.
// categories: 분류 목록 (기본은 수업 현장). notice: 분류 아래 작성자 안내 (몽키즈 연구소)
// uploadImages(files): 묶음의 사진 업로드 (기본은 수업 현장)
function PostEditorForm({ post = null, onSubmit, onCancel, categories = newsCategories, notice, uploadImages }) {
  const writableCategories = writableOf(categories)
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
    if (!title.trim() || !content.hasContent) {
      setError(newsEditor.errors.required)
      return
    }
    setError(null)
    setSubmitting(true)
    try {
      await onSubmit({ category, title: title.trim(), content: content.html })
    } catch (submitError) {
      setError(submitError.message || newsEditor.errors.submit)
      setSubmitting(false)
    }
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

      <div className="post-editor__field">
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
