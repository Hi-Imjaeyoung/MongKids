import { Fragment } from 'react'
import { adminPage } from '../../data/adminContent'
import './AdminLayout.css'

// 관리자 화면(SCR-014) 틀: 왼쪽 메뉴(모바일은 위로 쌓임) + 본문
// tabs: [{ id, label, group?, badge?, badgeLabel? }], activeTab, onTabChange(id). badge는 1 이상일 때만 보인다.
// group이 앞 메뉴와 달라지면 그 위에 묶음 이름을 붙인다
function AdminLayout({ tabs, activeTab, onTabChange, children }) {
  return (
    <div className="admin-layout">
      <div className="admin-layout__inner container">
        <nav className="admin-layout__nav" aria-label={adminPage.menuLabel}>
          <p className="admin-layout__eyebrow">{adminPage.eyebrow}</p>
          {tabs.map((tab, index) => (
            <Fragment key={tab.id}>
              {tab.group && tab.group !== tabs[index - 1]?.group && (
                <p className="admin-layout__group">{tab.group}</p>
              )}
              <button
                type="button"
                className={`admin-layout__tab ${tab.id === activeTab ? 'admin-layout__tab--active' : ''}`}
                aria-current={tab.id === activeTab ? 'page' : undefined}
                onClick={() => onTabChange(tab.id)}
              >
                {tab.label}
                {tab.badge > 0 && (
                  <span className="admin-layout__tab-badge" aria-label={tab.badgeLabel}>
                    {tab.badge}
                  </span>
                )}
              </button>
            </Fragment>
          ))}
        </nav>
        <div className="admin-layout__content">{children}</div>
      </div>
    </div>
  )
}

export default AdminLayout
