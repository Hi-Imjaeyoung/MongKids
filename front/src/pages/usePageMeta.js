import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import { siteMeta } from '../data/seoContent'

function setMeta(selector, attribute, value) {
  document.querySelector(selector)?.setAttribute(attribute, value)
}

// 화면별 탭 제목·검색 설명·대표 주소(canonical)를 바꾼다 (NFR-006).
// title이 없으면 기본 제목(홈). 화면을 떠나면 index.html의 기본값으로 되돌린다
function usePageMeta(title, description = siteMeta.description) {
  const { pathname } = useLocation()

  useEffect(() => {
    document.title = title ? `${title} | ${siteMeta.name}` : siteMeta.title
    setMeta('meta[name="description"]', 'content', description)
    setMeta('link[rel="canonical"]', 'href', `${siteMeta.origin}${pathname}`)
    return () => {
      document.title = siteMeta.title
      setMeta('meta[name="description"]', 'content', siteMeta.description)
      setMeta('link[rel="canonical"]', 'href', `${siteMeta.origin}/`)
    }
  }, [title, description, pathname])
}

export default usePageMeta
