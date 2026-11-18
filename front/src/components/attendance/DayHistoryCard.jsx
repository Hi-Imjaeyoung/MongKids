import { checkLine, classHistory } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import ClassStatusBadge from './ClassStatusBadge'
import { formatCheck, formatClassMeta, groupByInstitution } from './attendanceFormat'
import './ClassCard.css'

function recordLines(day) {
  const { start, end } = day
  const lines = []
  if (start) {
    const parts = [formatCheck(start, 'start')]
    if (end) parts.push(formatCheck(end, 'end'), checkLine.photoCount(day.images.length))
    lines.push(parts.join(' · '))
  }
  const rejected = [start, end].find((check) => check?.status === 'REJECTED')
  if (rejected) lines.push(checkLine.rejectReason(rejected.rejectReason))
  else {
    const firstNote = day.notes.find((note) => note.note)
    if (firstNote) lines.push(classHistory.note(`${firstNote.institutionName} ${firstNote.note.split('\n')[0]}`))
  }
  return lines
}

// 날짜별 수업 기록의 하루 카드 (EMP-SCR-001 ⑧·⑩, EMP-FR-020). 기관별 수업과 하루 시작·종료 기록을 보여 준다.
// 앞으로 날짜 → 버튼 없이 `예정`, 기록이 있으면(반려 포함) → 기록 보기(읽기 전용)
function DayHistoryCard({ day, future, onView }) {
  const recorded = day.start || day.end
  let badge = <ClassStatusBadge status={day.status} day />
  if (future) badge = <span className="class-status class-status--blue">{classHistory.scheduled}</span>
  else if (!recorded) badge = <span className="class-status class-status--plain">{classHistory.noRecord}</span>
  const rejected = !future && (day.status === 'REJECTED' || day.status === 'END_REJECTED')

  return (
    <article className={`class-card ${rejected ? 'class-card--rejected' : ''}`}>
      {groupByInstitution(day.classes).map((group, index) => (
        <div key={`${group.institutionId}-${group.classes[0].id}`} className="class-card__group">
          <div className="class-card__head">
            <h3 className="class-card__name">{group.institutionName}</h3>
            {index === 0 && badge}
          </div>
          <div className="class-card__details">
            {group.classes.map((dayClass) => (
              <p key={dayClass.id} className="class-card__detail">
                {formatClassMeta(dayClass)}
              </p>
            ))}
          </div>
        </div>
      ))}
      {!future && recorded && (
        <div className="class-card__details class-card__details--record">
          {recordLines(day).map((line) => (
            <p key={line} className="class-card__detail class-card__detail--clamp">
              {line}
            </p>
          ))}
        </div>
      )}
      {!future && recorded && (
        <AttendanceAction variant="outline" size="md" onClick={onView}>
          {classHistory.viewRecord}
        </AttendanceAction>
      )}
    </article>
  )
}

export default DayHistoryCard
