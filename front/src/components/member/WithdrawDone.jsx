import Button from '../common/Button'
import { withdrawDone } from '../../data/memberContent'
import './WithdrawDone.css'

// 탈퇴 완료 화면 (FR-028). 백엔드가 /me?withdrawn=true로 돌려보냈을 때
function WithdrawDone() {
  return (
    <div className="withdraw-done">
      <span className="withdraw-done__icon" aria-hidden="true">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
          <path d="M5 12l5 5 9-10" />
        </svg>
      </span>
      <h1 className="withdraw-done__title">{withdrawDone.title}</h1>
      <p className="withdraw-done__description">{withdrawDone.description}</p>
      <Button variant="blue" size="lg" to="/" className="withdraw-done__home">
        {withdrawDone.home}
      </Button>
    </div>
  )
}

export default WithdrawDone
