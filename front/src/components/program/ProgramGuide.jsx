import ImagePlaceholder from '../common/ImagePlaceholder'
import SectionHeading from '../common/SectionHeading'
import { programDetail } from '../../data/programContent'
import './ProgramGuide.css'

// 프로그램 안내: 사진 자리 + 상세 01~03
function ProgramGuide({ program }) {
  return (
    <section className="program-guide container" aria-labelledby="program-guide-title">
      <SectionHeading {...programDetail.guide} titleId="program-guide-title" align="left" />
      <div className="program-guide__body">
        <div className="program-guide__photo">
          <ImagePlaceholder label={`${program.photoLabel} · ${programDetail.photoPending}`} />
        </div>
        <div>
          <h3 className="program-guide__title">{program.summary}</h3>
          <ol className="program-guide__list">
            {program.details.map((detail, index) => (
              <li key={detail} className="program-guide__item">
                <span className="program-guide__number">{String(index + 1).padStart(2, '0')}</span>
                <p className="program-guide__text">{detail}</p>
              </li>
            ))}
          </ol>
        </div>
      </div>
    </section>
  )
}

export default ProgramGuide
