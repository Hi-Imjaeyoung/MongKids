import { highlights } from '../../data/homeContent'
import './StatsBar.css'

function StatsBar() {
  return (
    <section className="stats container" aria-label="몽키즈 한눈에 보기">
      <ul className="stats__list">
        {highlights.map((item) => (
          <li key={item.label} className="stats__item">
            <span className="stats__icon" aria-hidden="true">{item.icon}</span>
            <div>
              <strong className="stats__value">{item.value}</strong>
              <span className="stats__label">{item.label}</span>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}

export default StatsBar
