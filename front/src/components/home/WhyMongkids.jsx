import Highlight from '../common/Highlight'
import SectionHeading from '../common/SectionHeading'
import { reasons, sectionHeadings } from '../../data/homeContent'
import './WhyMongkids.css'

function WhyMongkids() {
  return (
    <section className="why container" id="about">
      <SectionHeading {...sectionHeadings.why} />

      <ul className="why__list">
        {reasons.map((reason) => (
          <li key={reason.title} className="why__item">
            <span className={`why__icon why__icon--${reason.theme}`} aria-hidden="true">
              {reason.icon}
            </span>
            <h3 className="why__title">{reason.title}</h3>
            <p className="why__description">
              <Highlight text={reason.description} word={reason.highlight} />
            </p>
          </li>
        ))}
      </ul>
    </section>
  )
}

export default WhyMongkids
