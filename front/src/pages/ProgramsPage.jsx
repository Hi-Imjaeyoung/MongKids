import ConsultCta from '../components/common/ConsultCta'
import ProgramGroup from '../components/program/ProgramGroup'
import ProgramProcess from '../components/program/ProgramProcess'
import ProgramsHero from '../components/program/ProgramsHero'
import { consultLabels } from '../data/homeContent'
import { programGroups, programs, programsCta, programsProcess } from '../data/programContent'

// 체육/행사 프로그램 전체 안내 (SCR-003). 운동회·특별 프로그램은 #special 앵커로도 들어온다
function ProgramsPage({ onInstitutionConsultClick, onPersonalConsultClick }) {
  return (
    <>
      <ProgramsHero
        onInstitutionConsultClick={onInstitutionConsultClick}
        onPersonalConsultClick={onPersonalConsultClick}
      />
      {programGroups.map((group) => (
        <ProgramGroup
          key={group.id}
          group={group}
          programs={programs.filter((program) => program.group === group.id)}
        />
      ))}
      <ProgramProcess
        heading={programsProcess}
        steps={programsProcess.steps}
        note={programsProcess.note}
        titleId="programs-process-title"
      />
      <ConsultCta
        titleLines={[programsCta.title]}
        description={programsCta.description}
        titleId="programs-cta-title"
        labels={consultLabels}
        onInstitutionConsultClick={onInstitutionConsultClick}
        onPersonalConsultClick={onPersonalConsultClick}
      />
    </>
  )
}

export default ProgramsPage
