import './StepList.css'

// STEP 01~04 단계 카드. variant: 'plain'(흰 배경 위) | 'cream'(크림 배경 위)
function StepList({ steps, variant = 'plain' }) {
  return (
    <ol className={`step-list step-list--${variant}`}>
      {steps.map((step, index) => (
        <li key={step.title} className="step-list__item">
          <p className="step-list__number">STEP {String(index + 1).padStart(2, '0')}</p>
          <p className="step-list__title">{step.title}</p>
          <p className="step-list__description">{step.description}</p>
        </li>
      ))}
    </ol>
  )
}

export default StepList
