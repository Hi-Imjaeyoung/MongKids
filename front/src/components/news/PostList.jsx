import { Link } from 'react-router-dom'
import Button from '../common/Button'
import NewsNotice from './NewsNotice'
import NewsTabs from './NewsTabs'
import PostCard from './PostCard'
import { newsList } from '../../data/newsContent'
import '../common/Button.css'
import './PostList.css'

// posts가 null이면 불러오는 중. canWrite: 글쓰기 버튼 노출 여부
function PostList({
  category,
  onCategoryChange,
  posts,
  total,
  hasNext,
  loadingMore,
  error,
  onLoadMore,
  canWrite,
}) {
  return (
    <section className="post-list container">
      <div className="post-list__toolbar">
        <NewsTabs active={category} onChange={onCategoryChange} />
        <div className="post-list__side">
          {posts && (
            <p className="post-list__total">
              {newsList.totalPrefix} <strong>{total}</strong>
              {newsList.totalSuffix}
            </p>
          )}
          {canWrite && (
            <Link to="/news/write" className="btn btn--blue btn--md">
              {newsList.write}
            </Link>
          )}
        </div>
      </div>

      {error && <NewsNotice message={error} tone="error" />}
      {!error && !posts && <NewsNotice message={newsList.loading} />}
      {posts?.length === 0 && <NewsNotice message={newsList.empty} />}

      {posts?.length > 0 && (
        <ul className="post-list__grid">
          {posts.map((post) => (
            <li key={post.id}>
              <PostCard post={post} />
            </li>
          ))}
        </ul>
      )}

      {hasNext && (
        <div className="post-list__more">
          <Button variant="yellow" size="lg" onClick={onLoadMore} disabled={loadingMore}>
            {newsList.loadMore}
          </Button>
        </div>
      )}
    </section>
  )
}

export default PostList
