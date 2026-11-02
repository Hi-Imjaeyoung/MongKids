import { useCallback, useEffect, useMemo, useState } from 'react'
import ClassHistory from '../components/attendance/ClassHistory'
import ClassRecordView from '../components/attendance/ClassRecordView'
import HistoryCalendar from '../components/attendance/HistoryCalendar'
import WeekStrip from '../components/attendance/WeekStrip'
import { addDays, summarizeByDate, toDateKey, weekStartOf } from '../components/attendance/attendanceFormat'
import useMyClasses from './useMyClasses'

// 날짜별 수업 기록 (EMP-SCR-001 ⑧~⑪, EMP-FR-015). 오늘 수업 화면(①)의 "내 수업 기록"에서 들어온다.
// 오늘은 오늘 수업 화면에서 보므로 오늘을 고르면 onGoToday로 돌아간다. 처음 고른 날은 어제
function AttendanceHistory({ today, onGoToday }) {
  const todayKey = toDateKey(today)
  const [selected, setSelected] = useState(() => addDays(today, -1))
  const [recordId, setRecordId] = useState(null)
  // calendar: 열린 달력의 { month: 그 달 1일, picked: 고른 날 키 } | null
  const [calendar, setCalendar] = useState(null)

  const weekStart = weekStartOf(selected)
  const week = useMyClasses(toDateKey(weekStart), toDateKey(addDays(weekStart, 6)))
  const monthStart = calendar?.month ?? weekStart
  const monthEnd = new Date(monthStart.getFullYear(), monthStart.getMonth() + 1, 0)
  const month = useMyClasses(toDateKey(monthStart), toDateKey(monthEnd), calendar != null)

  const weekSummary = useMemo(() => summarizeByDate(week.classes), [week.classes])
  const monthSummary = useMemo(() => summarizeByDate(month.classes), [month.classes])

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [recordId])

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
  const dayClasses = week.classes?.filter((classSession) => classSession.classDate === selectedKey) ?? null
  const record = recordId != null ? week.classes?.find((classSession) => classSession.id === recordId) : null

  if (record) return <ClassRecordView classSession={record} onBack={() => setRecordId(null)} />

  const shiftMonth = (months) =>
    setCalendar((current) => ({
      ...current,
      month: new Date(current.month.getFullYear(), current.month.getMonth() + months, 1),
    }))

  return (
    <>
      <ClassHistory
        date={selected}
        classes={dayClasses}
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
        onView={(classSession) => setRecordId(classSession.id)}
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
