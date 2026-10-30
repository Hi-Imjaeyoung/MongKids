import { Suspense, lazy, useCallback, useEffect, useState } from 'react'
import { Route, Routes, useLocation } from 'react-router-dom'
import { fetchMyInfo, logout } from './api/auth'
import LoginModal from './components/auth/LoginModal'
import InstitutionConsultationModal from './components/consultation/InstitutionConsultationModal'
import PersonalConsultationModal from './components/consultation/PersonalConsultationModal'
import Header from './components/layout/Header'
import Footer from './components/layout/Footer'
import FloatingMenu from './components/layout/FloatingMenu'
import MobileCtaBar from './components/layout/MobileCtaBar'
import WithdrawDone from './components/member/WithdrawDone'
import { isAdmin } from './components/news/postPermission'
import AboutPage from './pages/AboutPage'
import AdminPage from './pages/AdminPage'
import AttendancePage from './pages/AttendancePage'
import FaqPage from './pages/FaqPage'
import HomePage from './pages/HomePage'
import LabListPage from './pages/LabListPage'
import MyInfoPage from './pages/MyInfoPage'
import NewsDetailPage from './pages/NewsDetailPage'
import NewsListPage from './pages/NewsListPage'
import ProgramDetailPage from './pages/ProgramDetailPage'
import ProgramsPage from './pages/ProgramsPage'
import ReviewsPage from './pages/ReviewsPage'
import { labBoard } from './pages/boards'
import useNewConsultationCount from './pages/useNewConsultationCount'

// 편집기(TipTap)가 커서 작성·수정 화면에 들어갈 때만 불러온다
const NewsEditorPage = lazy(() => import('./pages/NewsEditorPage'))

// 모바일 하단 고정 상담 바를 두지 않는 화면
// SCR-024 몽키즈 연구소 목록, SCR-003 프로그램 전체 안내, SCR-022 자주 묻는 질문, SCR-012 수업 후기, SCR-014 관리자,
// 근태 EMP-SCR-001 수업 시작·종료 체크, SCR-025 내 정보
const NO_MOBILE_CTA_PATHS = ['/lab', '/programs', '/faq', '/reviews', '/admin', '/attendance', '/me']
// SCR-016~020 프로그램 상세 (/programs/:programId)
const NO_MOBILE_CTA_PREFIXES = ['/programs/']
// 고객용 푸터와 빠른 상담 버튼(후기·전화·인스타그램)을 두지 않는 직원용 화면 (근태 EMP-SCR-001)
const STAFF_TOOL_PATHS = ['/attendance']

function hasMobileCta(pathname) {
  return (
    !NO_MOBILE_CTA_PATHS.includes(pathname) && !NO_MOBILE_CTA_PREFIXES.some((prefix) => pathname.startsWith(prefix))
  )
}

// 소셜 로그인 실패 시 백엔드가 붙여 보내는 쿼리 파라미터
const LOGIN_ERROR_PARAM = 'loginError'
// 회원 탈퇴(FR-028)가 끝나면 백엔드가 홈으로 돌려보내며 붙이는 쿼리 파라미터
const WITHDRAWN_PARAM = 'withdrawn'

function hasParam(name) {
  return new URLSearchParams(window.location.search).has(name)
}

function hasLoginError() {
  return hasParam(LOGIN_ERROR_PARAM)
}

function hasWithdrawn() {
  return hasParam(WITHDRAWN_PARAM)
}

