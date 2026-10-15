// 게시물 표시용 변환 함수 (몽키즈 소식 컴포넌트 공용)
import { newsCategories } from '../../data/newsContent'

export function findCategory(categoryId) {
  return newsCategories.find((category) => category.id === categoryId) ?? newsCategories[0]
}

// '2026-10-15T14:30:00' → '2026.10.15'
export function formatDate(dateTime) {
  return dateTime ? dateTime.slice(0, 10).replaceAll('-', '.') : ''
}
