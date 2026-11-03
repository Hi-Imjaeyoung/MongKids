import { useState } from 'react'
import ChevronIcon from '../common/ChevronIcon'
import './PrivacyToc.css'

// 개인정보처리방침 목차 (SCR-023). 데스크톱은 왼쪽에 고정해 보여 주고, 모바일은 버튼으로 펼친다
// activeId: 지금 보고 있는 조항 id
function PrivacyToc({ label, sections, activeId }) {
  const [open, setOpen] = useState(false)

  return (
    <nav className="privacy-toc" aria-label={label}>
      <button
        type="button"
        className="privacy-toc__toggle"
        aria-expanded={open}
        aria-controls="privacy-toc-list"
        onClick={() => setOpen((value) => !value)}
      >
        {label}
        <span className={`privacy-toc__chevron ${open ? 'privacy-toc__chevron--open' : ''}`}>
          <ChevronIcon size={18} />
        </span>
      </button>
      <p className="privacy-toc__title">{label}</p>
      <ol id="privacy-toc-list" className={`privacy-toc__list ${open ? 'privacy-toc__list--open' : ''}`}>
        {sections.map((section) => {
          const active = section.id === activeId
          return (
            <li key={section.id}>
              <a
                href={`#${section.id}`}
                className={`privacy-toc__link ${active ? 'privacy-toc__link--active' : ''}`}
                aria-current={active ? 'true' : undefined}
                onClick={() => setOpen(false)}
              >
                {section.tocLabel}
              </a>
            </li>
          )
        })}
      </ol>
    </nav>
  )
}

export default PrivacyToc
