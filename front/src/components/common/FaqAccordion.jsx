import { useState } from 'react'
import './FaqAccordion.css'

// 질문 목록 아코디언. 여러 질문을 함께 펼칠 수 있다.
// items: { id, question, answer, badge? } (badge: 질문 옆에 표시할 분류), defaultOpenIds: 처음에 펼쳐 둘 질문 id
function FaqAccordion({ items, defaultOpenIds = [] }) {
  const [openIds, setOpenIds] = useState(() => new Set(defaultOpenIds))

  const toggle = (id) =>
    setOpenIds((current) => {
      const next = new Set(current)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })

  return (
    <ul className="faq-accordion">
      {items.map((item) => {
        const isOpen = openIds.has(item.id)
        const answerId = `faq-accordion-answer-${item.id}`
        return (
          <li key={item.id} className={`faq-accordion__item ${isOpen ? 'faq-accordion__item--open' : ''}`}>
            <button
              type="button"
              className="faq-accordion__question"
              aria-expanded={isOpen}
              aria-controls={answerId}
              onClick={() => toggle(item.id)}
            >
              <span className="faq-accordion__q" aria-hidden="true">Q</span>
              <span className="faq-accordion__text">
                <span className="faq-accordion__question-text">{item.question}</span>
                {item.badge && <span className="faq-accordion__category">{item.badge}</span>}
              </span>
              <svg
                className="faq-accordion__toggle"
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.4"
                strokeLinecap="round"
                aria-hidden="true"
              >
                {isOpen ? <path d="M5 12h14" /> : <path d="M12 5v14M5 12h14" />}
              </svg>
            </button>
            <p id={answerId} className="faq-accordion__answer" hidden={!isOpen}>
              {item.answer}
            </p>
          </li>
        )
      })}
    </ul>
  )
}

export default FaqAccordion
