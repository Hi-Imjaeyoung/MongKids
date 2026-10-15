import { useMemo } from 'react'
import DOMPurify from 'dompurify'
import './PostBody.css'

// 서버에서 이미 정화한 HTML이지만 화면에 넣기 전에 같은 허용 목록으로 한 번 더 거른다
const PURIFY_OPTIONS = {
  ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 'a', 'ul', 'ol', 'li', 'blockquote', 'hr', 'figure', 'figcaption', 'img'],
  ALLOWED_ATTR: ['href', 'target', 'rel', 'class', 'src', 'alt', 'data-image-key'],
}

// html: 상세 API 응답의 본문 (사진 src가 채워진 HTML)
function PostContent({ html }) {
  const safeHtml = useMemo(() => DOMPurify.sanitize(html, PURIFY_OPTIONS), [html])
  return <div className="post-body" dangerouslySetInnerHTML={{ __html: safeHtml }} />
}

export default PostContent
