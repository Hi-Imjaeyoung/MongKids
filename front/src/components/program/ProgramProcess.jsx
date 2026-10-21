import SectionHeading from '../common/SectionHeading'
import StepList from './StepList'
import './ProgramProcess.css'

// 상담 단계 섹션. tone: 'plain'(흰 배경, 전체 안내) | 'cream'(크림 띠, 상세)
function ProgramProcess({ heading, steps, note, tone = 'plain', titleId }) {
  return (
    <section className={`program-process program-process--${tone}`} aria-labelledby={titleId}>
      <div className="program-process__inner container">
        <SectionHeading {...heading} titleId={titleId} align="left" />
        <StepList steps={steps} variant={tone} />
        {note && <p className="program-process__note">{note}</p>}
      </div>
    </section>
  )
}

export default ProgramProcess
