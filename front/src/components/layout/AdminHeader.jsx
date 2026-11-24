import { Link } from 'react-router-dom'
import logoImage from '../../assets/logo.png'
import { adminHeader } from '../../data/adminContent'
import './AdminHeader.css'

// 관리자 화면(SCR-014) 전용 헤더. 사이트 메뉴·상담 버튼 없이 로고와 오늘 날짜 / [이름]님 / 로그아웃만 둔다 (#117).
// 모바일 폭에서는 날짜를 숨긴다
function AdminHeader({ member, onLogoutClick }) {
  return (
    <header className="admin-header">
      <div className="admin-header__inner">
        <Link to="/" className="admin-header__logo" aria-label={adminHeader.homeLabel}>
          <img className="admin-header__logo-image" src={logoImage} alt="몽키즈유아체육" />
        </Link>
        <div className="admin-header__info">
          <span className="admin-header__date">{adminHeader.today(new Date())}</span>
          <span className="admin-header__divider admin-header__divider--date" aria-hidden="true" />
          <span className="admin-header__member">
            <span className="admin-header__member-name">{member.name}</span>
            {adminHeader.memberSuffix}
          </span>
          <span className="admin-header__divider" aria-hidden="true" />
          <button type="button" className="admin-header__logout" onClick={onLogoutClick}>
            {adminHeader.logout}
          </button>
        </div>
      </div>
    </header>
  )
}

export default AdminHeader
