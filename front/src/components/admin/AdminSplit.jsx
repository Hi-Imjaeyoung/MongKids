import './AdminSplit.css'

// 목록(main)과 상세(side)를 나란히, 좁은 화면에서는 위아래로 놓는다
function AdminSplit({ main, side }) {
  return (
    <div className="admin-split">
      <div className="admin-split__main">{main}</div>
      <div className="admin-split__side">{side}</div>
    </div>
  )
}

export default AdminSplit
