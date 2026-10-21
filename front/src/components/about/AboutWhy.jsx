import SectionHeading from '../common/SectionHeading'
import { aboutWhy } from '../../data/aboutContent'
import './AboutWhy.css'

const icons = {
  substitute: (
    <>
      <circle cx="9" cy="8" r="3.5" />
      <path d="M2.5 20a6.5 6.5 0 0 1 13 0" />
      <path d="M17 4.5a3.5 3.5 0 0 1 0 7" />
      <path d="M18.5 14.5A6.5 6.5 0 0 1 21.5 20" />
    </>
  ),
  safety: (
    <>
      <path d="M12 3l8 3v6c0 4.5-3.4 8.3-8 9-4.6-.7-8-4.5-8-9V6l8-3z" />
      <path d="M8.5 12l2.5 2.5 4.5-5" />
    </>
  ),
  manage: (
    <>
      <rect x="5" y="4" width="14" height="17" rx="2" />
      <path d="M9 4V3h6v1" />
      <path d="M9 11h6M9 15h4" />
    </>
  ),
}

function AboutWhy() {
  return (
    <section className="about-why" aria-labelledby="about-why-title">
      <div className="about-why__inner container">
        <SectionHeading {...aboutWhy} titleId="about-why-title" align="left" />
        <div className="about-why__list">
          {aboutWhy.items.map((item) => (
            <div key={item.id} className={`about-why__item about-why__item--${item.theme}`}>
              <span className="about-why__icon">
                <svg
                  width="26"
                  height="26"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  {icons[item.id]}
                </svg>
              </span>
              <div>
                <h3 className="about-why__title">{item.title}</h3>
                <p className="about-why__description">{item.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

export default AboutWhy
