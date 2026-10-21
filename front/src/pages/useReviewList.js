import { useCallback, useEffect, useState } from 'react'
import { deleteReview, getReviews } from '../api/reviews'

const PAGE_SIZE = 10

// 서버 목록에 없는 후기만 이어 붙인다
function appendNew(list, items) {
  const ids = new Set(list.map((review) => review.id))
  return [...list, ...items.filter((review) => !ids.has(review.id))]
}

// 수업 후기 목록 상태 (SCR-012). 최신순으로 PAGE_SIZE씩 이어서 불러온다 (무한 스크롤).
// 다음 페이지 번호는 불러온 개수로 계산한다. 새 후기는 맨 앞에, 지운 후기는 빼므로 서버 순서와 어긋나지 않고,
// 겹쳐 받은 후기는 id로 걸러 낸다.
// enabled: 로그인 상태를 확인한 뒤에만 불러온다 (비로그인은 서버가 최신 4개만 준다, FR-012)
function useReviewList(type, loggedIn, enabled) {
  const key = `${type ?? 'ALL'}|${loggedIn}`
  const [result, setResult] = useState(null)
  const [loadingMore, setLoadingMore] = useState(false)

  useEffect(() => {
    if (!enabled) return undefined
    let ignore = false
    getReviews({ type, size: PAGE_SIZE })
      .then((page) => {
        if (!ignore) setResult({ key, reviews: page.content, total: page.totalElements, hasNext: page.hasNext })
      })
      .catch((error) => {
        if (!ignore) setResult({ key, error: error.message })
      })
    return () => {
      ignore = true
    }
  }, [key, type, enabled])

  const current = result?.key === key ? result : null

  const loadMore = useCallback(async () => {
    if (!current?.hasNext || loadingMore) return
    setLoadingMore(true)
    try {
      const page = await getReviews({
        type,
        page: Math.floor(current.reviews.length / PAGE_SIZE),
        size: PAGE_SIZE,
      })
      setResult((prev) =>
        prev.key === key
          ? { ...prev, reviews: appendNew(prev.reviews, page.content), total: page.totalElements, hasNext: page.hasNext }
          : prev,
      )
    } catch (error) {
      setResult((prev) => (prev.key === key ? { ...prev, hasNext: false, moreError: error.message } : prev))
    } finally {
      setLoadingMore(false)
    }
  }, [current, loadingMore, type, key])

  // 지금 탭에 맞는 새 후기만 맨 앞에 넣는다
  const addReview = (review) => {
    if (type && review.type !== type) return
    setResult((prev) => ({ ...prev, reviews: [review, ...prev.reviews], total: prev.total + 1 }))
  }

  // 유형을 바꿔 지금 탭에서 빠지면 목록에서 뺀다
  const replaceReview = (review) => {
    setResult((prev) => {
      if (type && review.type !== type) {
        return { ...prev, reviews: prev.reviews.filter((item) => item.id !== review.id), total: prev.total - 1 }
      }
      return { ...prev, reviews: prev.reviews.map((item) => (item.id === review.id ? review : item)) }
    })
  }

  const removeReview = async (reviewId) => {
    await deleteReview(reviewId)
    setResult((prev) => ({
      ...prev,
      reviews: prev.reviews.filter((review) => review.id !== reviewId),
      total: Math.max(prev.total - 1, 0),
    }))
  }

  return {
    reviews: current && !current.error ? current.reviews : null,
    total: current?.total ?? 0,
    hasNext: current?.hasNext ?? false,
    error: current?.error ?? current?.moreError,
    loadingMore,
    loadMore,
    addReview,
    replaceReview,
    removeReview,
  }
}

export default useReviewList
