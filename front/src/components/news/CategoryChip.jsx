import { findCategory } from './postFormat'
import './CategoryChip.css'

// categories: 분류 목록 (기본은 수업 현장)
function CategoryChip({ category, categories }) {
  const { label, tone } = findCategory(category, categories)
  return <span className={`category-chip category-chip--${tone}`}>{label}</span>
}

export default CategoryChip
