import AboutCurriculum from './AboutCurriculum'
import './AboutPrinciple.css'

// 수업 원칙별 상세 섹션 (강사 교육 / 연령별 커리큘럼 / 새로운 활동 경험).
// 사진(photoLabel)이 있으면 사진 + 글, 없으면 글 + 커리큘럼 예시 패널로 배치한다.
// curriculumEdit: 커리큘럼 패널 편집 상태 { savedMonths, canEdit, onSavedMonthsChange } (FR-034)
function AboutPrinciple({ principle, curriculumEdit }) {
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
            <img src={principle.photo} alt={principle.photoLabel} />
          </div>
        ) : (
          <AboutCurriculum curriculum={principle.curriculum} {...curriculumEdit} />
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
