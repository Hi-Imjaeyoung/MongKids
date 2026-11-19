// ABOUT 몽키즈(SCR-002) 월별 커리큘럼 편집 (FR-034) API
import { request } from './client'

// 관리자가 저장한 3줄 [{ month, topic }]. 저장 값이 없으면 빈 배열이다 (화면은 기본 문구 사용)
export function getAboutCurriculumMonths() {
  return request('/api/v1/about-curriculum-months')
}

// 관리자만. months: 3줄 [{ month, topic }]. 저장된 3줄을 돌려준다
export function saveAboutCurriculumMonths(months) {
  return request('/api/v1/admin/about-curriculum-months', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ months }),
  })
}

// 관리자만. 저장 값을 지워 기본 문구로 되돌린다
export function resetAboutCurriculumMonths() {
  return request('/api/v1/admin/about-curriculum-months', { method: 'DELETE' })
}
