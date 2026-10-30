import { useEffect, useState } from 'react'
import { withdrawConfirm } from '../../data/memberContent'
import './WithdrawConfirmModal.css'

// 회원 탈퇴 확인 창 (FR-028). 데스크톱은 모달, 모바일은 전체 화면.
// 안내 확인을 체크해야 탈퇴 버튼이 켜지고, 누르면 가입한 소셜 계정으로 본인 확인을 시작한다(onConfirm)
function WithdrawConfirmModal({ providerLabel, onConfirm, onClose }) {
  const [checked, setChecked] = useState(false)

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
    <div className="withdraw-modal" onClick={onClose}>
      <div
        className="withdraw-modal__dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="withdraw-modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="withdraw-modal__header">
          <h2 id="withdraw-modal-title" className="withdraw-modal__title">
            {withdrawConfirm.title}
          </h2>
          <button type="button" className="withdraw-modal__close" aria-label={withdrawConfirm.close} onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="withdraw-modal__body">
          <p className="withdraw-modal__lead">{withdrawConfirm.lead}</p>
          <ul className="withdraw-modal__notices">
            {withdrawConfirm.notices(providerLabel).map((parts, index) => (
              <li key={index}>
                {parts.map((part, partIndex) =>
                  typeof part === 'string' ? part : <strong key={partIndex}>{part.strong}</strong>,
                )}
              </li>
            ))}
          </ul>
          <label className="withdraw-modal__check">
            <input type="checkbox" checked={checked} onChange={(event) => setChecked(event.target.checked)} />
            {withdrawConfirm.check}
          </label>
        </div>

        <div className="withdraw-modal__actions">
          <button type="button" className="withdraw-modal__cancel" onClick={onClose}>
            {withdrawConfirm.cancel}
          </button>
          <button type="button" className="withdraw-modal__submit" disabled={!checked} onClick={onConfirm}>
            {withdrawConfirm.submit(providerLabel)}
          </button>
        </div>
      </div>
    </div>
  )
}

export default WithdrawConfirmModal
