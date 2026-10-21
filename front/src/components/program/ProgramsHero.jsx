import Button from '../common/Button'
import PageHero from '../common/PageHero'
import { ctaButtons } from '../../data/homeContent'
import { programsHero } from '../../data/programContent'

// 전체 안내(SCR-003) 소개 + 상담 버튼 2개
function ProgramsHero({ onInstitutionConsultClick, onPersonalConsultClick }) {
  return (
    <PageHero {...programsHero} titleId="programs-title">
      <Button variant="yellow" size="lg" onClick={onInstitutionConsultClick}>
        {ctaButtons.institution.label}
      </Button>
      <Button variant="blue" size="lg" onClick={onPersonalConsultClick}>
        {ctaButtons.private.label}
      </Button>
    </PageHero>
  )
}

export default ProgramsHero
