import './NewsNotice.css'

// 불러오는 중·빈 목록·오류처럼 본문 대신 보여 주는 안내. children에 버튼 등을 둔다.
// standalone: 화면 전체를 이 안내로 채울 때 (본문 폭·여백을 함께 준다)
function NewsNotice({ message, tone = 'default', standalone = false, children }) {
  const notice = (
    <div className={`news-notice news-notice--${tone}`} role={tone === 'error' ? 'alert' : 'status'}>
      <p className="news-notice__message">{message}</p>
      {children}
    </div>
  )
  return standalone ? <div className="news-notice-page container">{notice}</div> : notice
}

export default NewsNotice
