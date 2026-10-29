import AttendanceLayout from './AttendanceLayout'

// 근태 화면 안내 (로그인 확인 중·로그인 필요·직원 아님). children은 안내 아래 버튼
function AttendanceNotice({ message, children }) {
  return (
    <AttendanceLayout>
      <div className="attendance-notice">
        <p className="attendance-notice__message">{message}</p>
        {children}
      </div>
    </AttendanceLayout>
  )
}

export default AttendanceNotice
