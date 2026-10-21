import { faqCategories } from '../../data/faqContent'
import './FaqTabs.css'

// 서비스 분류 탭. active: 선택된 분류 id, onChange(id)
function FaqTabs({ active, onChange }) {
  return (
    <div className="faq-tabs" role="group" aria-label="서비스 분류">
      {faqCategories.map((category) => (
        <button
          key={category.id}
          type="button"
          className={`faq-tabs__tab ${category.id === active ? 'faq-tabs__tab--active' : ''}`}
          aria-pressed={category.id === active}
          onClick={() => onChange(category.id)}
        >
          {category.label}
        </button>
      ))}
    </div>
  )
}

export default FaqTabs
