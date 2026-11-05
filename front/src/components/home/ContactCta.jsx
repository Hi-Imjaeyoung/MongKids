import mascotImage from '../../assets/contact-mascot.png'
import Button from '../common/Button'
import { contactCta, ctaButtons } from '../../data/homeContent'
import './ContactCta.css'

function ContactCta({ onInstitutionConsultClick, onPersonalConsultClick }) {
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
        </div>
      </div>
    </section>
  )
}

export default ContactCta
