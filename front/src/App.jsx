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
import HomePage from './pages/HomePage'
import LabListPage from './pages/LabListPage'
import NewsDetailPage from './pages/NewsDetailPage'
import NewsListPage from './pages/NewsListPage'
import { labBoard } from './pages/boards'

// 편집기(TipTap)가 커서 작성·수정 화면에 들어갈 때만 불러온다
const NewsEditorPage = lazy(() => import('./pages/NewsEditorPage'))

// 모바일 하단 고정 상담 바를 두지 않는 화면 (SCR-024 몽키즈 연구소 목록)
const NO_MOBILE_CTA_PATHS = ['/lab']

// 소셜 로그인 실패 시 백엔드가 붙여 보내는 쿼리 파라미터
const LOGIN_ERROR_PARAM = 'loginError'

function hasLoginError() {
  return new URLSearchParams(window.location.search).has(LOGIN_ERROR_PARAM)
}

function App() {
  const { pathname } = useLocation()
  // undefined: 로그인 상태 확인 중, null: 비로그인
  const [member, setMember] = useState(undefined)
  const [loginError, setLoginError] = useState(hasLoginError)
  const [loginOpen, setLoginOpen] = useState(hasLoginError)
  const [consultOpen, setConsultOpen] = useState(false)
  const [personalConsultOpen, setPersonalConsultOpen] = useState(false)

  useEffect(() => {
    fetchMyInfo()
      .then(setMember)
      .catch(() => setMember(null))
  }, [])

  // 실패 표시는 한 번만 보여 주고 주소에서 지운다
  useEffect(() => {
    if (!hasLoginError()) return
    const url = new URL(window.location.href)
    url.searchParams.delete(LOGIN_ERROR_PARAM)
    window.history.replaceState(null, '', url)
  }, [])

  // 화면이 바뀌면 맨 위에서 시작한다
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])

  // 하단 상담 바가 없는 화면은 바 높이만큼 비워 둔 여백(--mobile-cta-height)도 없앤다
  const showMobileCta = !NO_MOBILE_CTA_PATHS.includes(pathname)
  useEffect(() => {
    document.documentElement.classList.toggle('no-mobile-cta', !showMobileCta)
  }, [showMobileCta])

  const openLogin = () => setLoginOpen(true)
  const closeLogin = useCallback(() => {
    setLoginOpen(false)
    setLoginError(false)
  }, [])

  const openConsult = () => setConsultOpen(true)
  const closeConsult = useCallback(() => setConsultOpen(false), [])
  const openPersonalConsult = () => setPersonalConsultOpen(true)
  const closePersonalConsult = useCallback(() => setPersonalConsultOpen(false), [])
  const homePage = (
    <HomePage onInstitutionConsultClick={openConsult} onPersonalConsultClick={openPersonalConsult} />
  )

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
        onLoginClick={openLogin}
        onLogoutClick={handleLogout}
        onInstitutionConsultClick={openConsult}
        onPersonalConsultClick={openPersonalConsult}
      />
      <main className="main">
        <Suspense fallback={null}>
          <Routes>
            <Route path="/" element={homePage} />
            <Route path="/news" element={<NewsListPage member={member} />} />
            <Route path="/news/write" element={<NewsEditorPage member={member} onLoginClick={openLogin} />} />
            <Route path="/news/:postId" element={<NewsDetailPage member={member} />} />
            <Route path="/news/:postId/edit" element={<NewsEditorPage member={member} onLoginClick={openLogin} />} />
            <Route path="/lab" element={<LabListPage member={member} />} />
            <Route
              path="/lab/write"
              element={<NewsEditorPage member={member} onLoginClick={openLogin} board={labBoard} />}
            />
            <Route path="/lab/:postId" element={<NewsDetailPage member={member} board={labBoard} />} />
            <Route
              path="/lab/:postId/edit"
              element={<NewsEditorPage member={member} onLoginClick={openLogin} board={labBoard} />}
            />
            <Route path="*" element={homePage} />
          </Routes>
        </Suspense>
      </main>
      <Footer />
      <FloatingMenu />
      {showMobileCta && (
        <MobileCtaBar onInstitutionConsultClick={openConsult} onPersonalConsultClick={openPersonalConsult} />
      )}
      {loginOpen && <LoginModal hasError={loginError} onClose={closeLogin} />}
      {consultOpen && <InstitutionConsultationModal onClose={closeConsult} />}
      {personalConsultOpen && <PersonalConsultationModal onClose={closePersonalConsult} />}
    </>
  )
}

export default App
