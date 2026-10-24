import { useState } from 'react'
import { changeReviewHidden, getAdminReviews } from '../../api/admin'
import AdminListBody from '../../components/admin/AdminListBody'
import AdminReviewCard from '../../components/admin/AdminReviewCard'
import AdminSection from '../../components/admin/AdminSection'
import FilterChips from '../../components/admin/FilterChips'
import HiddenOnlyToggle from '../../components/admin/HiddenOnlyToggle'
import { toApiFilter } from '../../components/admin/adminFormat'
import Pagination from '../../components/lab/Pagination'
import { reviewAdmin } from '../../data/adminContent'
import { reviewTypes } from '../../data/reviewContent'
import useAdminList from '../useAdminList'

// 후기 관리 (FR-021). 숨기기·다시 공개는 확인 없이 바로 처리한다 (사용자 결정 2026-10-24)
function ReviewsTab() {
  const [type, setType] = useState('ALL')
  const [hiddenOnly, setHiddenOnly] = useState(false)
  const [page, setPage] = useState(1)
  const list = useAdminList(getAdminReviews, { type: toApiFilter(type), hiddenOnly, page: page - 1 })
  const [busyId, setBusyId] = useState(null)

  const changeType = (value) => {
    setType(value)
    setPage(1)
  }
  const toggleHiddenOnly = (checked) => {
    setHiddenOnly(checked)
    setPage(1)
  }

  const toggleHidden = async ({ review, hidden }) => {
    setBusyId(review.id)
    try {
      const updated = await changeReviewHidden(review.id, !hidden)
      list.updateItem(
        (item) => item.review.id === review.id,
        () => updated,
      )
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <AdminSection
      title={reviewAdmin.title}
      description={reviewAdmin.description}
      toolbar={
        <>
          <FilterChips label={reviewAdmin.title} options={reviewTypes} value={type} onChange={changeType} />
          <HiddenOnlyToggle checked={hiddenOnly} onChange={toggleHiddenOnly} />
        </>
      }
    >
      <AdminListBody list={list} empty={reviewAdmin.empty}>
        {(result) =>
          result.content.map((item) => (
            <AdminReviewCard
              key={item.review.id}
              item={item}
              busy={busyId === item.review.id}
              onToggleHidden={toggleHidden}
            />
          ))
        }
      </AdminListBody>
      {list.page && <Pagination page={page} totalPages={list.page.totalPages} onChange={setPage} />}
    </AdminSection>
  )
}

export default ReviewsTab
