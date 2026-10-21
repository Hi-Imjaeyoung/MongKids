import { useEffect, useState } from 'react'
import { createPostComment, deletePostComment, getPostComments } from '../api/posts'

const PAGE_SIZE = 20

// 서버 목록에 없는 댓글만 이어 붙인다
function appendNew(list, items) {
  const ids = new Set(list.map((comment) => comment.id))
  return [...list, ...items.filter((comment) => !ids.has(comment.id))]
}

// 게시물 댓글 상태 (FR-010). 목록은 오래된 순으로 PAGE_SIZE씩 이어서 불러온다.
// loaded: 서버 목록 앞에서부터 불러온 댓글. 다음 페이지 번호를 이 개수로 계산하므로 작성·삭제 뒤에도 빠지는 댓글이 없다
// added: 아직 불러오지 않은 뒤 페이지가 있을 때 새로 쓴 댓글. 끝까지 불러오기 전까지 목록 맨 아래에 따로 보여 준다
function usePostComments(postId, enabled) {
  const [result, setResult] = useState(null)
  const [loadingMore, setLoadingMore] = useState(false)

  useEffect(() => {
    if (!enabled) return undefined
    let ignore = false
    getPostComments(postId, { size: PAGE_SIZE })
      .then((page) => {
        if (!ignore) {
          setResult({ key: postId, loaded: page.content, added: [], total: page.totalElements, hasNext: page.hasNext })
        }
      })
      .catch((error) => {
        if (!ignore) setResult({ key: postId, error: error.message })
      })
    return () => {
      ignore = true
    }
  }, [postId, enabled])

  const current = result?.key === postId ? result : null

  const loadMore = async () => {
    setLoadingMore(true)
    try {
      const page = await getPostComments(postId, {
        page: Math.floor(current.loaded.length / PAGE_SIZE),
        size: PAGE_SIZE,
      })
      setResult((prev) => {
        const loaded = appendNew(prev.loaded, page.content)
        const loadedIds = new Set(loaded.map((comment) => comment.id))
        return {
          ...prev,
          loaded,
          added: prev.added.filter((comment) => !loadedIds.has(comment.id)),
          total: page.totalElements,
          hasNext: page.hasNext,
        }
      })
    } catch (error) {
      window.alert(error.message)
    } finally {
      setLoadingMore(false)
    }
  }

  // 실패하면 예외를 그대로 던져 입력창이 오류를 보여 준다
  const addComment = async (content) => {
    const comment = await createPostComment(postId, content)
    setResult((prev) => ({
      ...prev,
      loaded: prev.hasNext ? prev.loaded : [...prev.loaded, comment],
      added: prev.hasNext ? [...prev.added, comment] : prev.added,
      total: prev.total + 1,
    }))
  }

  const removeComment = async (commentId) => {
    await deletePostComment(postId, commentId)
    setResult((prev) => ({
      ...prev,
      loaded: prev.loaded.filter((comment) => comment.id !== commentId),
      added: prev.added.filter((comment) => comment.id !== commentId),
      total: Math.max(prev.total - 1, 0),
    }))
  }

  return {
    comments: current && !current.error ? [...current.loaded, ...current.added] : null,
    total: current?.total ?? 0,
    hasNext: current?.hasNext ?? false,
    error: current?.error,
    loadingMore,
    loadMore,
    addComment,
    removeComment,
  }
}

export default usePostComments
