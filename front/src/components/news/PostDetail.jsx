import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import CategoryChip from './CategoryChip'
import PostCardNews from './PostCardNews'
import PostContent from './PostContent'
import { parsePhotoOnly } from './photoOnlyContent'
import { formatDate } from './postFormat'
import { newsDetail } from '../../data/newsContent'
import './PostDetail.css'

// post: 상세 API 응답 (PostDetailResponse·LabPostDetailResponse). canEdit이면 수정·삭제 버튼을 보여 준다.
// basePath: 목록 주소 (수업 현장 /news, 연구소 /lab). categories: 분류 목록. notice: 본문 아래 안내 문구
// children: 본문 아래 영역 (수업 현장 댓글)
// 본문에 글 없이 사진만 있으면 본문 대신 카드뉴스로 넘겨 본다 (SCR-006, 2026-11-05)
function PostDetail({ post, canEdit, deleting, onDelete, basePath = '/news', categories, notice, children }) {
  const cardPhotos = useMemo(() => parsePhotoOnly(post.content), [post.content])

  return (
    <article className="post-detail container">
      <header className="post-detail__header">
        <CategoryChip category={post.category} categories={categories} />
        <h1 className="post-detail__title">{post.title}</h1>
        <p className="post-detail__meta">
          {post.authorName} · {formatDate(post.createdAt)}
        </p>
      </header>

      {cardPhotos ? <PostCardNews images={cardPhotos} /> : <PostContent html={post.content} />}

      {notice && <p className="post-detail__notice">{notice}</p>}

      {children}

      <footer className="post-detail__footer">
        <Link to={basePath} className="post-detail__button">
          {newsDetail.backToList}
        </Link>
        {canEdit && (
          <div className="post-detail__actions">
            <Link to={`${basePath}/${post.id}/edit`} className="post-detail__button">
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
