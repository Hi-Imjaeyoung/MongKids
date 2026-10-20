import { Link } from 'react-router-dom'
import './Button.css'

// variant: 'yellow' | 'blue' | 'ghost', size: 'md' | 'lg'
// to가 있으면 같은 모양의 링크(react-router Link)로 렌더링한다.
function Button({ variant = 'yellow', size = 'md', icon, to, className = '', children, ...props }) {
  const classNames = `btn btn--${variant} btn--${size} ${className}`.trim()
  const content = (
    <>
      {icon && <span className="btn__icon" aria-hidden="true">{icon}</span>}
      {children}
    </>
  )

  if (to) {
    return (
      <Link to={to} className={classNames} {...props}>
        {content}
      </Link>
    )
  }

  return (
    <button type="button" className={classNames} {...props}>
      {content}
    </button>
  )
}

export default Button
