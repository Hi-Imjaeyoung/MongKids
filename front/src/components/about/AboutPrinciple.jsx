import { Link } from 'react-router-dom'
import ImagePlaceholder from '../common/ImagePlaceholder'
import ChevronIcon from '../common/ChevronIcon'
import './AboutPrinciple.css'

// 수업 원칙별 상세 섹션 (강사 교육 / 연령별 커리큘럼 / 새로운 활동 경험).
// 사진(photoLabel)이 있으면 사진 + 글, 없으면 글 + 커리큘럼 예시 패널로 배치한다
function AboutPrinciple({ principle }) {
  const titleId = `${principle.id}-title`
  const hasPhoto = Boolean(principle.photoLabel)

  return (
    <section
      id={principle.id}
      className={`about-principle about-principle--${principle.theme} ${hasPhoto ? '' : 'about-principle--panel'} container`}
      aria-labelledby={titleId}
    >
      <div className="about-principle__head">
        <p className="about-principle__label">
          {principle.number} · {principle.label}
        </p>
        <h2 id={titleId} className="about-principle__title">
          {principle.title}
        </h2>
        <p className="about-principle__summary">{principle.summary}</p>
      </div>

      <div className="about-principle__media">
        {hasPhoto ? (
          <div className="about-principle__photo">
            <ImagePlaceholder label={principle.photoLabel} />
          </div>
        ) : (
          <div className="about-principle__curriculum">
            <h3 className="about-principle__curriculum-title">{principle.curriculum.title}</h3>
            <ul className="about-principle__months">
              {principle.curriculum.rows.map((row, index) => (
                <li key={index} className="about-principle__month">
                  <strong>{row.month}</strong>
                  <span>{row.topic}</span>
                </li>
              ))}
            </ul>
            <Link to={principle.curriculum.link.to} className="about-principle__link">
              {principle.curriculum.link.label}
              <ChevronIcon />
            </Link>
          </div>
        )}
      </div>

      <div className="about-principle__extra">
        {principle.checks && (
          <ul className="about-principle__checks">
            {principle.checks.map((check) => (
              <li key={check} className="about-principle__check">
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.4"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  <path d="M5 12l5 5 9-10" />
                </svg>
                {check}
              </li>
            ))}
          </ul>
        )}
        {principle.ageGroups && (
          <ul className="about-principle__ages">
            {principle.ageGroups.map((age) => (
              <li key={age} className="about-principle__age">
                {age}
              </li>
            ))}
          </ul>
        )}
        {principle.examples && (
          <dl className="about-principle__examples">
            {principle.examples.map((example) => (
              <div key={example.label} className="about-principle__example">
                <dt>{example.label}</dt>
                <dd>{example.value}</dd>
              </div>
            ))}
          </dl>
        )}
      </div>
    </section>
  )
}

export default AboutPrinciple
