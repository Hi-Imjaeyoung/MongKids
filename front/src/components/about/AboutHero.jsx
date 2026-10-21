import ImagePlaceholder from '../common/ImagePlaceholder'
import { aboutHero } from '../../data/aboutContent'
import './AboutHero.css'

function AboutHero() {
  return (
    <section className="about-hero" aria-labelledby="about-title">
      <div className="about-hero__inner container">
        <div>
          <p className="about-hero__eyebrow">{aboutHero.eyebrow}</p>
          <h1 id="about-title" className="about-hero__title">
            {aboutHero.title}
          </h1>
          <p className="about-hero__description">
            {aboutHero.descriptionLines.map((line) => (
              <span key={line} className="about-hero__line">
                {line}
              </span>
            ))}
          </p>
          <p className="about-hero__area">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M12 22s7-6.2 7-12a7 7 0 0 0-14 0c0 5.8 7 12 7 12z" />
              <circle cx="12" cy="10" r="2.5" />
            </svg>
            {aboutHero.area}
          </p>
        </div>
        <div className="about-hero__photo">
          <ImagePlaceholder label={aboutHero.photoLabel} />
        </div>
      </div>
    </section>
  )
}

export default AboutHero
