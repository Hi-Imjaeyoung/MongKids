import ClassStatusBadge from './ClassStatusBadge'
import { formatClassMeta } from './attendanceFormat'
import './ClassSummary.css'

// 하위 화면 위쪽의 수업 요약 상자. label이 있으면 기관명 위에, children은 아래 줄에 붙인다
function ClassSummary({ classSession, label, showStatus = false, children }) {
  return (
    <section className="class-summary">
      {label && <span className="class-summary__label">{label}</span>}
      <div className="class-summary__head">
        <strong className="class-summary__name">{classSession.institutionName}</strong>
        {showStatus && <ClassStatusBadge status={classSession.status} />}
      </div>
      <span className="class-summary__meta">{formatClassMeta(classSession)}</span>
      {children}
    </section>
  )
}

export default ClassSummary
