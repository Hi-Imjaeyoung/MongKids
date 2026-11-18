// 근태 관리 하루 수업 시작·종료 체크(EMP-SCR-001, EMP-FR-020) API
// date는 그날 'YYYY-MM-DD'. 오늘 하루 응답의 date를 그대로 넘겨 자정을 넘겨 눌러도 그날 기록으로 보낸다
import { request } from './client'
import { uploadArticleImages } from './posts'

// 백엔드 검증과 같은 수업 사진 제한 (EMP-FR-020: 1장 이상 20장 이하)
export const attendanceImageRules = {
  minCount: 1,
  maxCount: 20,
  maxSize: 10 * 1024 * 1024,
  types: ['image/jpeg', 'image/png', 'image/webp'],
}

function jsonBody(body) {
  return { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }
}

// 오늘 하루 (수업 시간순, 하루 시작·종료 기록). 수업이 없으면 classes가 빈 배열이다
export function getToday() {
  return request('/api/v1/attendance/days/today')
}

// 날짜별 내 하루 기록 (EMP-FR-015). from·to는 'YYYY-MM-DD', 기간은 최대 31일. 수업이 있는 날만 온다
export function getMyDays(from, to) {
  return request(`/api/v1/attendance/days?from=${from}&to=${to}`)
}

// 위치로 하루 시작 확인. { confirmed, distanceMeters, radiusMeters, institutionName, day }을 돌려준다.
// 반경 밖이어도 오류가 아니라 confirmed: false로 온다
export function startDayByLocation(date, { latitude, longitude, accuracy }) {
  return request(`/api/v1/attendance/days/${date}/start`, {
    method: 'POST',
    ...jsonBody({ latitude, longitude, accuracy }),
  })
}

// 하루 시작 수동 등록. position은 위치를 받았을 때만 넘긴다 → 하루
export function startDayManually(date, reason, position) {
  return request(`/api/v1/attendance/days/${date}/start/manual`, {
    method: 'POST',
    ...jsonBody({ reason, ...position }),
  })
}

// 하루 종료 화면에 들어가기 전 마지막 기관 반경 안인지 확인. 저장하지 않는다
export function checkDayEndLocation(date, { latitude, longitude, accuracy }) {
  return request(`/api/v1/attendance/days/${date}/end/location-checks`, {
    method: 'POST',
    ...jsonBody({ latitude, longitude, accuracy }),
  })
}

// 위치로 하루 종료 + 제출. body: { latitude, longitude, accuracy, imageKeys, notes: [{ institutionId, note }] }
// 반경 밖이면 confirmed: false로 오고 저장되지 않는다
export function endDayByLocation(date, body) {
  return request(`/api/v1/attendance/days/${date}/end`, { method: 'POST', ...jsonBody(body) })
}

// 하루 종료 수동 등록 + 제출. body: { reason, latitude?, longitude?, accuracy?, imageKeys, notes } → 하루
export function endDayManually(date, body) {
  return request(`/api/v1/attendance/days/${date}/end/manual`, { method: 'POST', ...jsonBody(body) })
}

// 하루 사진·기관별 특이사항 정정 (EMP-FR-014 규칙). body: { imageKeys, notes }. 통째로 바꾼다 → 하루
export function correctDayEnd(date, body) {
  return request(`/api/v1/attendance/days/${date}/end`, { method: 'PUT', ...jsonBody(body) })
}

// 수업 사진은 attendance/ 아래에 올라간다
export function uploadAttendanceImages(files) {
  return uploadArticleImages('/api/v1/attendance/image-upload-urls', files)
}
