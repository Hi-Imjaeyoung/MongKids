// 상담 신청(FR-001, FR-002, FR-003) API. 로그인 없이 호출할 수 있다
import { request } from './client'

// consultation: { institutionName, preferredDays, preferredTime, childAgeGroups, childCount, phone, note, privacyAgreed }
export function createInstitutionConsultation(consultation) {
  return request('/api/v1/institution-consultations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(consultation),
  })
}

// consultation: { guardianName, phone, childName, childGender, childBirthDate, childAgeMonths, address, attendsDaycare,
//   temperament, favoritePlay, favoriteCharacter, healthNote, motivation, privacyAgreed, healthAgreed }
export function createPersonalConsultation(consultation) {
  return request('/api/v1/personal-consultations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(consultation),
  })
}

// 견적 신청 (SCR-011, FR-031). 백엔드는 이 형태로 구현 예정
// quote: { institutionName, scheduleUndecided, eventDate('YYYY-MM-DD'), eventTime('HH:mm'),
//   venueType(INDOOR|OUTDOOR|UNDECIDED), address, participantGroups[], childCount, guardianCount,
//   programs[], phone, note, privacyAgreed }. 일정 미정이면 날짜·시간, 장소 미정이면 주소, 적지 않은 인원은 null
export function createQuoteConsultation(quote) {
  return request('/api/v1/quote-consultations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(quote),
  })
}
