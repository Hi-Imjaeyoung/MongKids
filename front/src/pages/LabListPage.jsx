import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getLabPosts } from '../api/labPosts'
import LabHero from '../components/lab/LabHero'
import LabList from '../components/lab/LabList'
import { findCategory } from '../components/news/postFormat'
import { canWritePost } from '../components/news/postPermission'
import { labCategories } from '../data/labContent'

// 3열 × 3줄
const PAGE_SIZE = 9

// 몽키즈 연구소 목록 (SCR-024). 분류·검색어·페이지는 주소(?category=&keyword=&page=)에 남겨 새로고침·공유해도 유지한다.
// 비로그인도 전체를 본다 (FR-012 제한 없음)
function LabListPage({ member }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const category = findCategory(searchParams.get('category'), labCategories).id
  const keyword = searchParams.get('keyword')?.trim() ?? ''
  const page = Math.max(Number.parseInt(searchParams.get('page'), 10) || 1, 1)
  const apiCategory = category === 'ALL' ? undefined : category
  const key = `${category}|${keyword}|${page}`

  // key: 어떤 조건의 결과인지. 조건이 바뀌면 이전 결과를 보여 주지 않는다
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    let ignore = false
    getLabPosts({ category: apiCategory, keyword, page: page - 1, size: PAGE_SIZE })
      .then((data) => {
        if (ignore) return
        setResult({ key, ...data })
        setError(null)
      })
      .catch((loadError) => {
        if (!ignore) setError({ key, message: loadError.message })
      })
    return () => {
      ignore = true
    }
  }, [key, apiCategory, keyword, page])

  const current = result?.key === key ? result : null
  const currentError = error?.key === key ? error.message : null

  // 기본값(전체·검색어 없음·1페이지)은 주소에서 뺀다
  const updateParams = (next) => {
    const merged = { category, keyword, page, ...next }
    const params = {}
    if (merged.category !== 'ALL') params.category = merged.category
    if (merged.keyword) params.keyword = merged.keyword
    if (merged.page > 1) params.page = String(merged.page)
    setSearchParams(params)
  }

  const changePage = (nextPage) => {
    updateParams({ page: nextPage })
    document.getElementById('lab-list')?.scrollIntoView()
  }

  return (
    <>
      <LabHero keyword={keyword} onSearch={(nextKeyword) => updateParams({ keyword: nextKeyword, page: 1 })} />
      <LabList
        category={category}
        onCategoryChange={(categoryId) => updateParams({ category: categoryId, page: 1 })}
        keyword={keyword}
        onClearSearch={() => updateParams({ keyword: '', page: 1 })}
        labPosts={current?.content ?? null}
        total={current?.totalElements ?? 0}
        page={page}
        totalPages={current?.totalPages ?? 0}
        onPageChange={changePage}
        error={currentError}
        canWrite={canWritePost(member)}
      />
    </>
  )
}

export default LabListPage
