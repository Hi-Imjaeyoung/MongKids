import { useState } from 'react'
import Button from '../components/common/Button'
import PageHero from '../components/common/PageHero'
import ReviewFormModal from '../components/review/ReviewFormModal'
import ReviewList from '../components/review/ReviewList'
import { reviewHero, reviewList } from '../data/reviewContent'
import useReviewList from './useReviewList'

// 수업 후기 (SCR-012). 유형 탭은 두지 않는다 (2026-11-05).
// member: undefined면 로그인 상태 확인 중, null이면 비로그인 (최신 4개만, FR-012)
function ReviewsPage({ member, onLoginClick }) {
  const list = useReviewList(Boolean(member), member !== undefined)

  // null: 닫힘, { review: null }: 작성, { review }: 수정
  const [form, setForm] = useState(null)

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
        reviews={list.reviews}
        total={list.total}
        hasNext={list.hasNext}
        loadingMore={list.loadingMore}
        error={list.error}
        onLoadMore={list.loadMore}
        onWrite={() => setForm({ review: null })}
        onEdit={(review) => setForm({ review })}
        onDelete={handleDelete}
        onReplySaved={list.replaceReview}
        onLoginClick={onLoginClick}
      />
      {form && <ReviewFormModal review={form.review} onSaved={handleSaved} onClose={() => setForm(null)} />}
    </>
  )
}

export default ReviewsPage
