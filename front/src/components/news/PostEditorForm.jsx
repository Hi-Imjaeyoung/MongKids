import { useEffect, useRef, useState } from 'react'
import Button from '../common/Button'
import ImageUploader from './ImageUploader'
import { newsCategories, newsEditor } from '../../data/newsContent'
import { postImageRules } from '../../api/posts'
import './PostEditorForm.css'

const writableCategories = newsCategories.filter((category) => category.id !== 'ALL')

let nextImageId = 0

// 기존 사진은 imageKey, 새로 고른 사진은 file을 가진다
function toImageItems(post) {
  return (post?.images ?? []).map((image) => ({ id: nextImageId++, imageKey: image.imageKey, url: image.url }))
}

function isAllowedFile(file) {
  return postImageRules.types.includes(file.type) && file.size <= postImageRules.maxSize
}

// post: 수정할 게시물 (작성이면 null). onSubmit({ category, title, content, images })는 Promise를 돌려준다
function PostEditorForm({ post = null, onSubmit, onCancel }) {
  const [category, setCategory] = useState(post?.category ?? writableCategories[0].id)
  const [title, setTitle] = useState(post?.title ?? '')
  const [content, setContent] = useState(post?.content ?? '')
  const [images, setImages] = useState(() => toImageItems(post))
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  // 미리보기용 object URL을 화면을 떠날 때 해제한다
  const imagesRef = useRef(images)
  const changeImages = (next) => {
    imagesRef.current = next
    setImages(next)
  }
  useEffect(() => {
    return () => {
      imagesRef.current.forEach((image) => image.file && URL.revokeObjectURL(image.url))
    }
  }, [])

  const handleAdd = (files) => {
    if (images.length + files.length > postImageRules.maxCount) {
      setError(newsEditor.errors.tooMany)
      return
    }
    if (!files.every(isAllowedFile)) {
      setError(newsEditor.errors.invalidFile)
      return
    }
    setError(null)
    changeImages([...images, ...files.map((file) => ({ id: nextImageId++, file, url: URL.createObjectURL(file) }))])
  }

  const handleMove = (index, step) => {
    const next = [...images]
    ;[next[index], next[index + step]] = [next[index + step], next[index]]
    changeImages(next)
  }

  const handleRemove = (index) => {
    const removed = images[index]
    if (removed.file) URL.revokeObjectURL(removed.url)
    changeImages(images.filter((_, i) => i !== index))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!title.trim() || !content.trim()) {
      setError(newsEditor.errors.required)
      return
    }
    setError(null)
    setSubmitting(true)
    try {
      await onSubmit({ category, title: title.trim(), content, images })
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
        <label htmlFor="post-content" className="post-editor__label">
          {newsEditor.content}
        </label>
        <textarea
          id="post-content"
          className="post-editor__input post-editor__textarea"
          value={content}
          placeholder={newsEditor.contentPlaceholder}
          onChange={(event) => setContent(event.target.value)}
          disabled={submitting}
          required
        />
      </div>

      <div className="post-editor__field">
        <span className="post-editor__label">{newsEditor.images}</span>
        <p className="post-editor__hint">{newsEditor.imagesHint}</p>
        <ImageUploader
          images={images}
          onAdd={handleAdd}
          onMove={handleMove}
          onRemove={handleRemove}
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
        <Button type="submit" variant="blue" size="lg" disabled={submitting}>
          {submitting ? newsEditor.submitting : post ? newsEditor.submitEdit : newsEditor.submitCreate}
        </Button>
      </div>
    </form>
  )
}

export default PostEditorForm
