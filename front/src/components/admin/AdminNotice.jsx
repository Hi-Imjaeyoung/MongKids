import './AdminNotice.css'

// 관리자 화면 안내 문구. inline이면 목록 자리(로딩·오류·빈 목록)에, 아니면 화면 가운데에 보여 준다
function AdminNotice({ message, inline = false, error = false, children }) {
  return (
    <div className={`admin-notice ${inline ? 'admin-notice--inline' : ''}`} role={error ? 'alert' : undefined}>
      <p className={`admin-notice__message ${error ? 'admin-notice__message--error' : ''}`}>{message}</p>
      {children && <div className="admin-notice__actions">{children}</div>}
    </div>
  )
}

export default AdminNotice
