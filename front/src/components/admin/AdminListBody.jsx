import { adminPage } from '../../data/adminContent'
import AdminNotice from './AdminNotice'
import './AdminListBody.css'

// 목록 자리의 불러오는 중·오류·빈 목록 처리. list는 useAdminList 결과, children(page)로 목록을 그린다.
// 조건을 바꿔 다시 불러오는 동안에는 이전 목록을 흐리게 남겨 둔다
function AdminListBody({ list, empty, children }) {
  if (list.error && !list.loading) return <AdminNotice inline error message={list.error.message} />
  if (!list.page) return <AdminNotice inline message={adminPage.loading} />
  if (list.page.content.length === 0 && !list.loading) return <AdminNotice inline message={empty} />
  return (
    <div className={`admin-list-body ${list.loading ? 'admin-list-body--loading' : ''}`} aria-busy={list.loading}>
      {children(list.page)}
    </div>
  )
}

export default AdminListBody
