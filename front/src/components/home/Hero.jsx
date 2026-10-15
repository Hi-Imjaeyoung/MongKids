import Button from '../common/Button'
import Highlight from '../common/Highlight'
import ImagePlaceholder from '../common/ImagePlaceholder'
import { ctaButtons, hero } from '../../data/homeContent'
import './Hero.css'

function Hero({ onInstitutionConsultClick, onPersonalConsultClick }) {
  return (
    <section className="hero">
      <div className="hero__visual">
        <ImagePlaceholder label="메인 비주얼 (수업 사진)" />
      </div>

      <div className="hero__inner container">
        <div className="hero__content">
          <h1 className="hero__title">
            {hero.titleLines.map((line) => (
              <span key={line} className="hero__title-line">
                <Highlight text={line} word={hero.highlight} as="em" className="hero__highlight" />
              </span>
            ))}
          </h1>
          <p className="hero__description">
            {hero.description.map((line) => (
              <span key={line}>{line}</span>
            ))}
          </p>
          <div className="hero__actions">
            <Button variant="yellow" size="lg" icon={ctaButtons.institution.icon} onClick={onInstitutionConsultClick}>
              {ctaButtons.institution.label}
            </Button>
            <Button variant="blue" size="lg" icon={ctaButtons.private.icon} onClick={onPersonalConsultClick}>
              {ctaButtons.private.label}
            </Button>
          </div>
        </div>
      </div>

      <span className="hero__deco hero__deco--blob" aria-hidden="true" />
      <span className="hero__deco hero__deco--star" aria-hidden="true">★</span>
      <span className="hero__deco hero__deco--dot" aria-hidden="true" />
    </section>
  )
}

export default Hero
