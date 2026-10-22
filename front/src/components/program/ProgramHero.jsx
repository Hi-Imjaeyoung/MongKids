import Button from '../common/Button'
import PageHero from '../common/PageHero'
import { ctaButtons } from '../../data/homeContent'
import { programDetail } from '../../data/programContent'

// 프로그램 상세 소개: 상담 버튼
// 1:1 가정 방문체육만 개인 상담(SCR-010), 나머지는 기관 문의(SCR-009)
function ProgramHero({ program, onInstitutionConsultClick, onPersonalConsultClick }) {
  return (
    <PageHero
      eyebrow={programDetail.eyebrow}
      tag={program.tag}
      title={program.name}
      description={program.blurb}
      titleId="program-title"
    >
      {program.consult === 'personal' ? (
        <Button variant="blue" size="lg" onClick={onPersonalConsultClick}>
          {ctaButtons.private.label}
        </Button>
      ) : (
        <Button variant="yellow" size="lg" onClick={onInstitutionConsultClick}>
          {ctaButtons.institution.label}
        </Button>
      )}
    </PageHero>
  )
}

export default ProgramHero
