import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Button from '../components/common/Button'
import NewsNotice from '../components/news/NewsNotice'
import PostEditorForm from '../components/news/PostEditorForm'
import { canManagePost, canWritePost } from '../components/news/postPermission'
import { newsEditor } from '../data/newsContent'
import { newsBoard } from './boards'

// 게시물 작성·수정 (SCR-015, FR-008). 직원만 작성, 작성자·관리자만 수정 (FR-009).
// member가 undefined면 로그인 상태 확인 중. board: 수업 현장(newsBoard) 또는 몽키즈 연구소(labBoard)
function NewsEditorPage({ member, onLoginClick, board = newsBoard }) {
  const { getPost, createPost, updatePost, basePath } = board
  const { postId } = useParams()
  const navigate = useNavigate()
  const [result, setResult] = useState(null)

  useEffect(() => {
    if (!postId) return
    let ignore = false
    getPost(postId)
      .then((post) => {
        if (!ignore) setResult({ key: postId, post })
      })
      .catch((error) => {
        if (!ignore) setResult({ key: postId, error: error.message })
      })
    return () => {
      ignore = true
    }
  }, [postId, getPost])

  // 사진은 편집기에서 고를 때 이미 올라가 있고, 본문 HTML에 키로 들어 있다
  const handleSubmit = async (post) => {
    if (postId) {
      await updatePost(postId, post)
      navigate(`${basePath}/${postId}`, { replace: true })
    } else {
      const { id } = await createPost(post)
      navigate(`${basePath}/${id}`, { replace: true })
    }
  }

  const handleCancel = () => navigate(postId ? `${basePath}/${postId}` : basePath)

  const current = postId ? (result?.key === postId ? result : null) : { post: null }
  let notice = null
  if (member === undefined || !current) {
    notice = <NewsNotice message={newsEditor.loading} standalone />
  } else if (member === null) {
    notice = (
      <NewsNotice message={newsEditor.loginRequired} standalone>
        <Button variant="blue" onClick={onLoginClick}>
          {newsEditor.login}
        </Button>
      </NewsNotice>
    )
  } else if (!canWritePost(member)) {
    notice = <NewsNotice message={newsEditor.staffOnly} tone="error" standalone />
  } else if (current.error) {
    notice = <NewsNotice message={current.error} tone="error" standalone />
  } else if (current.post && !canManagePost(member, current.post)) {
    notice = <NewsNotice message={newsEditor.forbidden} tone="error" standalone />
  }

  if (notice) return notice
  return (
    <PostEditorForm
      key={postId ?? 'new'}
      post={current.post}
      onSubmit={handleSubmit}
      onCancel={handleCancel}
      categories={board.categories}
      notice={board.editorNotice}
      uploadImages={board.uploadImages}
    />
  )
}

export default NewsEditorPage
