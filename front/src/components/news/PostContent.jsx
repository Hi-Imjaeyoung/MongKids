import { useEffect, useMemo, useRef, useState } from 'react'
import DOMPurify from 'dompurify'
import ReviewPhotoViewer from '../review/ReviewPhotoViewer'
import { newsDetail } from '../../data/newsContent'
import './PostBody.css'

// 서버에서 이미 정화한 HTML이지만 화면에 넣기 전에 같은 허용 목록으로 한 번 더 거른다
const PURIFY_OPTIONS = {
  ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 'a', 'ul', 'ol', 'li', 'blockquote', 'hr', 'figure', 'figcaption', 'img'],
  ALLOWED_ATTR: ['href', 'target', 'rel', 'class', 'src', 'alt', 'data-image-key'],
}

// html: 상세 API 응답의 본문 (사진 src가 채워진 HTML)
// 본문 사진을 누르면(또는 Enter·Space) 후기와 같은 사진 크게 보기로 연다 (SCR-006, 2026-11-05)
function PostContent({ html }) {
  const safeHtml = useMemo(() => DOMPurify.sanitize(html, PURIFY_OPTIONS), [html])
  const bodyRef = useRef(null)
  const [viewer, setViewer] = useState(null)

  // 본문 사진에 키보드 초점이 가도록 버튼 역할을 붙인다
  useEffect(() => {
    const images = [...bodyRef.current.querySelectorAll('img')]
    images.forEach((image, index) => {
      image.tabIndex = 0
      image.setAttribute('role', 'button')
      image.setAttribute('aria-label', newsDetail.photoOpen(index + 1, images.length))
    })
  }, [safeHtml])

  const openPhoto = (target) => {
    if (target.tagName !== 'IMG') return
    const images = [...bodyRef.current.querySelectorAll('img')]
    setViewer({ images: images.map((image) => ({ url: image.src })), startIndex: images.indexOf(target) })
  }

  return (
    <>
      <div
        ref={bodyRef}
        className="post-body"
        dangerouslySetInnerHTML={{ __html: safeHtml }}
        onClick={(event) => openPhoto(event.target)}
        onKeyDown={(event) => {
          if (event.key !== 'Enter' && event.key !== ' ') return
          if (event.target.tagName !== 'IMG') return
          event.preventDefault()
          openPhoto(event.target)
        }}
      />
      {viewer && (
        <ReviewPhotoViewer
          images={viewer.images}
          startIndex={viewer.startIndex}
          photoLabel={(index) => newsDetail.photoAlt(index, viewer.images.length)}
          onClose={() => setViewer(null)}
        />
      )}
    </>
  )
}

export default PostContent
