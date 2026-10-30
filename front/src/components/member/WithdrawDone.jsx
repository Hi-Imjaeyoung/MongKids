import { useEffect } from 'react'
import { withdrawDone } from '../../data/memberContent'
import './WithdrawDone.css'

// 탈퇴 완료 모달 (FR-028). 백엔드가 홈(/?withdrawn=true)으로 돌려보냈을 때 홈 위에 띄운다
function WithdrawDone({ onClose }) {
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [onClose])

  return (
    <div className="withdraw-done" onClick={onClose}>
      <div
        className="withdraw-done__dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="withdraw-done-title"
        onClick={(event) => event.stopPropagation()}
      >
        <span className="withdraw-done__icon" aria-hidden="true">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
            <path d="M5 12l5 5 9-10" />
          </svg>
        </span>
        <h2 id="withdraw-done-title" className="withdraw-done__title">
          {withdrawDone.title}
        </h2>
        <p className="withdraw-done__description">{withdrawDone.description}</p>
        <button type="button" className="withdraw-done__confirm" onClick={onClose} autoFocus>
          {withdrawDone.confirm}
        </button>
      </div>
    </div>
  )
}

export default WithdrawDone
