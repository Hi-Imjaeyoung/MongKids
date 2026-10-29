import { useEffect, useRef } from 'react'
import { toPhoto } from './photoItem'
import './PhotoUploader.css'

// 사진 썸네일 격자 + 추가 칸. 업로드는 하지 않고 고른 사진 목록(photos)만 관리한다.
// rules: { maxCount, maxSize, types }, labels: { add, remove(index), countError, fileError }
// onChange(photos), onError(message | null)
function PhotoUploader({ photos, rules, labels, disabled = false, onChange, onError }) {
  const fileInputRef = useRef(null)
  const objectUrlsRef = useRef([])

  // 미리보기로 만든 주소는 화면을 떠날 때 정리한다
  useEffect(() => {
    const objectUrls = objectUrlsRef.current
    return () => objectUrls.forEach((url) => URL.revokeObjectURL(url))
  }, [])

  const isAllowedFile = (file) => rules.types.includes(file.type) && file.size > 0 && file.size <= rules.maxSize

  const addFiles = (event) => {
    const files = [...event.target.files]
    event.target.value = ''
    if (files.length === 0) return

    if (!files.every(isAllowedFile)) {
      onError(labels.fileError)
      return
    }
    if (photos.length + files.length > rules.maxCount) {
      onError(labels.countError)
      return
    }
    const added = files.map((file) => {
      const url = URL.createObjectURL(file)
      objectUrlsRef.current.push(url)
      return toPhoto({ url, file })
    })
    onChange([...photos, ...added])
    onError(null)
  }

  const removePhoto = (photoId) => {
    onChange(photos.filter((photo) => photo.id !== photoId))
    onError(null)
  }

  return (
    <div className="photo-uploader">
      {photos.length < rules.maxCount && (
        <button
          type="button"
          className="photo-uploader__add"
          disabled={disabled}
          onClick={() => fileInputRef.current?.click()}
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
            <path d="M4 8h3l2-3h6l2 3h3v11H4z" />
            <circle cx="12" cy="13" r="3.5" />
          </svg>
          {labels.add}
        </button>
      )}
      {photos.map((photo, index) => (
        <div key={photo.id} className="photo-uploader__photo">
          <img src={photo.url} alt="" />
          <button
            type="button"
            className="photo-uploader__remove"
            aria-label={labels.remove(index + 1)}
            disabled={disabled}
            onClick={() => removePhoto(photo.id)}
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" aria-hidden="true">
              <path d="M6 6l12 12M18 6L6 18" />
            </svg>
          </button>
        </div>
      ))}
      <input ref={fileInputRef} type="file" accept={rules.types.join(',')} multiple hidden onChange={addFiles} />
    </div>
  )
}

export default PhotoUploader
