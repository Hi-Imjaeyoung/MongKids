import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Button from '../components/common/Button'
import PageHero from '../components/common/PageHero'
import ReviewFormModal from '../components/review/ReviewFormModal'
import ReviewList from '../components/review/ReviewList'
import { reviewHero, reviewList, reviewTypes } from '../data/reviewContent'
import useReviewList from './useReviewList'

// 수업 후기 (SCR-012). 유형 탭은 주소(?type=)에 남겨 새로고침·공유해도 유지한다.
// member: undefined면 로그인 상태 확인 중, null이면 비로그인 (최신 4개만, FR-012)
function ReviewsPage({ member, onLoginClick }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const type = reviewTypes.find((item) => item.id === searchParams.get('type'))?.id ?? 'ALL'
  const apiType = type === 'ALL' ? undefined : type
  const list = useReviewList(apiType, Boolean(member), member !== undefined)

  // null: 닫힘, { review: null }: 작성, { review }: 수정
  const [form, setForm] = useState(null)

  const changeType = (typeId) => setSearchParams(typeId === 'ALL' ? {} : { type: typeId })

  const handleSaved = (saved) => {
    if (form.review) list.replaceReview(saved)
    else list.addReview(saved)
  }

  const handleDelete = async (review) => {
    if (!window.confirm(reviewList.deleteConfirm)) return
    try {
      await list.removeReview(review.id)
    } catch (error) {
      window.alert(error.message)
    }
  }

  return (
    <>
      <PageHero
        eyebrow={reviewHero.eyebrow}
        title={reviewHero.title}
        description={reviewHero.description}
        titleId="review-title"
      >
        {member === null && (
          <Button variant="ghost" size="lg" onClick={onLoginClick}>
            {reviewHero.loginToWrite}
          </Button>
        )}
      </PageHero>
      <ReviewList
        member={member}
        type={type}
        onTypeChange={changeType}
        reviews={list.reviews}
        total={list.total}
        hasNext={list.hasNext}
        loadingMore={list.loadingMore}
        error={list.error}
        onLoadMore={list.loadMore}
        onWrite={() => setForm({ review: null })}
        onEdit={(review) => setForm({ review })}
        onDelete={handleDelete}
        onLoginClick={onLoginClick}
      />
      {form && <ReviewFormModal review={form.review} onSaved={handleSaved} onClose={() => setForm(null)} />}
    </>
  )
}

export default ReviewsPage
