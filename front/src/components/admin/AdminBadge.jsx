import './AdminBadge.css'

// 유형·상태·권한 표시. tone: 'yellow' | 'blue' | 'green' | 'gray' | 'dark', shape: 'pill' | 'tag'
function AdminBadge({ tone = 'gray', shape = 'pill', children }) {
  return <span className={`admin-badge admin-badge--${tone} admin-badge--${shape}`}>{children}</span>
}

export default AdminBadge
