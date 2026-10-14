import { useEffect } from 'react'
import { getSocialLoginUrl } from '../../api/auth'
import { loginModal, socialProviders } from '../../data/authContent'
import './LoginModal.css'

// 로그인·회원가입 모달 (SCR-013, FR-018)
function LoginModal({ hasError = false, onClose }) {
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
    <div className="login-modal" onClick={onClose}>
      <div
        className="login-modal__dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="login-modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <button type="button" className="login-modal__close" aria-label="닫기" onClick={onClose}>
          ✕
        </button>

        <span className="login-modal__mascot" aria-hidden="true">🐵</span>
        <h2 id="login-modal-title" className="login-modal__title">{loginModal.title}</h2>
        <p className="login-modal__description">
          {loginModal.description.map((line) => (
            <span key={line}>{line}</span>
          ))}
        </p>

        {hasError && (
          <p className="login-modal__error" role="alert">{loginModal.errorMessage}</p>
        )}

        <div className="login-modal__providers">
          {socialProviders.map((provider) => (
            <a
              key={provider.id}
              href={getSocialLoginUrl(provider.id)}
              className={`login-modal__provider login-modal__provider--${provider.id}`}
            >
              <span className="login-modal__provider-icon" aria-hidden="true">{provider.icon}</span>
              {provider.label}
            </a>
          ))}
        </div>

        <p className="login-modal__notice">{loginModal.notice}</p>
      </div>
    </div>
  )
}

export default LoginModal
