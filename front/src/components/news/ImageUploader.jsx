import { useId } from 'react'
import { newsEditor } from '../../data/newsContent'
import { postImageRules } from '../../api/posts'
import './ImageUploader.css'

// images: [{ id, url }] 표시 순서대로. onAdd(files), onMove(index, step), onRemove(index)
function ImageUploader({ images, onAdd, onMove, onRemove, disabled }) {
  const inputId = useId()

  const handleChange = (event) => {
    onAdd(Array.from(event.target.files))
    // 같은 파일을 다시 고를 수 있도록 비운다
    event.target.value = ''
  }

  return (
    <div className="image-uploader">
      <ul className="image-uploader__list">
        {images.map((image, index) => (
          <li key={image.id} className="image-uploader__item">
            <img className="image-uploader__preview" src={image.url} alt="" />
            {index === 0 && <span className="image-uploader__badge">{newsEditor.representative}</span>}
            <div className="image-uploader__controls">
              <button
                type="button"
                className="image-uploader__control"
                aria-label={`${index + 1}번 사진 ${newsEditor.moveBefore}`}
                onClick={() => onMove(index, -1)}
                disabled={disabled || index === 0}
              >
                ‹
              </button>
              <button
                type="button"
                className="image-uploader__control"
                aria-label={`${index + 1}번 사진 ${newsEditor.removeImage}`}
                onClick={() => onRemove(index)}
                disabled={disabled}
              >
                ×
              </button>
              <button
                type="button"
                className="image-uploader__control"
                aria-label={`${index + 1}번 사진 ${newsEditor.moveAfter}`}
                onClick={() => onMove(index, 1)}
                disabled={disabled || index === images.length - 1}
              >
                ›
              </button>
            </div>
          </li>
        ))}
        {images.length < postImageRules.maxCount && (
          <li className="image-uploader__add-item">
            <input
              id={inputId}
              className="image-uploader__input"
              type="file"
              accept={postImageRules.types.join(',')}
              multiple
              onChange={handleChange}
              disabled={disabled}
            />
            <label htmlFor={inputId} className="image-uploader__add">
              <span className="image-uploader__add-icon" aria-hidden="true">＋</span>
              {newsEditor.addImages}
              <span className="image-uploader__count">
                {images.length}/{postImageRules.maxCount}
              </span>
            </label>
          </li>
        )}
      </ul>
    </div>
  )
}

export default ImageUploader
