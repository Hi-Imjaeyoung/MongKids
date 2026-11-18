import { Link } from 'react-router-dom'
import phoneIcon from '../../assets/phoneIcon.png'
import InstagramIcon from '../common/InstagramIcon'
import { floatingActions } from '../../data/homeContent'
import './FloatingMenu.css'

// 검은 전화기 그림(투명 배경)을 마스크로 써서 CSS 색(빨간색)으로 칠한다
const phoneMaskStyle = { WebkitMaskImage: `url(${phoneIcon})`, maskImage: `url(${phoneIcon})` }

function renderIcon(action) {
  if (action.id === 'instagram') return <InstagramIcon />
  if (action.id === 'phone') return <span className="floating-menu__phone" style={phoneMaskStyle} />
  return action.icon
}

function FloatingMenu() {
  const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

  return (
    <aside className="floating-menu" aria-label="빠른 상담">
      {floatingActions.map((action) => {
        const className = `floating-menu__item floating-menu__item--${action.theme}`
        const content = (
          <>
            <span className="floating-menu__icon" aria-hidden="true">
              {renderIcon(action)}
            </span>
            <span className="floating-menu__label">{action.label}</span>
          </>
        )

        // to가 있는 항목(후기 보러가기)은 같은 창에서 화면 이동
        if (action.to) {
          return (
            <Link key={action.id} to={action.to} className={className}>
              {content}
            </Link>
          )
        }

        // tel이 있는 항목(전화 문의)은 같은 창에서 전화 앱을 연다
        if (action.tel) {
          return (
            <a key={action.id} href={action.tel} className={className}>
              {content}
            </a>
          )
        }

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
