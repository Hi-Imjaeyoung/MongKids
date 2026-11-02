import { Suspense, lazy, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getPendingStarts } from '../api/admin'
import AdminLayout from '../components/admin/AdminLayout'
import AdminNotice from '../components/admin/AdminNotice'
import Button from '../components/common/Button'
import { isAdmin } from '../components/news/postPermission'
import { adminPage, adminTabs, newConsultationCountLabel } from '../data/adminContent'
import { pendingStartCountLabel } from '../data/attendanceAdminContent'
import ConsultationsTab from './admin/ConsultationsTab'
import MembersTab from './admin/MembersTab'
import ReviewsTab from './admin/ReviewsTab'

// 근태 관리 메뉴(EMP-SCR-006)는 들어갈 때 불러온다 (첫 화면 번들 크기)
const ClassStartsTab = lazy(() => import('./admin/ClassStartsTab'))
const ClassRecordsTab = lazy(() => import('./admin/ClassRecordsTab'))
const ClassSessionsTab = lazy(() => import('./admin/ClassSessionsTab'))
const InstitutionsTab = lazy(() => import('./admin/InstitutionsTab'))

const TAB_COMPONENTS = {
  consultations: ConsultationsTab,
  reviews: ReviewsTab,
  members: MembersTab,
  'class-starts': ClassStartsTab,
  'class-records': ClassRecordsTab,
  'class-sessions': ClassSessionsTab,
  institutions: InstitutionsTab,
}

// 메뉴 배지 (FR-005 신규 상담, EMP-FR-011 승인 대기)
function withBadges(tabs, newConsultationCount, pendingStartCount) {
  return tabs.map((item) => {
    if (item.id === 'consultations' && newConsultationCount > 0) {
      return { ...item, badge: newConsultationCount, badgeLabel: newConsultationCountLabel(newConsultationCount) }
    }
    if (item.id === 'class-starts' && pendingStartCount > 0) {
      return { ...item, badge: pendingStartCount, badgeLabel: pendingStartCountLabel(pendingStartCount) }
    }
    return item
  })
}

// 관리자 화면 (SCR-014). 메뉴는 주소(?tab=)에 남겨 새로고침해도 유지한다.
// member: undefined면 로그인 상태 확인 중, null이면 비로그인. 관리자가 아니면 API를 부르지 않고 안내만 보여 준다
// newConsultationCount: 신규 상담 건수 (FR-005). 상담 신청 메뉴에 배지로 보여 준다.
// 수업 시작 승인 대기 건수(EMP-FR-011)는 들어올 때 한 번 불러오고, 승인 메뉴에서 처리하면 그 메뉴가 알려 준다
function AdminPage({ member, newConsultationCount = null, onLoginClick }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const tab = adminTabs.find((item) => item.id === searchParams.get('tab'))?.id ?? adminTabs[0].id
  const [pendingStartCount, setPendingStartCount] = useState(null)
  const admin = isAdmin(member)

  useEffect(() => {
    if (!admin) return undefined
    let ignore = false
    getPendingStarts()
      .then((items) => {
        if (!ignore) setPendingStartCount(items.length)
      })
      .catch(() => {})
    return () => {
      ignore = true
    }
  }, [admin])

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
  if (!admin) {
    return (
      <AdminNotice message={adminPage.notAdmin}>
        <Button variant="ghost" to="/">
          {adminPage.goHome}
        </Button>
      </AdminNotice>
    )
  }

  const TabComponent = TAB_COMPONENTS[tab]
  const tabs = withBadges(adminTabs, newConsultationCount, pendingStartCount)
  return (
    <AdminLayout tabs={tabs} activeTab={tab} onTabChange={(id) => setSearchParams({ tab: id })}>
      <Suspense fallback={<AdminNotice inline message={adminPage.loading} />}>
        <TabComponent onPendingCountChange={setPendingStartCount} />
      </Suspense>
    </AdminLayout>
  )
}

export default AdminPage
