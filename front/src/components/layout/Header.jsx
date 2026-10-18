import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import logoImage from '../../assets/logo.png'
import Button from '../common/Button'
import { headerAuth } from '../../data/authContent'
import { ctaButtons, navItems } from '../../data/homeContent'
import './Header.css'

const navLinkClassName = ({ isActive }) => `gnb__link ${isActive ? 'gnb__link--active' : ''}`

// member: 로그인한 회원 정보 (비로그인이면 null)
function Header({ member = null, onLoginClick, onLogoutClick, onInstitutionConsultClick, onPersonalConsultClick }) {
  const [menuOpen, setMenuOpen] = useState(false)

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
            <div className="header__auth">
              {member ? (
                <>
                  <span className="header__member">
                    <span className="header__member-name">{member.name}</span>
                    {headerAuth.memberSuffix}
                  </span>
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
              )}
            </div>
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
