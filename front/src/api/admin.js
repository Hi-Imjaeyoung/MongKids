// 관리자 화면(SCR-014) API. 모두 관리자(ADMIN)만 호출할 수 있다
import { request } from './client'

function jsonBody(body) {
  return { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }
}

// 값이 없는 조건은 빼고 쿼리 문자열을 만든다
function query(params) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value)
  })
  return search.toString()
}

// 상담 신청 (FR-004, FR-026). 최근 접수 순, type·status가 없으면 전체
export function getConsultations({ type, status, page = 0, size = 20 } = {}) {
  return request(`/api/v1/admin/consultations?${query({ type, status, page, size })}`)
}

export function getConsultation(consultationId) {
  return request(`/api/v1/admin/consultations/${consultationId}`)
}

// 처리 상태 변경. 바뀐 상세(ConsultationDetailResponse)를 돌려준다
export function changeConsultationStatus(consultationId, status) {
  return request(`/api/v1/admin/consultations/${consultationId}/status`, { method: 'PUT', ...jsonBody({ status }) })
}

// 신규 상담 건수 실시간 구독 (FR-005, SSE). 연결하면 바로 현재 건수가 오고, 바뀔 때마다 다시 온다.
// 끊기면 EventSource가 알아서 다시 연결한다. 돌려준 함수를 부르면 구독을 닫는다
export function subscribeNewConsultationCount(onCount) {
  const source = new EventSource('/api/v1/admin/consultations/new-count/stream')
  source.addEventListener('new-count', (event) => onCount(Number(event.data)))
  return () => source.close()
}

// 후기 (FR-021). 숨긴 후기도 포함한다. 항목은 { review, hidden, hiddenAt }
export function getAdminReviews({ type, hiddenOnly = false, page = 0, size = 20 } = {}) {
  return request(`/api/v1/admin/reviews?${query({ type, hiddenOnly, page, size })}`)
}

export function changeReviewHidden(reviewId, hidden) {
  return request(`/api/v1/admin/reviews/${reviewId}/hidden`, { method: 'PUT', ...jsonBody({ hidden }) })
}

// 회원·직원 권한 (FR-022). keyword는 이름 또는 전화번호
export function getMembers({ role, keyword, page = 0, size = 20 } = {}) {
  return request(`/api/v1/admin/members?${query({ role, keyword, page, size })}`)
}

// role: 'STAFF'(지정) | 'MEMBER'(해제)
export function changeMemberRole(memberId, role) {
  return request(`/api/v1/admin/members/${memberId}/role`, { method: 'PUT', ...jsonBody({ role }) })
}
