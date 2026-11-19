import './AboutAgeCards.css'

// "연령별 맞춤 커리큘럼" 아래 연령별 카드 (SCR-002).
// ageGroups의 연령 이름·색(theme)을 같은 id의 카드 내용과 합쳐 보여 준다
function AboutAgeCards({ ageGroups, ageCards }) {
  return (
    <ul className="about-age-cards">
      {ageCards.map((card) => {
        const group = ageGroups.find((item) => item.id === card.id)
        return (
          <li key={card.id} className={`about-age-card about-age-card--${group?.theme ?? 'blue'}`}>
            <span className="about-age-card__badge">{group?.label}</span>
            <h3 className="about-age-card__title">{card.title}</h3>
            <p className="about-age-card__keywords">{card.keywords}</p>
            <p className="about-age-card__description">{card.description}</p>
          </li>
        )
      })}
    </ul>
  )
}

export default AboutAgeCards
