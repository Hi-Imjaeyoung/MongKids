import { adminPage } from '../../data/adminContent'
import './AdminLayout.css'

// 관리자 화면(SCR-014) 틀: 왼쪽 메뉴(모바일은 위로 쌓임) + 본문
// tabs: [{ id, label, badge? }], activeTab, onTabChange(id)
function AdminLayout({ tabs, activeTab, onTabChange, children }) {
  return (
    <div className="admin-layout">
      <div className="admin-layout__inner container">
        <nav className="admin-layout__nav" aria-label={adminPage.menuLabel}>
          <p className="admin-layout__eyebrow">{adminPage.eyebrow}</p>
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              className={`admin-layout__tab ${tab.id === activeTab ? 'admin-layout__tab--active' : ''}`}
              aria-current={tab.id === activeTab ? 'page' : undefined}
              onClick={() => onTabChange(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </nav>
        <div className="admin-layout__content">{children}</div>
      </div>
    </div>
  )
}

export default AdminLayout
