// 강사 지원서 접수 (SCR-007, FR-007). 로그인 없이 호출할 수 있다. 백엔드는 이 형태로 구현 예정
// multipart/form-data: file(지원서 파일 1개), privacyAgreed('true')
import { request } from './client'

export function submitTeacherApplication(file, privacyAgreed) {
  const body = new FormData()
  body.append('file', file)
  body.append('privacyAgreed', String(privacyAgreed))
  // Content-Type은 브라우저가 boundary와 함께 정한다
  return request('/api/v1/teacher-applications', { method: 'POST', body })
}
