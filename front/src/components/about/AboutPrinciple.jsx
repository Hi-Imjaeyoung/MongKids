import { Fragment } from 'react'
import { Link } from 'react-router-dom'
import ChevronIcon from '../common/ChevronIcon'
import AboutAgeCards from './AboutAgeCards'
import AboutCurriculum from './AboutCurriculum'
import './AboutPrinciple.css'

// 수업 원칙별 상세 섹션 (강사 교육 / 연령별 커리큘럼 / 새로운 활동 경험).
// 사진(photoLabel)이 있으면 사진 + 글, 없으면 글 + 커리큘럼 예시 패널로 배치한다.
// curriculumEdit: 커리큘럼 패널 편집 상태 { savedMonths, canEdit, onSavedMonthsChange } (FR-034)
// 줄을 나눠 보여 준다. mobileFlow면 모바일에서는 줄을 나누지 않고 이어서 흐르게 한다
// (줄 앞 공백은 줄바꿈 뒤에서는 보이지 않고, 줄바꿈을 숨기면 띄어쓰기가 된다)
function Lines({ lines, mobileFlow = false }) {
  return lines.map((line, index) => (
    <Fragment key={line}>
      {index > 0 && (
        <>
          <br className={mobileFlow ? 'about-principle__break' : undefined} />{' '}
        </>
      )}
      {line}
    </Fragment>
  ))
}

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
          {principle.titleLines ? <Lines lines={principle.titleLines} /> : principle.title}
        </h2>
        <p className="about-principle__summary">
          {principle.detailLines ? <Lines lines={principle.detailLines} mobileFlow /> : principle.summary}
        </p>
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
        {principle.steps && (
          <ol className="about-principle__steps" aria-label={principle.stepsLabel}>
            {principle.steps.map((step) => (
              <li key={step.number} className={`about-principle__step about-principle__step--${step.theme}`}>
                <span className="about-principle__step-number" aria-hidden="true">
                  {step.number}
                </span>
                <span className="about-principle__step-text">
                  <strong className="about-principle__step-title">{step.title}</strong>
                  <span className="about-principle__step-description">{step.description}</span>
                </span>
              </li>
            ))}
          </ol>
        )}
        {principle.routine && <p className="about-principle__routine">{principle.routine}</p>}
        {principle.ageGroups && (
          <ul className="about-principle__ages">
            {principle.ageGroups.map((age) => (
              <li key={age.id} className={`about-principle__age about-principle__age--${age.theme}`}>
                {age.label}
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
        {principle.planLink && (
          <Link to={principle.planLink.to} className="about-principle__button">
            {principle.planLink.label}
            <ChevronIcon />
          </Link>
        )}
      </div>

      {principle.ageCards && (
        <div className="about-principle__cards">
          <AboutAgeCards ageGroups={principle.ageGroups} ageCards={principle.ageCards} />
        </div>
      )}
    </section>
  )
}

export default AboutPrinciple
