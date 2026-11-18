import { useCallback, useEffect, useMemo, useState } from 'react'
import ClassHistory from '../components/attendance/ClassHistory'
import ClassRecordView from '../components/attendance/ClassRecordView'
import DayRecordView from '../components/attendance/DayRecordView'
import HistoryCalendar from '../components/attendance/HistoryCalendar'
import WeekStrip from '../components/attendance/WeekStrip'
import { addDays, summarizeByDate, toDateKey, weekStartOf } from '../components/attendance/attendanceFormat'
import useMyDays from './useMyDays'

// 날짜별 수업 기록 (EMP-SCR-001 ⑧~⑪, EMP-FR-015). 오늘 하루 화면(①)의 "내 수업 기록"에서 들어온다.
// 오늘은 오늘 하루 화면에서 보므로 오늘을 고르면 onGoToday로 돌아간다. 처음 고른 날은 어제
function AttendanceHistory({ today, onGoToday }) {
  const todayKey = toDateKey(today)
  const [selected, setSelected] = useState(() => addDays(today, -1))
  // record: 보고 있는 기록. { legacyId: 수업 id }면 하루 단위 적용 전 수업 기록, {}면 그날 하루 기록, null이면 목록
  const [record, setRecord] = useState(null)
  // calendar: 열린 달력의 { month: 그 달 1일, picked: 고른 날 키 } | null
  const [calendar, setCalendar] = useState(null)

  const weekStart = weekStartOf(selected)
  const week = useMyDays(toDateKey(weekStart), toDateKey(addDays(weekStart, 6)))
  const monthStart = calendar?.month ?? weekStart
  const monthEnd = new Date(monthStart.getFullYear(), monthStart.getMonth() + 1, 0)
  const month = useMyDays(toDateKey(monthStart), toDateKey(monthEnd), calendar != null)

  const weekSummary = useMemo(() => summarizeByDate(week.days), [week.days])
  const monthSummary = useMemo(() => summarizeByDate(month.days), [month.days])

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [record])

  const select = useCallback(
    (date) => {
      if (toDateKey(date) === todayKey) {
        onGoToday()
        return
      }
      setSelected(date)
      setCalendar(null)
    },
    [todayKey, onGoToday],
  )
  const closeCalendar = useCallback(() => setCalendar(null), [])

  const selectedKey = toDateKey(selected)
  // 불러오는 중이면 undefined, 수업이 없는 날이면 null
  const day = week.days ? (week.days.find((item) => item.date === selectedKey) ?? null) : undefined

  if (record && day) {
    const legacy = record.legacyId != null ? day.legacyClasses.find((item) => item.id === record.legacyId) : null
    if (legacy) return <ClassRecordView classSession={legacy} onBack={() => setRecord(null)} />
    return <DayRecordView day={day} onBack={() => setRecord(null)} />
  }

  const shiftMonth = (months) =>
    setCalendar((current) => ({
      ...current,
      month: new Date(current.month.getFullYear(), current.month.getMonth() + months, 1),
    }))

  return (
    <>
      <ClassHistory
        date={selected}
        day={day}
        error={week.error}
        future={selectedKey > todayKey}
        strip={
          <WeekStrip
            weekStart={weekStart}
            selectedKey={selectedKey}
            todayKey={todayKey}
            summary={weekSummary}
            onSelect={select}
            onPrevWeek={() => setSelected(addDays(selected, -7))}
            onNextWeek={() => setSelected(addDays(selected, 7))}
            onToday={onGoToday}
          />
        }
        onBack={onGoToday}
        onCalendar={() =>
          setCalendar({ month: new Date(selected.getFullYear(), selected.getMonth(), 1), picked: selectedKey })
        }
        onReload={week.reload}
        onViewDay={() => setRecord({})}
        onViewLegacy={(classSession) => setRecord({ legacyId: classSession.id })}
      />
      {calendar && (
        <HistoryCalendar
          month={calendar.month}
          pickedKey={calendar.picked}
          todayKey={todayKey}
          summary={monthSummary}
          error={month.error}
          onPrevMonth={() => shiftMonth(-1)}
          onNextMonth={() => shiftMonth(1)}
          onPick={(date) => setCalendar((current) => ({ ...current, picked: toDateKey(date) }))}
          onApply={select}
          onGoToday={onGoToday}
          onClose={closeCalendar}
        />
      )}
    </>
  )
}

export default AttendanceHistory