function App() {
  const { pathname, hash } = useLocation()
  // undefined: 로그인 상태 확인 중, null: 비로그인
  const [member, setMember] = useState(undefined)
  const [loginError, setLoginError] = useState(hasLoginError)
  const [loginOpen, setLoginOpen] = useState(hasLoginError)
  const [withdrawDoneOpen, setWithdrawDoneOpen] = useState(hasWithdrawn)
  const [consultOpen, setConsultOpen] = useState(false)
  const [personalConsultOpen, setPersonalConsultOpen] = useState(false)
  // 관리자로 로그인해 있는 동안만 구독한다 (FR-005). 로그아웃하면 member가 null이 되어 연결을 닫는다
  const newConsultationCount = useNewConsultationCount(isAdmin(member))

  useEffect(() => {
    fetchMyInfo()
      .then(setMember)
      .catch(() => setMember(null))
  }, [])

  // 로그인 실패·탈퇴 완료 표시는 한 번만 보여 주고 주소에서 지운다
  useEffect(() => {
    if (!hasLoginError() && !hasWithdrawn()) return
    const url = new URL(window.location.href)
    url.searchParams.delete(LOGIN_ERROR_PARAM)
    url.searchParams.delete(WITHDRAWN_PARAM)
    window.history.replaceState(null, '', url)
  }, [])

  // 화면이 바뀌면 맨 위에서 시작한다. 주소에 #앵커가 있으면 그 섹션으로 이동한다 (예: /programs#special)
  useEffect(() => {
    const target = hash && document.getElementById(decodeURIComponent(hash.slice(1)))
    if (target) target.scrollIntoView()
    else window.scrollTo(0, 0)
  }, [pathname, hash])

  // 하단 상담 바가 없는 화면은 바 높이만큼 비워 둔 여백(--mobile-cta-height)도 없앤다
  const showMobileCta = hasMobileCta(pathname)
  const isStaffTool = STAFF_TOOL_PATHS.includes(pathname)
  useEffect(() => {
    document.documentElement.classList.toggle('no-mobile-cta', !showMobileCta)
  }, [showMobileCta])

  const openLogin = () => setLoginOpen(true)
  const closeLogin = useCallback(() => {
    setLoginOpen(false)
    setLoginError(false)
  }, [])

  const closeWithdrawDone = useCallback(() => setWithdrawDoneOpen(false), [])

  const openConsult = () => setConsultOpen(true)
  const closeConsult = useCallback(() => setConsultOpen(false), [])
  const openPersonalConsult = () => setPersonalConsultOpen(true)
  const closePersonalConsult = useCallback(() => setPersonalConsultOpen(false), [])
  const consultHandlers = { onInstitutionConsultClick: openConsult, onPersonalConsultClick: openPersonalConsult }
  const homePage = <HomePage {...consultHandlers} />

  const handleLogout = async () => {
    try {
      await logout()
    } finally {
      setMember(null)
    }
  }

  return (
    <>
      <Header
        member={member}
        newConsultationCount={newConsultationCount}
        onLoginClick={openLogin}
        onLogoutClick={handleLogout}
        onInstitutionConsultClick={openConsult}
        onPersonalConsultClick={openPersonalConsult}
      />
      <main className={`main ${isStaffTool ? 'main--staff-tool' : ''}`}>
        <Suspense fallback={null}>
          <Routes>
            <Route path="/" element={homePage} />
            <Route path="/about" element={<AboutPage {...consultHandlers} />} />
            <Route path="/programs" element={<ProgramsPage {...consultHandlers} />} />
            <Route path="/programs/:programId" element={<ProgramDetailPage {...consultHandlers} />} />
            <Route path="/faq" element={<FaqPage {...consultHandlers} />} />
            <Route path="/news" element={<NewsListPage member={member} />} />
            <Route path="/news/write" element={<NewsEditorPage member={member} onLoginClick={openLogin} />} />
            <Route path="/news/:postId" element={<NewsDetailPage member={member} onLoginClick={openLogin} />} />
            <Route path="/news/:postId/edit" element={<NewsEditorPage member={member} onLoginClick={openLogin} />} />
            <Route path="/lab" element={<LabListPage member={member} />} />
            <Route
              path="/lab/write"
              element={<NewsEditorPage member={member} onLoginClick={openLogin} board={labBoard} />}
            />
            <Route
              path="/lab/:postId"
              element={<NewsDetailPage member={member} onLoginClick={openLogin} board={labBoard} />}
            />
            <Route
              path="/lab/:postId/edit"
              element={<NewsEditorPage member={member} onLoginClick={openLogin} board={labBoard} />}
            />
            <Route path="/reviews" element={<ReviewsPage member={member} onLoginClick={openLogin} />} />
            <Route path="/admin" element={<AdminPage member={member} newConsultationCount={newConsultationCount} onLoginClick={openLogin} />} />
            <Route path="/attendance" element={<AttendancePage member={member} onLoginClick={openLogin} />} />
            <Route
              path="/me"
              element={<MyInfoPage member={member} onLoginClick={openLogin} onMemberChange={setMember} />}
            />
            <Route path="*" element={homePage} />
          </Routes>
        </Suspense>
      </main>
      {!isStaffTool && <Footer />}
      {!isStaffTool && <FloatingMenu />}
      {showMobileCta && (
        <MobileCtaBar onInstitutionConsultClick={openConsult} onPersonalConsultClick={openPersonalConsult} />
      )}
      {loginOpen && <LoginModal hasError={loginError} onClose={closeLogin} />}
      {withdrawDoneOpen && <WithdrawDone onClose={closeWithdrawDone} />}
      {consultOpen && <InstitutionConsultationModal onClose={closeConsult} />}
      {personalConsultOpen && <PersonalConsultationModal onClose={closePersonalConsult} />}
    </>
  )
}

export default App
