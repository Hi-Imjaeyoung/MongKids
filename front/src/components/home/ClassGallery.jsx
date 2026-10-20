import Button from '../common/Button'
import ImagePlaceholder from '../common/ImagePlaceholder'
import SectionHeading from '../common/SectionHeading'
import { gallery, sectionHeadings } from '../../data/homeContent'
import './ClassGallery.css'

// 실제 사진은 게시 동의 확인 후 교체. 그 전에는 사실과 혼동되지 않도록 "등록 예정"을 표시한다.
function ClassGallery() {
  return (
    <section className="class-gallery container" aria-labelledby="gallery-title">
      <SectionHeading {...sectionHeadings.gallery} titleId="gallery-title" />
      <ul className="class-gallery__list">
        {gallery.items.map((item) => (
          <li key={item.id} className="class-gallery__item">
            <div className="class-gallery__image">
              <ImagePlaceholder label={item.title} />
            </div>
            <p className="class-gallery__caption">
              <strong>{item.title}</strong>
              <span>{gallery.pending}</span>
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
