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
