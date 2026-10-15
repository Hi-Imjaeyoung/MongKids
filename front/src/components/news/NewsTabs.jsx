import { newsCategories } from '../../data/newsContent'
import './NewsTabs.css'

// active: 선택된 분류 id, onChange(id)
function NewsTabs({ active, onChange }) {
  return (
    <div className="news-tabs" role="group" aria-label="분류">
      {newsCategories.map((category) => (
        <button
          key={category.id}
          type="button"
          className={`news-tabs__tab ${category.id === active ? 'news-tabs__tab--active' : ''}`}
          aria-pressed={category.id === active}
          onClick={() => onChange(category.id)}
        >
          {category.label}
        </button>
      ))}
    </div>
  )
}

export default NewsTabs
