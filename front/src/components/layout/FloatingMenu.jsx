import InstagramIcon from '../common/InstagramIcon'
import { floatingActions } from '../../data/homeContent'
import './FloatingMenu.css'

function FloatingMenu() {
  const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

  return (
    <aside className="floating-menu" aria-label="빠른 상담">
      {floatingActions.map((action) => {
        const className = `floating-menu__item floating-menu__item--${action.theme}`
        const content = (
          <>
            <span className="floating-menu__icon" aria-hidden="true">
              {action.id === 'instagram' ? <InstagramIcon /> : action.icon}
            </span>
            <span className="floating-menu__label">{action.label}</span>
          </>
        )

        // href가 있는 항목(인스타그램)은 외부 링크로 새 창에서 연다
        return action.href ? (
          <a
            key={action.id}
            href={action.href}
            className={className}
            target="_blank"
            rel="noopener noreferrer"
            aria-label={`${action.label} (새 창)`}
          >
            {content}
          </a>
        ) : (
          <button key={action.id} type="button" className={className}>
            {content}
          </button>
        )
      })}
      <button
        type="button"
        className="floating-menu__item floating-menu__item--white"
        onClick={scrollToTop}
      >
        <span className="floating-menu__icon floating-menu__icon--top" aria-hidden="true">⌃</span>
        <span className="floating-menu__label">맨 위로</span>
      </button>
    </aside>
  )
}

export default FloatingMenu
