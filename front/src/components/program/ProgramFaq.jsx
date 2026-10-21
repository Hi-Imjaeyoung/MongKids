import { Link } from 'react-router-dom'
import SectionHeading from '../common/SectionHeading'
import FaqAccordion from '../common/FaqAccordion'
import ChevronIcon from '../common/ChevronIcon'
import { faqsByCategory } from '../../data/faqContent'
import { programDetail } from '../../data/programContent'
import './ProgramFaq.css'

// 이 프로그램 관련 질문: 분류별 질문 + 공통 질문
function ProgramFaq({ programId }) {
  const items = [...faqsByCategory(programId), ...faqsByCategory('common')]
  return (
    <section id={programDetail.faqAnchor} className="program-faq container" aria-labelledby="program-faq-title">
      <SectionHeading {...programDetail.faq} titleId="program-faq-title" align="left" />
      {/* 프로그램을 바꾸면 펼친 상태를 새로 시작한다 */}
      <FaqAccordion key={programId} items={items} />
      <Link to="/faq" className="program-faq__more">
        {programDetail.faq.more}
        <ChevronIcon />
      </Link>
    </section>
  )
}

export default ProgramFaq
