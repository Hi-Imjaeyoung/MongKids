import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import NewsNotice from '../components/news/NewsNotice'
import PostDetail from '../components/news/PostDetail'
import { canManagePost } from '../components/news/postPermission'
import { newsDetail } from '../data/newsContent'
import { newsBoard } from './boards'

// 게시물 상세 (SCR-006). 작성자에게만 수정·삭제를 보여 준다.
// board: 수업 현장(newsBoard) 또는 몽키즈 연구소(labBoard, SCR-024에서 재사용)
function NewsDetailPage({ member, board = newsBoard }) {
  const { getPost, deletePost, basePath } = board
  const { postId } = useParams()
  const navigate = useNavigate()
  const [result, setResult] = useState(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    let ignore = false
    getPost(postId)
      .then((post) => {
        if (!ignore) setResult({ key: postId, post })
      })
      .catch((error) => {
        if (!ignore) setResult({ key: postId, error: error.status === 404 ? newsDetail.notFound : error.message })
      })
    return () => {
      ignore = true
    }
  }, [postId, getPost])

  const current = result?.key === postId ? result : null

  const handleDelete = async () => {
    if (!window.confirm(newsDetail.deleteConfirm)) return
    setDeleting(true)
    try {
      await deletePost(postId)
      navigate(basePath, { replace: true })
    } catch (error) {
      window.alert(error.message)
      setDeleting(false)
    }
  }

  if (!current) {
    return <NewsNotice message={newsDetail.loading} standalone />
  }
  if (current.error) {
    return (
      <NewsNotice message={current.error} tone="error" standalone>
        <Link to={basePath}>{newsDetail.backToList}</Link>
      </NewsNotice>
    )
  }
  return (
    <PostDetail
      post={current.post}
      canEdit={canManagePost(member, current.post)}
      deleting={deleting}
      onDelete={handleDelete}
      basePath={basePath}
      categories={board.categories}
      notice={board.detailNotice}
    />
  )
}

export default NewsDetailPage
