import { Link } from 'react-router-dom'
import ImagePlaceholder from '../common/ImagePlaceholder'
import SectionHeading from '../common/SectionHeading'
import { sectionHeadings, services } from '../../data/homeContent'
import './ServiceCards.css'

function ServiceCards() {
  return (
    <section className="services container" id="programs" aria-labelledby="services-title">
      <SectionHeading {...sectionHeadings.services} titleId="services-title" />
      <div className="services__list">
        {services.map((service) => (
          <article key={service.id} className={`service-card service-card--${service.theme}`}>
            <div className="service-card__body">
              <span className="service-card__tag">{service.tag}</span>
              <h3 className="service-card__title">{service.title}</h3>
              <p className="service-card__description">{service.description}</p>
              <Link to={service.to} className="service-card__more">
                <span className="service-card__arrow" aria-hidden="true">›</span>
                자세히 보기
              </Link>
            </div>
            <div className="service-card__image">
              <ImagePlaceholder label={service.imageLabel} />
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default ServiceCards
