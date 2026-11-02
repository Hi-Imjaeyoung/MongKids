import { addDays } from '../../components/admin/attendanceAdminFormat'

// 서버 기록 조회 기간 제한 (최대 31일, AttendanceService.MAX_SEARCH_DAYS)
const MAX_SEARCH_DAYS = 31

export function isValidPeriod(from, to) {
  return Boolean(from && to) && from <= to && to <= addDays(from, MAX_SEARCH_DAYS)
}
