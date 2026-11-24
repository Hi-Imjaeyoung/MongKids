import { useState } from 'react'
import { createReviewCode, getReviewCodes, revokeReviewCode } from '../../api/admin'
import AdminListBody from '../../components/admin/AdminListBody'
import AdminSection from '../../components/admin/AdminSection'
import FilterChips from '../../components/admin/FilterChips'
import ReviewCodeForm from '../../components/admin/ReviewCodeForm'
import ReviewCodeTable from '../../components/admin/ReviewCodeTable'
import Pagination from '../../components/lab/Pagination'
import { reviewCodeAdmin, reviewCodeFilters } from '../../data/adminContent'
import useAdminList from '../useAdminList'
import './ReviewCodesTab.css'

const ACTIVE_PARAM = { ALL: undefined, ACTIVE: true, ENDED: false }

// 후기 작성 코드 (FR-029). 왼쪽 발급 폼, 오른쪽 발급한 코드 목록. 폐기는 확인 창을 거친다.
// 발급하면 목록을 첫 쪽부터 다시 불러온다(version은 다시 불러오기 위한 값이고 API에는 보내지 않는다)
function ReviewCodesTab() {
  const [filter, setFilter] = useState('ALL')
  const [page, setPage] = useState(1)
  const [version, setVersion] = useState(0)
  const list = useAdminList(getReviewCodes, { active: ACTIVE_PARAM[filter], page: page - 1, version })
  const [busyId, setBusyId] = useState(null)

  const changeFilter = (value) => {
    setFilter(value)
    setPage(1)
  }

  const issue = async (body) => {
    const code = await createReviewCode(body)
    setPage(1)
    setVersion((current) => current + 1)
    return code
  }

  const revoke = async (code) => {
    if (!window.confirm(reviewCodeAdmin.revokeConfirm(code.code))) return
    setBusyId(code.id)
    try {
      const updated = await revokeReviewCode(code.id)
      list.updateItem(
        (item) => item.id === updated.id,
        () => updated,
      )
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <AdminSection title={reviewCodeAdmin.title} description={reviewCodeAdmin.description}>
      <div className="review-codes-tab">
        <ReviewCodeForm onIssue={issue} />
        <div className="review-codes-tab__list">
          <FilterChips
            label={reviewCodeAdmin.filterLabel}
            options={reviewCodeFilters}
            value={filter}
            onChange={changeFilter}
          />
          <AdminListBody list={list} empty={reviewCodeAdmin.empty}>
            {(result) => <ReviewCodeTable codes={result.content} busyId={busyId} onRevoke={revoke} />}
          </AdminListBody>
          {list.page && <Pagination page={page} totalPages={list.page.totalPages} onChange={setPage} />}
        </div>
      </div>
    </AdminSection>
  )
}

export default ReviewCodesTab
