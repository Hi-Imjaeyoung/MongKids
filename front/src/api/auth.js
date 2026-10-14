// 소셜 로그인(FR-018) API
import { ApiError, request } from './client'

// 소셜 로그인은 페이지 이동으로 시작한다 (백엔드가 카카오·네이버 인가 페이지로 리다이렉트)
export function getSocialLoginUrl(provider) {
  return `/oauth2/authorization/${provider}`
}

// 로그인한 회원 정보. 로그인하지 않았으면 null
export async function fetchMyInfo() {
  try {
    return await request('/api/v1/members/me')
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      return null
    }
    throw error
  }
}

export function logout() {
  return request('/api/v1/auth/logout', { method: 'POST' })
}
