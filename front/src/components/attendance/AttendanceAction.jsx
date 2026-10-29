import './AttendanceAction.css'

// 근태 화면의 가로 꽉 찬 버튼. variant: 'primary'(파랑) | 'dark' | 'outline' | 'outline-blue', size: 'lg' | 'md'
function AttendanceAction({ variant = 'primary', size = 'lg', icon, children, ...props }) {
  return (
    <button
      type="button"
      className={`attendance-action attendance-action--${variant} attendance-action--${size}`}
      {...props}
    >
      {icon}
      {children}
    </button>
  )
}

export default AttendanceAction
