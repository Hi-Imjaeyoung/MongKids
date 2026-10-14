import { useCallback, useEffect, useState } from 'react'
import { fetchMyInfo, logout } from './api/auth'
import LoginModal from './components/auth/LoginModal'
import Header from './components/layout/Header'
import FloatingMenu from './components/layout/FloatingMenu'
import HomePage from './pages/HomePage'

// 소셜 로그인 실패 시 백엔드가 붙여 보내는 쿼리 파라미터
const LOGIN_ERROR_PARAM = 'loginError'

function hasLoginError() {
  return new URLSearchParams(window.location.search).has(LOGIN_ERROR_PARAM)
}

function App() {
  const [member, setMember] = useState(null)
  const [loginError, setLoginError] = useState(hasLoginError)
  const [loginOpen, setLoginOpen] = useState(hasLoginError)

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

  const openLogin = () => setLoginOpen(true)
  const closeLogin = useCallback(() => {
    setLoginOpen(false)
    setLoginError(false)
  }, [])

  const handleLogout = async () => {
    try {
      await logout()
    } finally {
      setMember(null)
    }
  }

  return (
    <>
      <Header member={member} onLoginClick={openLogin} onLogoutClick={handleLogout} />
      <main className="main">
        <HomePage />
      </main>
      <FloatingMenu />
      {loginOpen && <LoginModal hasError={loginError} onClose={closeLogin} />}
    </>
  )
}

export default App
