import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getPosts } from '../api/posts'
import NewsHero from '../components/news/NewsHero'
import PostList from '../components/news/PostList'
import { findCategory } from '../components/news/postFormat'
import { canWritePost } from '../components/news/postPermission'

const PAGE_SIZE = 12

// 수업 현장 목록 (SCR-006). 분류 탭은 주소(?category=)에 남겨 새로고침·공유해도 유지한다
function NewsListPage({ member }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const category = findCategory(searchParams.get('category')).id
  const apiCategory = category === 'ALL' ? undefined : category

  // key: 어떤 분류의 결과인지. 분류가 바뀌면 이전 결과를 보여 주지 않는다
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loadingMore, setLoadingMore] = useState(false)

  useEffect(() => {
    let ignore = false
    getPosts({ category: apiCategory, size: PAGE_SIZE })
      .then((data) => {
        if (ignore) return
        setResult({ key: category, ...data })
        setError(null)
      })
      .catch((loadError) => {
        if (!ignore) setError({ key: category, message: loadError.message })
      })
    return () => {
      ignore = true
    }
  }, [category, apiCategory])

  const current = result?.key === category ? result : null
  const currentError = error?.key === category ? error.message : null

  const changeCategory = (categoryId) => {
    setSearchParams(categoryId === 'ALL' ? {} : { category: categoryId })
  }

  const loadMore = async () => {
    setLoadingMore(true)
    try {
      const data = await getPosts({ category: apiCategory, page: current.page + 1, size: PAGE_SIZE })
      setResult({ ...data, key: category, content: [...current.content, ...data.content] })
    } catch (loadError) {
      setError({ key: category, message: loadError.message })
    } finally {
      setLoadingMore(false)
    }
  }

  return (
    <>
      <NewsHero />
      <PostList
        category={category}
        onCategoryChange={changeCategory}
        posts={current?.content ?? null}
        total={current?.totalElements ?? 0}
        hasNext={Boolean(current?.hasNext)}
        loadingMore={loadingMore}
        error={currentError}
        onLoadMore={loadMore}
        canWrite={canWritePost(member)}
      />
    </>
  )
}

export default NewsListPage
