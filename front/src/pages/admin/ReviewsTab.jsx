import { useState } from 'react'
import { changeReviewHidden, getAdminReviews } from '../../api/admin'
import AdminListBody from '../../components/admin/AdminListBody'
import AdminReviewCard from '../../components/admin/AdminReviewCard'
import AdminSection from '../../components/admin/AdminSection'
import HiddenOnlyToggle from '../../components/admin/HiddenOnlyToggle'
import Pagination from '../../components/lab/Pagination'
import { reviewAdmin } from '../../data/adminContent'
import useAdminList from '../useAdminList'

// 후기 관리 (FR-021). 숨기기·다시 공개는 확인 없이 바로 처리한다 (사용자 결정 2026-10-24)
// 후기 유형 필터는 두지 않는다 (2026-11-05)
function ReviewsTab() {
  const [hiddenOnly, setHiddenOnly] = useState(false)
  const [page, setPage] = useState(1)
  const list = useAdminList(getAdminReviews, { hiddenOnly, page: page - 1 })
  const [busyId, setBusyId] = useState(null)

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
      toolbar={<HiddenOnlyToggle checked={hiddenOnly} onChange={toggleHiddenOnly} />}
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
