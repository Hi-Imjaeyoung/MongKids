import { Fragment } from 'react'
import { Link } from 'react-router-dom'
import ChevronIcon from '../common/ChevronIcon'
import AboutAgeCards from './AboutAgeCards'
import AboutCurriculum from './AboutCurriculum'
import './AboutPrinciple.css'

// 수업 원칙별 상세 섹션 (강사 교육 / 연령별 커리큘럼 / 새로운 활동 경험).
// 사진(photoLabel)이 있으면 사진 + 글, 없으면 글 + 커리큘럼 예시 패널로 배치한다.
// curriculumEdit: 커리큘럼 패널 편집 상태 { savedMonths, canEdit, onSavedMonthsChange } (FR-034)
// 세부 수업 버튼 왼쪽 달력 그림 (장식)
function CalendarIcon() {
  return (
    <svg width="44" height="44" viewBox="0 0 40 40" aria-hidden="true">
      <rect x="4" y="7" width="30" height="29" rx="6" fill="#fff" stroke="currentColor" strokeWidth="2.6" />
      <path d="M4 13a6 6 0 0 1 6-6h18a6 6 0 0 1 6 6v3H4z" fill="var(--green)" stroke="currentColor" strokeWidth="2.6" strokeLinejoin="round" />
      <path d="M12 4v6M26 4v6" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" />
      <path
        d="M19 19.5l1.9 3.8 4.1.6-3 2.9.7 4.1-3.7-1.9-3.7 1.9.7-4.1-3-2.9 4.1-.6z"
        fill="var(--yellow)"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
    </svg>
  )
}

// 세부 수업 버튼 반짝임 한 줄: 아래(시작점)는 가늘고 위(끝점)로 갈수록 두꺼워진다
// [x1, y1] 아래 끝, [x2, y2] 위 끝
const SPARKLE_RAYS = [
  [12, 40, 7, 13],
  [22, 37, 27, 9],
  [29, 43, 46, 27],
]

function SparkleRay({ ray: [x1, y1, x2, y2] }) {
  const length = Math.hypot(x2 - x1, y2 - y1)
  const nx = -(y2 - y1) / length
  const ny = (x2 - x1) / length
  const base = 0.8
  const tip = 3.6
  const points = [
    [x1 + nx * base, y1 + ny * base],
    [x2 + nx * tip, y2 + ny * tip],
    [x2 - nx * tip, y2 - ny * tip],
    [x1 - nx * base, y1 - ny * base],
  ]
    .map(([x, y]) => `${x.toFixed(1)},${y.toFixed(1)}`)
    .join(' ')
  return (
    <>
      <polygon points={points} />
      <circle cx={x2} cy={y2} r={tip} />
      <circle cx={x1} cy={y1} r={base} />
    </>
  )
}

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
          <div className="about-principle__plan">
            <Link to={principle.planLink.to} className="about-principle__button">
              <span className="about-principle__button-icon">
                <CalendarIcon />
              </span>
              <span className="about-principle__button-label">{principle.planLink.label}</span>
              <span className="about-principle__button-arrow">
                <ChevronIcon size={24} strokeWidth={6} />
              </span>
            </Link>
            {/* 버튼 오른쪽 위 반짝임 (장식) */}
            <svg className="about-principle__sparkle" width="44" height="41" viewBox="0 0 52 48" fill="currentColor" aria-hidden="true">
              {SPARKLE_RAYS.map((ray) => (
                <SparkleRay key={ray.join('-')} ray={ray} />
              ))}
            </svg>
          </div>
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
