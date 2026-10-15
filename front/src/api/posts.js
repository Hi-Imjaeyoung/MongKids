// 게시물(FR-008) API
import { request } from './client'

// 백엔드 검증과 같은 사진 제한
export const postImageRules = {
  maxCount: 20,
  maxSize: 10 * 1024 * 1024,
  types: ['image/jpeg', 'image/png', 'image/webp'],
}

function jsonBody(body) {
  return { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }
}

// category가 없으면 전체 탭
export function getPosts({ category, page = 0, size = 12 } = {}) {
  const params = new URLSearchParams({ page, size })
  if (category) params.set('category', category)
  return request(`/api/v1/posts?${params}`)
}

export function getPost(postId) {
  return request(`/api/v1/posts/${postId}`)
}

// post: { category, title, content, imageKeys }
export function createPost(post) {
  return request('/api/v1/posts', { method: 'POST', ...jsonBody(post) })
}

export function updatePost(postId, post) {
  return request(`/api/v1/posts/${postId}`, { method: 'PUT', ...jsonBody(post) })
}

export function deletePost(postId) {
  return request(`/api/v1/posts/${postId}`, { method: 'DELETE' })
}

// 사진을 S3에 직접 올리고 순서대로 객체 키를 돌려준다.
// 업로드 URL은 발급 때 정한 타입·크기로만 올릴 수 있다
export async function uploadPostImages(files) {
  if (files.length === 0) return []

  const uploads = await request('/api/v1/posts/image-upload-urls', {
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
