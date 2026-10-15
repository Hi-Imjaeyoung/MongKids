import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createPost, getPost, updatePost, uploadPostImages } from '../api/posts'
import Button from '../components/common/Button'
import NewsNotice from '../components/news/NewsNotice'
import PostEditorForm from '../components/news/PostEditorForm'
import { newsEditor } from '../data/newsContent'

// 새로 고른 사진만 올리고, 기존 사진 키와 합쳐 표시 순서대로 돌려준다
async function resolveImageKeys(images) {
  const uploadedKeys = await uploadPostImages(images.filter((image) => image.file).map((image) => image.file))
  let uploadIndex = 0
  return images.map((image) => image.imageKey ?? uploadedKeys[uploadIndex++])
}

// 몽키즈 소식 작성·수정 (SCR-006, FR-008). member가 undefined면 로그인 상태 확인 중
function NewsEditorPage({ member, onLoginClick }) {
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
  }, [postId])

  const handleSubmit = async ({ category, title, content, images }) => {
    const post = { category, title, content, imageKeys: await resolveImageKeys(images) }
    if (postId) {
      await updatePost(postId, post)
      navigate(`/news/${postId}`, { replace: true })
    } else {
      const { id } = await createPost(post)
      navigate(`/news/${id}`, { replace: true })
    }
  }

  const handleCancel = () => navigate(postId ? `/news/${postId}` : '/news')

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
  } else if (current.error) {
    notice = <NewsNotice message={current.error} tone="error" standalone />
  } else if (current.post && current.post.authorId !== member.id) {
    notice = <NewsNotice message={newsEditor.forbidden} tone="error" standalone />
  }

  if (notice) return notice
  return (
    <PostEditorForm key={postId ?? 'new'} post={current.post} onSubmit={handleSubmit} onCancel={handleCancel} />
  )
}

export default NewsEditorPage
