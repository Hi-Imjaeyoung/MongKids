import PageHero from '../common/PageHero'
import { programsHero } from '../../data/programContent'

// 전체 안내(SCR-003) 소개. 상담 버튼은 두지 않는다 (2026-11-05)
function ProgramsHero() {
  return <PageHero {...programsHero} titleId="programs-title" />
}

export default ProgramsHero
