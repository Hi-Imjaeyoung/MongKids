import { Link } from 'react-router-dom'
import ImagePlaceholder from '../common/ImagePlaceholder'
import CategoryChip from './CategoryChip'
import { formatDate } from './postFormat'
import { newsLike, newsList } from '../../data/newsContent'
import './PostCard.css'

// post: 목록 API 응답 항목 (PostSummaryResponse). 제목·작성자를, 본문 우측 상단에 공감 수(FR-011)를 보여 준다
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
        <div className="post-card__top">
          <CategoryChip category={post.category} />
          <span className="post-card__like">
            <span aria-hidden="true">♥</span> {newsLike.label} {post.likeCount}
          </span>
        </div>
        <strong className="post-card__title">{post.title}</strong>
        <span className="post-card__meta">
          {post.authorName} · {formatDate(post.createdAt)}
        </span>
      </div>
    </Link>
  )
}

export default PostCard
