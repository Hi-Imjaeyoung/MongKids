import './WorkCalendarFootnotes.css'

// 근무 캘린더 아래 숫자 기준 설명. notes: 문구 배열
function WorkCalendarFootnotes({ notes }) {
  return (
    <ul className="work-calendar-footnotes">
      {notes.map((note) => (
        <li key={note}>{note}</li>
      ))}
    </ul>
  )
}

export default WorkCalendarFootnotes
