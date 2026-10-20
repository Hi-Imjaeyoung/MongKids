import { footer } from '../../data/homeContent'
import './Footer.css'

function Footer() {
  return (
    <footer className="footer">
      <div className="footer__inner container">
        <div className="footer__brand">
          <strong className="footer__name">{footer.brand}</strong>
          <p className="footer__slogan">{footer.slogan}</p>
          <p className="footer__note">{footer.area}</p>
        </div>
        <div className="footer__info">
          <p className="footer__sns">
            공식 인스타그램{' '}
            <a href={footer.instagram.href} target="_blank" rel="noopener noreferrer">
              {footer.instagram.label} ↗
            </a>
          </p>
          <p className="footer__note">{footer.pending}</p>
        </div>
      </div>
    </footer>
  )
}

export default Footer
