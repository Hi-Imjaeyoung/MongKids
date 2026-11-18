import { useSyncExternalStore } from 'react'
import { labHero } from '../../data/labContent'
import './LabHero.css'

// 모바일 폭이면 검색 안내 문구를 짧게 바꿔 잘리지 않게 한다 (SCR-024, 2026-11-12)
const MOBILE_QUERY = '(max-width: 768px)'

function subscribeMobile(onChange) {
  const media = window.matchMedia(MOBILE_QUERY)
  media.addEventListener('change', onChange)
  return () => media.removeEventListener('change', onChange)
}

function isMobile() {
  return window.matchMedia(MOBILE_QUERY).matches
}

// keyword: 현재 검색어, onSearch(keyword). 검색어가 바뀌면(지우기 포함) 입력칸을 새로 그린다
function LabHero({ keyword, onSearch }) {
  const mobile = useSyncExternalStore(subscribeMobile, isMobile)
  const handleSubmit = (event) => {
    event.preventDefault()
    onSearch(new FormData(event.currentTarget).get('keyword').trim())
  }

  return (
    <section className="lab-hero" aria-labelledby="lab-hero-title">
      <div className="lab-hero__inner container">
        <p className="lab-hero__eyebrow">{labHero.eyebrow}</p>
        <h1 id="lab-hero-title" className="lab-hero__title">
          {labHero.title}
        </h1>
        <p className="lab-hero__description">{labHero.description}</p>
        <form key={keyword} className="lab-search" role="search" aria-label={labHero.searchLabel} onSubmit={handleSubmit}>
          <svg
            className="lab-search__icon"
            width="22"
            height="22"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <circle cx="11" cy="11" r="7" />
            <path d="M20 20l-3.5-3.5" />
          </svg>
          <label htmlFor="lab-search-input" className="lab-search__label">
            {labHero.searchInputLabel}
          </label>
          <input
            id="lab-search-input"
            className="lab-search__input"
            type="search"
            name="keyword"
            defaultValue={keyword}
            maxLength={100}
            placeholder={mobile ? labHero.searchPlaceholderShort : labHero.searchPlaceholder}
          />
          <button type="submit" className="lab-search__button">
            {labHero.searchButton}
          </button>
        </form>
      </div>
    </section>
  )
}

export default LabHero
