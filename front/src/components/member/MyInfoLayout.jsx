import './MyInfoLayout.css'

// 내 정보 화면(SCR-025) 바탕. 회색 배경 위 가운데 좁은 칸
function MyInfoLayout({ children }) {
  return (
    <div className="my-info-layout">
      <div className="my-info-layout__inner">{children}</div>
    </div>
  )
}

export default MyInfoLayout
