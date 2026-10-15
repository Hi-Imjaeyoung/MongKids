import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deletePost, getPost } from '../api/posts'
import NewsNotice from '../components/news/NewsNotice'
import PostDetail from '../components/news/PostDetail'
import { newsDetail } from '../data/newsContent'

// 몽키즈 소식 상세 (SCR-006). 작성자에게만 수정·삭제를 보여 준다
function NewsDetailPage({ member }) {
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
  }, [postId])

  const current = result?.key === postId ? result : null

  const handleDelete = async () => {
    if (!window.confirm(newsDetail.deleteConfirm)) return
    setDeleting(true)
    try {
      await deletePost(postId)
      navigate('/news', { replace: true })
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
        <Link to="/news">{newsDetail.backToList}</Link>
      </NewsNotice>
    )
  }
  return (
    <PostDetail
      post={current.post}
      canEdit={member?.id === current.post.authorId}
      deleting={deleting}
      onDelete={handleDelete}
    />
  )
}

export default NewsDetailPage
