import { useEffect, useState } from 'react'

// 게시물 공감 상태 (FR-011). board의 getLike·like·unlike로 수업 현장·연구소 글을 함께 다룬다.
// 로그인 상태가 바뀌면 내 공감 여부가 달라지므로 memberId가 바뀔 때도 다시 불러온다.
// initialCount: 공감 상태를 불러오기 전에 보여 줄 공감 수 (상세 응답의 likeCount)
function usePostLike(postId, memberId, { getLike, like, unlike }, initialCount) {
  const [result, setResult] = useState(null)
  const [pending, setPending] = useState(false)
  const key = `${postId}:${memberId ?? ''}`

  useEffect(() => {
    let ignore = false
    getLike(postId)
      .then((state) => {
        if (!ignore) setResult({ key, ...state })
      })
      .catch(() => {
        // 공감 상태를 못 불러와도 상세는 보여 준다. 공감 수는 상세 응답 값으로 둔다
        if (!ignore) setResult({ key, liked: false, likeCount: null })
      })
    return () => {
      ignore = true
    }
  }, [key, postId, getLike])

  const current = result?.key === key ? result : null

  const toggle = async () => {
    setPending(true)
    try {
      const state = current?.liked ? await unlike(postId) : await like(postId)
      setResult({ key, ...state })
    } catch (error) {
      window.alert(error.message)
    } finally {
      setPending(false)
    }
  }

  return {
    liked: current?.liked ?? false,
    likeCount: current?.likeCount ?? initialCount,
    ready: Boolean(current),
    pending,
    toggle,
  }
}

export default usePostLike
