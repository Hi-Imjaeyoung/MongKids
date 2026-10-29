import Highlight from '../common/Highlight'
import heroImage from '../../assets/hero-main.jpg'
import { hero } from '../../data/homeContent'
import './Hero.css'

function Hero() {
  return (
    <section className="hero">
      <div className="hero__visual">
        <img className="hero__image" src={heroImage} alt={hero.visualAlt} />
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
          <p className="hero__area">
            {hero.area.map((line, index) => (
              <span key={line}>{index === 0 ? `📍 ${line}` : line}</span>
            ))}
          </p>
        </div>
      </div>

      <span className="hero__deco hero__deco--blob" aria-hidden="true" />
      <span className="hero__deco hero__deco--star" aria-hidden="true">★</span>
      <span className="hero__deco hero__deco--dot" aria-hidden="true" />
    </section>
  )
}

export default Hero
