import SectionHeading from '../common/SectionHeading'
import ProgramCard from './ProgramCard'
import './ProgramGroup.css'

// 전체 안내(SCR-003)의 분류 섹션. 운동회·특별 프로그램은 크림 띠 + 가로형 카드
function ProgramGroup({ group, programs }) {
  const titleId = `program-group-${group.id}-title`
  return (
    <section
      id={group.anchor}
      className={`program-group program-group--${group.id}`}
      aria-labelledby={titleId}
    >
      <div className="program-group__inner container">
        <SectionHeading
          eyebrow={group.eyebrow}
          accent={group.accent}
          title={group.title}
          description={group.description}
          titleId={titleId}
          align="left"
        />
        <div className="program-group__list">
          {programs.map((program) => (
            <ProgramCard key={program.id} program={program} variant={group.id} />
          ))}
        </div>
      </div>
    </section>
  )
}

export default ProgramGroup
