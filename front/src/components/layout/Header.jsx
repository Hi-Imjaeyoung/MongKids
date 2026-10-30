import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import logoImage from '../../assets/logo.png'
import Button from '../common/Button'
import { canUseAttendance } from '../attendance/attendanceFormat'
import { isAdmin } from '../news/postPermission'
import { headerAdminLink, newConsultationCountLabel } from '../../data/adminContent'
import { headerAttendanceLink } from '../../data/attendanceContent'
import { headerAuth } from '../../data/authContent'
import { ctaButtons, navItems } from '../../data/homeContent'
import './Header.css'

const navLinkClassName = ({ isActive }) => `gnb__link ${isActive ? 'gnb__link--active' : ''}`

// member: 로그인한 회원 정보 (비로그인이면 null)
// newConsultationCount: 신규 상담 건수 (관리자만, FR-005). 1 이상이면 "관리자" 링크에 배지로 보여 준다
function Header({
  member = null,
  newConsultationCount = null,
  onLoginClick,
  onLogoutClick,
  onInstitutionConsultClick,
  onPersonalConsultClick,
}) {
  const [menuOpen, setMenuOpen] = useState(false)

  // 로그인 버튼, 또는 회원 이름 · 수업 체크 링크(STAFF·ADMIN, EMP-SCR-001) · 관리자 링크(ADMIN만) · 로그아웃. 데스크톱은 주메뉴 오른쪽, 모바일은 햄버거 버튼 왼쪽에 둔다
  const auth = member ? (
    <>
      {/* 내 정보(SCR-025)로 들어가는 링크 */}
      <NavLink
        to="/me"
        className={({ isActive }) => `header__member ${isActive ? 'header__member--active' : ''}`}
        onClick={() => setMenuOpen(false)}
      >
        <span className="header__member-name">{member.name}</span>
        {headerAuth.memberSuffix}
      </NavLink>
      {canUseAttendance(member) && (
        <Link to="/attendance" className="header__admin-link" onClick={() => setMenuOpen(false)}>
          {headerAttendanceLink}
        </Link>
      )}
      {isAdmin(member) && (
        <Link to="/admin" className="header__admin-link" onClick={() => setMenuOpen(false)}>
          {headerAdminLink}
          {newConsultationCount > 0 && (
            <span className="header__admin-count" aria-label={newConsultationCountLabel(newConsultationCount)}>
              {newConsultationCount}
            </span>
          )}
        </Link>
      )}
      <button type="button" className="header__auth-button" onClick={onLogoutClick}>
        {headerAuth.logout}
      </button>
    </>
  ) : (
    <Button
      variant="ghost"
      onClick={() => {
        setMenuOpen(false)
        onLoginClick()
      }}
    >
      {headerAuth.login}
    </Button>
  )

  return (
    <header className="header">
      <div className="header__inner container">
        <Link to="/" className="logo" aria-label="몽키즈 홈">
          <img className="logo__image" src={logoImage} alt="몽키즈유아체육" />
        </Link>

        <nav className={`gnb ${menuOpen ? 'gnb--open' : ''}`} aria-label="주 메뉴">
          <ul className="gnb__list">
            {navItems.map((item) => (
              <li key={item.label}>
                {/* to: 라우팅된 화면, href: 아직 화면이 없는 메뉴(앵커) */}
                {item.to ? (
                  <NavLink to={item.to} className={navLinkClassName} onClick={() => setMenuOpen(false)}>
                    {item.label}
                  </NavLink>
                ) : (
                  <a href={item.href} className="gnb__link" onClick={() => setMenuOpen(false)}>
                    {item.label}
                  </a>
                )}
              </li>
            ))}
          </ul>
          <div className="header__actions">
            {/* 모바일 폭에서는 펼침 메뉴가 아닌 헤더 줄(header__auth-bar)에 보여 준다 */}
            <div className="header__auth header__auth--menu">{auth}</div>
            <div className="header__cta">
              <Button
                variant="yellow"
                onClick={() => {
                  setMenuOpen(false)
                  onInstitutionConsultClick()
                }}
              >
                {ctaButtons.institution.label}
              </Button>
              <Button
                variant="blue"
                onClick={() => {
                  setMenuOpen(false)
                  onPersonalConsultClick()
                }}
              >
                {ctaButtons.private.label}
              </Button>
            </div>
          </div>
        </nav>

        <div className="header__auth header__auth-bar">{auth}</div>

        <button
          type="button"
          className="header__toggle"
          aria-label={menuOpen ? '메뉴 닫기' : '메뉴 열기'}
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen((open) => !open)}
        >
          <span />
          <span />
          <span />
        </button>
      </div>
    </header>
  )
}

export default Header
