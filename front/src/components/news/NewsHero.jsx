import Highlight from '../common/Highlight'
import { newsHero } from '../../data/newsContent'
import './NewsHero.css'

function NewsHero() {
  return (
    <section className="news-hero">
      <div className="news-hero__inner container">
        <p className="news-hero__eyebrow">{newsHero.eyebrow}</p>
        <h1 className="news-hero__title">
          <Highlight text={newsHero.title} word={newsHero.highlight} as="em" className="news-hero__highlight" />
        </h1>
        <p className="news-hero__description">{newsHero.description}</p>
      </div>
    </section>
  )
}

export default NewsHero
