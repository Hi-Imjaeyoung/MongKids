import ConsultCta from '../components/common/ConsultCta'
import ProgramGroup from '../components/program/ProgramGroup'
import ProgramProcess from '../components/program/ProgramProcess'
import ProgramsHero from '../components/program/ProgramsHero'
import { consultLabels } from '../data/homeContent'
import { programDetail, programGroups, programs, programsCta, programsProcess } from '../data/programContent'
import { pageMeta } from '../data/seoContent'
import usePageMeta from './usePageMeta'

// 체육/행사 프로그램 전체 안내 (SCR-003). 운동회·특별 프로그램은 #special 앵커로도 들어온다
// 하단 상담 CTA에는 운동회 견적 요청(SCR-011) 버튼도 둔다 (2026-11-19)
function ProgramsPage({ onInstitutionConsultClick, onPersonalConsultClick, onQuoteClick }) {
  usePageMeta(pageMeta.programs.title, pageMeta.programs.description)
  return (
    <>
      <ProgramsHero />
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
        labels={{ ...consultLabels, quote: programDetail.quoteButton }}
        onInstitutionConsultClick={onInstitutionConsultClick}
        onPersonalConsultClick={onPersonalConsultClick}
        onQuoteClick={onQuoteClick}
      />
    </>
  )
}

export default ProgramsPage
