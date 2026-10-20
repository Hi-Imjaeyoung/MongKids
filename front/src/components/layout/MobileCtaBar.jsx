import Button from '../common/Button'
import { ctaButtons, mobileCta } from '../../data/homeContent'
import './MobileCtaBar.css'

// 640px 이하에서만 보이는 하단 고정 상담 바
function MobileCtaBar({ onInstitutionConsultClick, onPersonalConsultClick }) {
  return (
    <div className="mobile-cta">
      <Button variant="yellow" icon={ctaButtons.institution.icon} onClick={onInstitutionConsultClick}>
        {mobileCta.institution}
      </Button>
      <Button variant="blue" icon={ctaButtons.private.icon} onClick={onPersonalConsultClick}>
        {mobileCta.private}
      </Button>
    </div>
  )
}

export default MobileCtaBar
