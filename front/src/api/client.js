// 백엔드 공통 응답(ApiResponse<T>)을 처리하는 fetch 래퍼.
// 인증은 HttpOnly 쿠키로 하므로 같은 출처 요청에 쿠키를 함께 보낸다.

export class ApiError extends Error {
  constructor(status, error) {
    super(error?.message ?? '요청을 처리하지 못했습니다.')
    this.status = status
    this.code = error?.code
    this.fieldErrors = error?.fieldErrors ?? []
  }
}

export async function request(path, { headers, ...options } = {}) {
  const response = await fetch(path, {
    credentials: 'same-origin',
    headers: { Accept: 'application/json', ...headers },
    ...options,
  })
  const body = await response.json().catch(() => null)

  if (!response.ok || !body?.success) {
    throw new ApiError(response.status, body?.error)
  }
  return body.data
}
