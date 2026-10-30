import { adminPage } from '../../data/adminContent'
import AdminNotice from './AdminNotice'
import './AdminListBody.css'

// 배열 목록(useAdminQuery 결과)의 불러오는 중·오류·빈 목록 처리. children(items)로 목록을 그린다.
// items를 넘기면 query.data 대신 그것(걸러 낸 목록)을 쓴다. 다시 불러오는 동안에는 이전 목록을 흐리게 남겨 둔다
function AdminQueryBody({ query, items = query.data, empty, children }) {
  if (query.error && !query.loading) return <AdminNotice inline error message={query.error.message} />
  if (!query.data) return <AdminNotice inline message={adminPage.loading} />
  if (items.length === 0 && !query.loading) return <AdminNotice inline message={empty} />
  return (
    <div className={`admin-list-body ${query.loading ? 'admin-list-body--loading' : ''}`} aria-busy={query.loading}>
      {children(items)}
    </div>
  )
}

export default AdminQueryBody
