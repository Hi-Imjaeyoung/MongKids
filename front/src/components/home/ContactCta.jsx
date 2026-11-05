import mascotImage from '../../assets/contact-mascot.png'
import Button from '../common/Button'
import { contactCta, ctaButtons } from '../../data/homeContent'
import './ContactCta.css'

// 하단 상담 CTA. 운동회 견적 요청(SCR-011) 버튼도 둔다 (2026-11-05)
function ContactCta({ onInstitutionConsultClick, onPersonalConsultClick, onQuoteClick }) {
  return (
    <section className="contact-cta container" aria-labelledby="contact-cta-title">
      <div className="contact-cta__panel">
        <img className="contact-cta__mascot" src={mascotImage} alt="" />
        <h2 id="contact-cta-title" className="contact-cta__title">{contactCta.title}</h2>
        <p className="contact-cta__description">{contactCta.description}</p>
        <div className="contact-cta__actions">
          <Button variant="yellow" size="lg" icon={ctaButtons.institution.icon} onClick={onInstitutionConsultClick}>
            {ctaButtons.institution.label}
          </Button>
          <Button variant="blue" size="lg" icon={ctaButtons.private.icon} onClick={onPersonalConsultClick}>
            {ctaButtons.private.label}
          </Button>
          <Button variant="ghost" size="lg" icon={ctaButtons.quote.icon} onClick={onQuoteClick}>
            {ctaButtons.quote.label}
          </Button>
        </div>
      </div>
    </section>
  )
}

export default ContactCta
