// 근태 관리 수업 시작·종료 체크(EMP-SCR-001) API
import { request } from './client'
import { uploadArticleImages } from './posts'

// 백엔드 검증과 같은 수업 사진 제한 (EMP-FR-010: 1장 이상 20장 이하)
export const attendanceImageRules = {
  minCount: 1,
  maxCount: 20,
  maxSize: 10 * 1024 * 1024,
  types: ['image/jpeg', 'image/png', 'image/webp'],
}

function jsonBody(body) {
  return { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }
}

// 오늘 수업과 수업 시작이 반려된 채 남은 지난 수업 (EMP-FR-013). 시간순
export function getTodayClasses() {
  return request('/api/v1/attendance/classes/today')
}

// 위치로 수업 시작 확인 (EMP-FR-001). { confirmed, distanceMeters, radiusMeters, classSession }을 돌려준다.
// 반경 밖이어도 오류가 아니라 confirmed: false로 온다
export function startClassByLocation(classId, { latitude, longitude, accuracy }) {
  return request(`/api/v1/attendance/classes/${classId}/start`, {
    method: 'POST',
    ...jsonBody({ latitude, longitude, accuracy }),
  })
}

// 수업 시작 수동 등록 (EMP-FR-011). position은 위치를 받았을 때만 넘긴다
export function startClassManually(classId, reason, position) {
  return request(`/api/v1/attendance/classes/${classId}/start/manual`, {
    method: 'POST',
    ...jsonBody({ reason, ...position }),
  })
}

// 수업 시작 특이사항 (EMP-FR-002)
export function saveStartNote(classId, note) {
  return request(`/api/v1/attendance/classes/${classId}/start/note`, { method: 'PATCH', ...jsonBody({ note }) })
}

// 수업 사진은 attendance/ 아래에 올라간다
export function uploadAttendanceImages(files) {
  return uploadArticleImages('/api/v1/attendance/image-upload-urls', files)
}

// 수업 종료 제출 (EMP-FR-010). body: { note, imageKeys }
export function endClass(classId, body) {
  return request(`/api/v1/attendance/classes/${classId}/end`, { method: 'POST', ...jsonBody(body) })
}

// 수업 기록 정정 (EMP-FR-014). 사진·특이사항을 통째로 바꾼다
export function correctClassEnd(classId, body) {
  return request(`/api/v1/attendance/classes/${classId}/end`, { method: 'PUT', ...jsonBody(body) })
}
