import { useState } from 'react'
import Button from '../common/Button'
import { headerAuth } from '../../data/authContent'
import { ctaButtons, navItems } from '../../data/homeContent'
import './Header.css'

// member: 로그인한 회원 정보 (비로그인이면 null)
function Header({ member = null, onLoginClick, onLogoutClick }) {
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <header className="header">
      <div className="header__inner container">
        <a href="/" className="logo" aria-label="몽키즈 홈">
          <span className="logo__mascot" aria-hidden="true">🐵</span>
          <span className="logo__text">몽키즈</span>
        </a>

        <nav className={`gnb ${menuOpen ? 'gnb--open' : ''}`} aria-label="주 메뉴">
          <ul className="gnb__list">
            {navItems.map((item) => (
              <li key={item.label}>
                <a href={item.href} className="gnb__link" onClick={() => setMenuOpen(false)}>
                  {item.label}
                </a>
              </li>
            ))}
          </ul>
          <div className="header__cta">
            <Button variant="yellow">{ctaButtons.institution.label}</Button>
            <Button variant="blue">{ctaButtons.private.label}</Button>
          </div>
          <div className="header__auth">
            {member ? (
              <>
                <span className="header__member">
                  {member.name}
                  {headerAuth.memberSuffix}
                </span>
                <button type="button" className="header__auth-button" onClick={onLogoutClick}>
                  {headerAuth.logout}
                </button>
              </>
            ) : (
              <button
                type="button"
                className="header__auth-button"
                onClick={() => {
                  setMenuOpen(false)
                  onLoginClick()
                }}
              >
                {headerAuth.login}
              </button>
            )}
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
