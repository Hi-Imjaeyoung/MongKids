import { useCallback, useState } from 'react'
import { approveDay, getAdminDays, getWorkCalendar, registerDay, rejectDay } from '../../api/admin'
import AdminModal from '../../components/admin/AdminModal'
import AdminNotice from '../../components/admin/AdminNotice'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSection from '../../components/admin/AdminSection'
import DayDetail from '../../components/admin/DayDetail'
import DirectStartDialog from '../../components/admin/DirectStartDialog'
import WorkCalendarFootnotes from '../../components/admin/WorkCalendarFootnotes'
import WorkCalendarLegend from '../../components/admin/WorkCalendarLegend'
import WorkCalendarNav from '../../components/admin/WorkCalendarNav'
import WorkCalendarTotals from '../../components/admin/WorkCalendarTotals'
import WorkMonthCalendar from '../../components/admin/WorkMonthCalendar'
import WorkWeekTable from '../../components/admin/WorkWeekTable'
import {
  addDays,
  addMonths,
  endOfMonth,
  startOfMonth,
  startOfWeek,
  toIsoDate,
} from '../../components/admin/attendanceAdminFormat'
import { formatRoute } from '../../components/attendance/attendanceFormat'
import { adminPage } from '../../data/adminContent'
import { dayRecordAdmin, workCalendarAdmin } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'
import useAttendanceOptions from './useAttendanceOptions'

function weekDates(weekStart) {
  return Array.from({ length: 7 }, (_, index) => addDays(weekStart, index))
}

// 그 달이 든 주(월~일)를 모두. 앞뒤 달 날짜도 칸은 채운다
function monthWeeks(month) {
  const last = endOfMonth(month)
  const weeks = []
  for (let weekStart = startOfWeek(month); weekStart <= last; weekStart = addDays(weekStart, 7)) {
    weeks.push(weekDates(weekStart))
  }
  return weeks
}

// 근무 캘린더 (EMP-SCR-006 ③⑤, EMP-FR-017). 주간 표(직원 × 7일)와 월간 달력·직원별 합계. 근무 시간은 계산하지 않는다.
// 주간 칸을 누르면 하루 근무 상세(④) 모달에서 승인·반려·직접 등록하고, 월간 날짜를 누르면 그 주 주간 보기로 간다
function WorkCalendarTab() {
  const today = toIsoDate(new Date())
  const { staffs } = useAttendanceOptions()
  const [view, setView] = useState('week')
  // anchor: 보고 있는 기간 안의 아무 날짜
  const [anchor, setAnchor] = useState(today)
  const [staffId, setStaffId] = useState(null)
  const weekStart = startOfWeek(anchor)
  const month = startOfMonth(anchor)
  const period = view === 'week' ? { from: weekStart, to: addDays(weekStart, 6) } : { from: month, to: endOfMonth(month) }
  const calendar = useAdminQuery(getWorkCalendar, { ...period, staffId })

  // opened: 상세를 연 칸 { staffId, date }. registering: 직접 등록할 'start' | 'end'
  const [opened, setOpened] = useState(null)
  const detail = useAdminQuery(getAdminDays, opened ? { from: opened.date, to: opened.date, staffId: opened.staffId } : null)
  const selected = detail.loading ? null : detail.data?.[0]
  const [registering, setRegistering] = useState(null)
  const [busy, setBusy] = useState(false)
  const closeDetail = useCallback(() => setOpened(null), [setOpened])
  const closeRegister = useCallback(() => setRegistering(null), [setRegistering])

  const move = (step) => {
    setAnchor(view === 'week' ? addDays(weekStart, step * 7) : addMonths(month, step))
  }

  const changeView = (next) => {
    setView(next)
    // 이번 달을 보다가 주로 바꾸면 오늘이 든 주, 다른 달이면 그 달 첫 주
    if (next === 'week' && month === startOfMonth(today)) setAnchor(today)
  }

  const showWeek = (date) => {
    setView('week')
    setAnchor(date)
  }

  const process = async (task) => {
    setBusy(true)
    try {
      const updated = await task()
      detail.setData(() => [updated])
      setRegistering(null)
      calendar.reload()
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusy(false)
    }
  }

  const label = view === 'week' ? workCalendarAdmin.weekRange(period.from, period.to) : workCalendarAdmin.monthLabel(month)

  return (
    <AdminSection
      title={workCalendarAdmin.title}
      description={workCalendarAdmin.descriptions[view]}
      toolbar={
        <WorkCalendarNav
          view={view}
          label={label}
          staffs={staffs}
          staffId={staffId}
          onViewChange={changeView}
          onMove={move}
          onCurrent={() => setAnchor(today)}
          onStaffChange={setStaffId}
        />
      }
    >
      <WorkCalendarLegend />
      <AdminQueryBody query={calendar} items={calendar.data?.staffs} empty={workCalendarAdmin.empty}>
        {(rows) =>
          view === 'week' ? (
            <WorkWeekTable
              dates={weekDates(weekStart)}
              staffs={rows}
              today={today}
              onSelect={(id, date) => setOpened({ staffId: id, date })}
            />
          ) : (
            <>
              <WorkMonthCalendar weeks={monthWeeks(month)} month={month} staffs={rows} today={today} onSelectDate={showWeek} />
              <WorkCalendarTotals title={workCalendarAdmin.totalsTitle(month)} staffs={rows} />
            </>
          )
        }
      </AdminQueryBody>
      <WorkCalendarFootnotes notes={workCalendarAdmin.footnotes[view]} />
      {opened && !registering && (
        <AdminModal label={workCalendarAdmin.detailLabel} closeLabel={workCalendarAdmin.close} onClose={closeDetail}>
          {selected ? (
            <DayDetail
              key={`${opened.staffId}-${opened.date}-${selected.day.status}`}
              item={selected}
              busy={busy}
              onApprove={(type) => process(() => approveDay(selected.staffId, selected.day.date, type))}
              onReject={(type, reason) => process(() => rejectDay(selected.staffId, selected.day.date, type, reason))}
              onRegister={setRegistering}
            />
          ) : (
            <AdminNotice inline error={Boolean(detail.error)} message={detail.error?.message ?? adminPage.loading} />
          )}
        </AdminModal>
      )}
      {selected && registering && (
        <DirectStartDialog
          type={registering}
          item={selected}
          summary={dayRecordAdmin.route(formatRoute(selected.day), selected.day.classes.length)}
          busy={busy}
          onSubmit={(reason) => process(() => registerDay(selected.staffId, selected.day.date, registering, reason))}
          onClose={closeRegister}
        />
      )}
    </AdminSection>
  )
}

export default WorkCalendarTab
