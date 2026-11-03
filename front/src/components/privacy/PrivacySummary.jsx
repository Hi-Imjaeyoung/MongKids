import { effectiveDate, privacySummary } from '../../data/privacyContent'
import './PrivacySummary.css'

// 개인정보처리방침 시행일과 한눈에 보기 요약 카드 (SCR-023)
function PrivacySummary() {
  return (
    <section className="privacy-summary container" aria-labelledby="privacy-summary-title">
      <p className="privacy-summary__date">
        {privacySummary.dateLabel} <strong>{effectiveDate}</strong>
      </p>
      <h2 id="privacy-summary-title" className="privacy-summary__title">
        {privacySummary.title}
      </h2>
      <ul className="privacy-summary__list">
        {privacySummary.items.map((item) => (
          <li
            key={item.id}
            className={`privacy-summary__card ${item.tone ? `privacy-summary__card--${item.tone}` : ''}`}
          >
            <span className="privacy-summary__icon" aria-hidden="true">
              {item.icon}
            </span>
            <div>
              <strong className="privacy-summary__card-title">{item.title}</strong>
              <p className="privacy-summary__card-text">{item.text}</p>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}

export default PrivacySummary
