import { Link } from 'react-router-dom'
import ImagePlaceholder from '../common/ImagePlaceholder'
import CategoryChip from './CategoryChip'
import { formatDate } from './postFormat'
import { newsList } from '../../data/newsContent'
import './PostCard.css'

// post: 목록 API 응답 항목 (PostSummaryResponse)
function PostCard({ post }) {
  return (
    <Link to={`/news/${post.id}`} className="post-card">
      <div className="post-card__thumb">
        {post.thumbnailUrl ? (
          <img className="post-card__image" src={post.thumbnailUrl} alt="" loading="lazy" />
        ) : (
          <ImagePlaceholder label={newsList.imageLabel} />
        )}
      </div>
      <div className="post-card__body">
        <CategoryChip category={post.category} />
        <strong className="post-card__title">{post.title}</strong>
        <span className="post-card__meta">
          {post.authorName} · {formatDate(post.createdAt)}
        </span>
      </div>
    </Link>
  )
}

export default PostCard
