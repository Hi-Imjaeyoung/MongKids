import SectionHeading from '../common/SectionHeading'
import { aboutPrinciples, aboutStandards } from '../../data/aboutContent'
import './AboutStandards.css'

function ArrowDown() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M6 9l6 6 6-6" />
    </svg>
  )
}

// 수업 원칙 카드 3개. "자세히 보기"는 아래 원칙별 상세 섹션으로 이동한다
function AboutStandards() {
  return (
    <section className="about-standards container" aria-labelledby="about-standards-title">
      <div className="about-standards__panel">
        <SectionHeading {...aboutStandards} titleId="about-standards-title" align="left" />
        <div className="about-standards__list">
          {aboutPrinciples.map((principle) => (
            <a
              key={principle.id}
              href={`#${principle.id}`}
              className={`about-standard about-standard--${principle.theme}`}
            >
              <span className="about-standard__number">{principle.number}</span>
              <span className="about-standard__body">
                <span className="about-standard__name">{principle.name}</span>
                <span className="about-standard__summary">{principle.summary}</span>
                <span className="about-standard__more">
                  {aboutStandards.more}
                  <ArrowDown />
                </span>
              </span>
              <span className="about-standard__arrow">
                <ArrowDown />
              </span>
            </a>
          ))}
        </div>
      </div>
    </section>
  )
}

export default AboutStandards
