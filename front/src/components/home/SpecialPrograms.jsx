import { Link } from 'react-router-dom'
import SectionHeading from '../common/SectionHeading'
import { sectionHeadings, specialPrograms } from '../../data/homeContent'
import './SpecialPrograms.css'

function SpecialPrograms() {
  return (
    <section className="special container" id="events" aria-labelledby="special-title">
      <SectionHeading {...sectionHeadings.special} titleId="special-title" />
      <div className="special__list">
        {specialPrograms.map((program) => (
          <article key={program.id} className="special-card">
            <span className="special-card__icon" aria-hidden="true">{program.icon}</span>
            <div className="special-card__body">
              <span className="special-card__tag">{program.tag}</span>
              <h3 className="special-card__title">{program.title}</h3>
              <p className="special-card__description">{program.description}</p>
              <Link to={program.to} className="special-card__more">
                자세히 보기 <span aria-hidden="true">→</span>
              </Link>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default SpecialPrograms
