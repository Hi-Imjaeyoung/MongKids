import Button from '../common/Button'
import SectionHeading from '../common/SectionHeading'
import { gallery, sectionHeadings } from '../../data/homeContent'
import './ClassGallery.css'

function ClassGallery() {
  return (
    <section className="class-gallery container" aria-labelledby="gallery-title">
      <SectionHeading {...sectionHeadings.gallery} titleId="gallery-title" />
      <ul className="class-gallery__list">
        {gallery.items.map((item) => (
          <li key={item.id} className="class-gallery__item">
            <div className="class-gallery__image">
              <img src={item.image} alt={item.title} />
            </div>
            <p className="class-gallery__caption">
              <strong>{item.title}</strong>
            </p>
          </li>
        ))}
      </ul>
      <div className="section-actions">
        <Button variant="ghost" to={gallery.more.to}>
          {gallery.more.label} →
        </Button>
      </div>
    </section>
  )
}

export default ClassGallery
