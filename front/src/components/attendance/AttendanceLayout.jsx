import './AttendanceLayout.css'

// 근태 화면 바탕. 모바일 우선(EMP-NFR-001)이라 데스크톱에서도 휴대폰 폭으로 가운데에 둔다
function AttendanceLayout({ children }) {
  return (
    <div className="attendance-layout">
      <div className="attendance-layout__inner">{children}</div>
    </div>
  )
}

export default AttendanceLayout
