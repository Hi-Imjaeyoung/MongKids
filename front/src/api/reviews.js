// 수업 후기(FR-013~017, FR-025) API
import { request } from './client'

// 백엔드 검증과 같은 사진 제한 (FR-015)
export const reviewImageRules = {
  maxCount: 5,
  maxSize: 10 * 1024 * 1024,
  types: ['image/jpeg', 'image/png', 'image/webp'],
}

function jsonBody(body) {
  return { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }
}

// 최신순. type이 없으면 전체 탭. 비로그인은 page·size와 상관없이 최신 4개만 온다 (FR-012)
export function getReviews({ type, page = 0, size = 10 } = {}) {
  const params = new URLSearchParams({ page, size })
  if (type) params.set('type', type)
  return request(`/api/v1/reviews?${params}`)
}

// review: { type, satisfaction, content, imageKeys }. 저장된 후기(ReviewResponse)를 돌려준다
export function createReview(review) {
  return request('/api/v1/reviews', { method: 'POST', ...jsonBody(review) })
}

export function updateReview(reviewId, review) {
  return request(`/api/v1/reviews/${reviewId}`, { method: 'PUT', ...jsonBody(review) })
}

export function deleteReview(reviewId) {
  return request(`/api/v1/reviews/${reviewId}`, { method: 'DELETE' })
}

// 관리자 답글 저장·수정 (FR-017). 답글이 반영된 후기(ReviewResponse)를 돌려준다
export function saveReviewReply(reviewId, content) {
  return request(`/api/v1/reviews/${reviewId}/reply`, { method: 'PUT', ...jsonBody({ content }) })
}

// 사진을 S3에 직접 올리고 순서대로 객체 키를 돌려준다.
// 업로드 URL은 발급 때 정한 타입·크기로만 올릴 수 있다
export async function uploadReviewImages(files) {
  if (files.length === 0) return []

  const uploads = await request('/api/v1/reviews/image-upload-urls', {
    method: 'POST',
    ...jsonBody({ files: files.map((file) => ({ contentType: file.type, size: file.size })) }),
  })
  await Promise.all(
    uploads.map(async ({ uploadUrl }, index) => {
      const response = await fetch(uploadUrl, {
        method: 'PUT',
        headers: { 'Content-Type': files[index].type },
        body: files[index],
      })
      if (!response.ok) throw new Error('사진을 올리지 못했습니다.')
    }),
  )
  return uploads.map(({ imageKey }) => imageKey)
}
