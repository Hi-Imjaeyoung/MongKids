import { Link } from 'react-router-dom'
import ImagePlaceholder from '../common/ImagePlaceholder'
import CategoryChip from '../news/CategoryChip'
import { formatDate } from '../news/postFormat'
import { labCategories, labList } from '../../data/labContent'
import './LabCard.css'

// labPost: 목록 API 응답 항목 (LabPostSummaryResponse). 모바일에서는 썸네일 + 제목 한 줄 목록이 된다
function LabCard({ labPost }) {
  return (
    <Link to={`/lab/${labPost.id}`} className="lab-card">
      <div className="lab-card__thumb">
        {labPost.thumbnailUrl ? (
          <img className="lab-card__image" src={labPost.thumbnailUrl} alt="" loading="lazy" />
        ) : (
          <ImagePlaceholder label={labList.imageLabel} />
        )}
      </div>
      <div className="lab-card__body">
        <CategoryChip category={labPost.category} categories={labCategories} />
        <strong className="lab-card__title">{labPost.title}</strong>
        <span className="lab-card__date">{formatDate(labPost.createdAt)}</span>
      </div>
    </Link>
  )
}

export default LabCard
