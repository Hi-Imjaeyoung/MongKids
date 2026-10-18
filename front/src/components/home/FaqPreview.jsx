import { useState } from 'react'
import SectionHeading from '../common/SectionHeading'
import { faqPreview, sectionHeadings } from '../../data/homeContent'
import './FaqPreview.css'

function FaqPreview() {
  // 한 번에 하나만 펼친다. null이면 모두 접힘
  const [openIndex, setOpenIndex] = useState(null)

  const toggle = (index) => setOpenIndex((current) => (current === index ? null : index))

  return (
    <section className="faq container" id="faq" aria-labelledby="faq-title">
      <SectionHeading {...sectionHeadings.faq} titleId="faq-title" />
      <ul className="faq__list">
        {faqPreview.map((item, index) => {
          const isOpen = openIndex === index
          const answerId = `faq-answer-${index}`
          return (
            <li key={item.question} className={`faq__item ${isOpen ? 'faq__item--open' : ''}`}>
              <button
                type="button"
                className="faq__question"
                aria-expanded={isOpen}
                aria-controls={answerId}
                onClick={() => toggle(index)}
              >
                <span className="faq__q" aria-hidden="true">Q</span>
                <span className="faq__question-text">{item.question}</span>
                <span className="faq__toggle" aria-hidden="true">{isOpen ? '−' : '+'}</span>
              </button>
              <p id={answerId} className="faq__answer" hidden={!isOpen}>
                {item.answer}
              </p>
            </li>
          )
        })}
      </ul>
    </section>
  )
}

export default FaqPreview
