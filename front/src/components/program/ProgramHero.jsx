import Button from '../common/Button'
import PageHero from '../common/PageHero'
import { programDetail } from '../../data/programContent'

// 프로그램 상세 소개. 상담 버튼은 두지 않고, 운동회·체육대회만 견적 신청(SCR-011) 버튼을 둔다 (2026-11-05)
function ProgramHero({ program, onQuoteClick }) {
  return (
    <PageHero
      eyebrow={programDetail.eyebrow}
      tag={program.tag}
      title={program.name}
      description={program.blurb}
      titleId="program-title"
    >
      {program.quote && (
        <Button variant="yellow" size="lg" onClick={onQuoteClick}>
          {programDetail.quoteButton}
        </Button>
      )}
    </PageHero>
  )
}

export default ProgramHero
