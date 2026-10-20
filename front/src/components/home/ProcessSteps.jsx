import SectionHeading from '../common/SectionHeading'
import { processSteps, sectionHeadings } from '../../data/homeContent'
import './ProcessSteps.css'

function ProcessSteps() {
  return (
    <section className="process container" aria-labelledby="process-title">
      <SectionHeading {...sectionHeadings.process} titleId="process-title" />
      <ol className="process__list">
        {processSteps.steps.map((step, index) => (
          <li key={step.title} className="process__item">
            <span className={`process__icon process__icon--${step.theme}`} aria-hidden="true">
              {step.icon}
            </span>
            <span className="process__step">STEP {String(index + 1).padStart(2, '0')}</span>
            <strong className="process__title">{step.title}</strong>
            <span className="process__description">{step.description}</span>
          </li>
        ))}
      </ol>
      <p className="process__note">{processSteps.note}</p>
    </section>
  )
}

export default ProcessSteps
