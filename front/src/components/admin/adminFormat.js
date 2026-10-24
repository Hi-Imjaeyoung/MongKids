// 관리자 화면 표시용 변환 함수
import { formatDate } from '../news/postFormat'

// 숫자만 저장된 전화번호에 하이픈을 넣는다. '01012345678' → '010-1234-5678', '0212345678' → '02-1234-5678'
export function formatPhone(phone) {
  if (!phone) return '-'
  const digits = phone.replace(/\D/g, '')
  const match = digits.startsWith('02')
    ? digits.match(/^(02)(\d{3,4})(\d{4})$/)
    : digits.match(/^(0\d{2})(\d{3,4})(\d{4})$/)
  return match ? match.slice(1).join('-') : phone
}

// '2026-10-24T09:12:33' → '2026.10.24 09:12'
export function formatDateTime(dateTime) {
  return dateTime ? `${formatDate(dateTime)} ${dateTime.slice(11, 16)}` : ''
}

// 목록의 짧은 접수일. '2026-10-24T09:12:33' → '10.24 09:12'
export function formatShortDateTime(dateTime) {
  return dateTime ? formatDateTime(dateTime).slice(5) : ''
}

// 'ALL'은 조건 없음으로 보낸다
export function toApiFilter(value) {
  return value === 'ALL' ? undefined : value
}
