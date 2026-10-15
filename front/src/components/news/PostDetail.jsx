import { Link } from 'react-router-dom'
import CategoryChip from './CategoryChip'
import PostGallery from './PostGallery'
import { formatDate } from './postFormat'
import { newsDetail } from '../../data/newsContent'
import './PostDetail.css'

// post: 상세 API 응답 (PostDetailResponse). canEdit이면 수정·삭제 버튼을 보여 준다
function PostDetail({ post, canEdit, deleting, onDelete }) {
  return (
    <article className="post-detail container">
      <header className="post-detail__header">
        <CategoryChip category={post.category} />
        <h1 className="post-detail__title">{post.title}</h1>
        <p className="post-detail__meta">
          {post.authorName} · {formatDate(post.createdAt)}
        </p>
      </header>

      <PostGallery images={post.images} />

      <p className="post-detail__content">{post.content}</p>

      <footer className="post-detail__footer">
        <Link to="/news" className="post-detail__button">
          {newsDetail.backToList}
        </Link>
        {canEdit && (
          <div className="post-detail__actions">
            <Link to={`/news/${post.id}/edit`} className="post-detail__button">
              {newsDetail.edit}
            </Link>
            <button
              type="button"
              className="post-detail__button post-detail__button--danger"
              onClick={onDelete}
              disabled={deleting}
            >
              {newsDetail.delete}
            </button>
          </div>
        )}
      </footer>
    </article>
  )
}

export default PostDetail
