import { useSearchParams } from 'react-router-dom'
import AdminLayout from '../components/admin/AdminLayout'
import AdminNotice from '../components/admin/AdminNotice'
import Button from '../components/common/Button'
import { isAdmin } from '../components/news/postPermission'
import { adminPage, adminTabs, newConsultationCountLabel } from '../data/adminContent'
import ConsultationsTab from './admin/ConsultationsTab'
import MembersTab from './admin/MembersTab'
import ReviewsTab from './admin/ReviewsTab'

const TAB_COMPONENTS = {
  consultations: ConsultationsTab,
  reviews: ReviewsTab,
  members: MembersTab,
}

// 관리자 화면 (SCR-014). 메뉴는 주소(?tab=)에 남겨 새로고침해도 유지한다.
// member: undefined면 로그인 상태 확인 중, null이면 비로그인. 관리자가 아니면 API를 부르지 않고 안내만 보여 준다
// newConsultationCount: 신규 상담 건수 (FR-005). 상담 신청 메뉴에 배지로 보여 준다
function AdminPage({ member, newConsultationCount = null, onLoginClick }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const tab = adminTabs.find((item) => item.id === searchParams.get('tab'))?.id ?? adminTabs[0].id

  if (member === undefined) return <AdminNotice message={adminPage.checking} />
  if (member === null) {
    return (
      <AdminNotice message={adminPage.loginRequired}>
        <Button variant="blue" onClick={onLoginClick}>
          {adminPage.login}
        </Button>
      </AdminNotice>
    )
  }
  if (!isAdmin(member)) {
    return (
      <AdminNotice message={adminPage.notAdmin}>
        <Button variant="ghost" to="/">
          {adminPage.goHome}
        </Button>
      </AdminNotice>
    )
  }

  const TabComponent = TAB_COMPONENTS[tab]
  const tabs = adminTabs.map((item) =>
    item.id === 'consultations' && newConsultationCount > 0
      ? { ...item, badge: newConsultationCount, badgeLabel: newConsultationCountLabel(newConsultationCount) }
      : item,
  )
  return (
    <AdminLayout tabs={tabs} activeTab={tab} onTabChange={(id) => setSearchParams({ tab: id })}>
      <TabComponent />
    </AdminLayout>
  )
}

export default AdminPage
