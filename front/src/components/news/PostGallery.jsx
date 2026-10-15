import { useState } from 'react'
import { newsDetail } from '../../data/newsContent'
import './PostGallery.css'

// images: [{ imageKey, url }] (0번이 대표 사진)
function PostGallery({ images }) {
  const [selected, setSelected] = useState(0)
  if (images.length === 0) return null

  const current = images[Math.min(selected, images.length - 1)]
  return (
    <div className="post-gallery">
      <div className="post-gallery__main">
        <img
          className="post-gallery__image"
          src={current.url}
          alt={newsDetail.photoAlt(images.indexOf(current) + 1, images.length)}
        />
      </div>
      {images.length > 1 && (
        <ul className="post-gallery__thumbs">
          {images.map((image, index) => (
            <li key={image.imageKey}>
              <button
                type="button"
                className={`post-gallery__thumb ${image === current ? 'post-gallery__thumb--active' : ''}`}
                aria-label={newsDetail.photoAlt(index + 1, images.length)}
                aria-pressed={image === current}
                onClick={() => setSelected(index)}
              >
                <img src={image.url} alt="" loading="lazy" />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default PostGallery
