import FaqAccordion from '../common/FaqAccordion'
import FaqTabs from './FaqTabs'
import { faqCategoryLabel, faqsByCategory } from '../../data/faqContent'
import './FaqList.css'

// 서비스 분류 탭 + 질문 목록 (질문마다 분류 표시). category: 선택된 분류 id, onCategoryChange(id)
function FaqList({ category, onCategoryChange }) {
  const items = faqsByCategory(category).map((faq) => ({ ...faq, badge: faqCategoryLabel(faq) }))

  return (
    <section className="faq-list container" aria-label="질문 목록">
      <FaqTabs active={category} onChange={onCategoryChange} />
      <div className="faq-list__items">
        <FaqAccordion items={items} />
      </div>
    </section>
  )
}

export default FaqList
