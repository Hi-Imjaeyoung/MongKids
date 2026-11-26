import { daysOfWeek, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatTime } from '../attendance/attendanceFormat'

const DAY_LABELS = Object.fromEntries(daysOfWeek.map((day) => [day.id, day.label]))
const DAY_ORDER = Object.fromEntries(daysOfWeek.map((day, order) => [day.id, order]))
// 사유 날짜를 몇 개까지 보여 줄지
const MAX_DATES = 5

// 정기 수업(EMP-SCR-007) 표시용 변환

// "해님어린이집 달님반"
export function seriesTitle(series) {
  return [series.institutionName, series.target].filter(Boolean).join(' ')
}

// ['FRIDAY', 'MONDAY'] → "월 · 금"
export function seriesDays(days) {
  return [...days].sort((a, b) => DAY_ORDER[a] - DAY_ORDER[b]).map((day) => DAY_LABELS[day]).join(' · ')
}

export function timeRange(item) {
  return `${formatTime(item.startTime)}–${formatTime(item.endTime)}`
}

// 사유 날짜: 앞 다섯 개만, 나머지는 "외 N회"
export function reasonDates(dates) {
  return { labels: dates.slice(0, MAX_DATES).map(shortDateLabel), rest: Math.max(0, dates.length - MAX_DATES) }
}
