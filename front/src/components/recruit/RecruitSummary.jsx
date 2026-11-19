import { recruitSummary } from '../../data/recruitContent'
import './RecruitSummary.css'

// 강사 채용 모집 요강 (SCR-007)
function RecruitSummary() {
  return (
    <section className="recruit-summary recruit-container" aria-labelledby="recruit-summary-title">
      <h2 id="recruit-summary-title" className="recruit-section-title">{recruitSummary.title}</h2>
      <dl className="recruit-summary__list">
        {recruitSummary.items.map((item) => (
          <div key={item.term} className="recruit-summary__item">
            <dt className="recruit-summary__term">{item.term}</dt>
            <dd className="recruit-summary__description">{item.description}</dd>
          </div>
        ))}
      </dl>
    </section>
  )
}

export default RecruitSummary
