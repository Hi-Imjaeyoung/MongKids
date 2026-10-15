import { findCategory } from './postFormat'
import './CategoryChip.css'

function CategoryChip({ category }) {
  const { label, tone } = findCategory(category)
  return <span className={`category-chip category-chip--${tone}`}>{label}</span>
}

export default CategoryChip
