// 몽키즈 연구소 글(FR-008, SCR-024) API. 사진은 게시물과 같은 방식으로 올리고 경로만 lab/로 나뉜다
import { request } from './client'
import { uploadArticleImages } from './posts'

function jsonBody(body) {
  return { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }
}

// category가 없으면 전체 탭. keyword는 제목 부분 일치 검색 (FR-024)
export function getLabPosts({ category, keyword, page = 0, size = 9 } = {}) {
  const params = new URLSearchParams({ page, size })
  if (category) params.set('category', category)
  if (keyword) params.set('keyword', keyword)
  return request(`/api/v1/lab-posts?${params}`)
}

export function getLabPost(labPostId) {
  return request(`/api/v1/lab-posts/${labPostId}`)
}

// labPost: { category, title, content }
export function createLabPost(labPost) {
  return request('/api/v1/lab-posts', { method: 'POST', ...jsonBody(labPost) })
}

export function updateLabPost(labPostId, labPost) {
  return request(`/api/v1/lab-posts/${labPostId}`, { method: 'PUT', ...jsonBody(labPost) })
}

export function deleteLabPost(labPostId) {
  return request(`/api/v1/lab-posts/${labPostId}`, { method: 'DELETE' })
}

// 연구소 사진은 lab/ 아래에 올라간다
export function uploadLabPostImages(files) {
  return uploadArticleImages('/api/v1/lab-posts/image-upload-urls', files)
}

// 공감(FR-011). 모두 { liked, likeCount }를 돌려준다. 비로그인 조회는 liked가 false
export function getLabPostLike(labPostId) {
  return request(`/api/v1/lab-posts/${labPostId}/likes`)
}

export function likeLabPost(labPostId) {
  return request(`/api/v1/lab-posts/${labPostId}/likes`, { method: 'POST' })
}

export function unlikeLabPost(labPostId) {
  return request(`/api/v1/lab-posts/${labPostId}/likes`, { method: 'DELETE' })
}
