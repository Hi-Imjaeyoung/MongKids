import { Navigate, useParams } from 'react-router-dom'
import OtherPrograms from '../components/program/OtherPrograms'
import ProgramBreadcrumb from '../components/program/ProgramBreadcrumb'
import ProgramGuide from '../components/program/ProgramGuide'
import ProgramHero from '../components/program/ProgramHero'
import ProgramProcess from '../components/program/ProgramProcess'
import { findProgram, programDetail } from '../data/programContent'

// 프로그램 상세 (SCR-016~020, SCR-026). 여섯 화면이 같은 구성이고 내용만 다르다. 없는 id는 전체 안내로 보낸다
function ProgramDetailPage({ onQuoteClick }) {
  const { programId } = useParams()
  const program = findProgram(programId)

  if (!program) return <Navigate to="/programs" replace />

  return (
    <>
      <ProgramBreadcrumb program={program} />
      <ProgramHero program={program} onQuoteClick={onQuoteClick} />
      <ProgramGuide program={program} />
      <ProgramProcess
        heading={programDetail.process}
        steps={programDetail.process.steps}
        tone="cream"
        titleId="program-process-title"
      />
      <OtherPrograms currentId={program.id} />
    </>
  )
}

export default ProgramDetailPage
