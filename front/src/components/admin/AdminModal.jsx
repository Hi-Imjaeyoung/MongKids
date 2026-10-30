import { useEffect, useRef } from 'react'
import './AdminModal.css'

// 관리자 화면 상세 모달. 배경·Esc·닫기 버튼으로 onClose. 열면 닫기 버튼에 포커스, 닫으면 연 버튼으로 돌려준다.
// wide면 지도처럼 넓은 내용용으로 폭을 넓힌다
function AdminModal({ label, closeLabel, wide = false, onClose, children }) {
  const closeRef = useRef(null)

  useEffect(() => {
    const opener = document.activeElement
    closeRef.current?.focus()
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
      opener?.focus?.()
    }
  }, [onClose])

  return (
    <div className="admin-modal" onClick={onClose}>
      <div
        className={`admin-modal__dialog ${wide ? 'admin-modal__dialog--wide' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-label={label}
        onClick={(event) => event.stopPropagation()}
      >
        <button ref={closeRef} type="button" className="admin-modal__close" aria-label={closeLabel} onClick={onClose}>
          ✕
        </button>
        {children}
      </div>
    </div>
  )
}

export default AdminModal
