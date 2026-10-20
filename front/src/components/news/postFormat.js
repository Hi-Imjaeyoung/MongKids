// 게시물 표시용 변환 함수 (수업 현장·몽키즈 연구소 컴포넌트 공용)
import { newsCategories } from '../../data/newsContent'

// categories: 분류 목록 (첫 항목이 '전체'). 없는 id면 첫 항목을 돌려준다
export function findCategory(categoryId, categories = newsCategories) {
  return categories.find((category) => category.id === categoryId) ?? categories[0]
}

// '2026-10-15T14:30:00' → '2026.10.15'
export function formatDate(dateTime) {
  return dateTime ? dateTime.slice(0, 10).replaceAll('-', '.') : ''
}
