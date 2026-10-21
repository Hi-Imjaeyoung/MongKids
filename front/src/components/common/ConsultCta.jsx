import Button from './Button'
import './ConsultCta.css'

// 화면 하단 상담 CTA 띠. variant: 'dark'(파란 배경) | 'light'(연파랑 배경)
// titleLines: 데스크톱에서 줄을 나눌 제목 (모바일은 이어서 표시), labels: { institution, personal } 상담 버튼 문구
function ConsultCta({
  variant = 'dark',
  titleLines,
  description,
  titleId,
  labels,
  onInstitutionConsultClick,
  onPersonalConsultClick,
}) {
  return (
    <section className={`consult-cta consult-cta--${variant} container`} aria-labelledby={titleId}>
      <div className="consult-cta__panel">
        <div>
          <h2 id={titleId} className="consult-cta__title">
            {titleLines.map((line, index) => (
              <span key={line} className="consult-cta__title-line">
                {index > 0 && ' '}
                {line}
              </span>
            ))}
          </h2>
          <p className="consult-cta__description">{description}</p>
        </div>
        <div className="consult-cta__actions">
          <Button variant="yellow" size="lg" onClick={onInstitutionConsultClick}>
            {labels.institution}
          </Button>
          <Button variant="blue" size="lg" className="consult-cta__personal" onClick={onPersonalConsultClick}>
            {labels.personal}
          </Button>
        </div>
      </div>
    </section>
  )
}

export default ConsultCta
