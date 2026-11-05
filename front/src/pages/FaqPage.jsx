import { useState } from 'react'
import ConsultCta from '../components/common/ConsultCta'
import PageHero from '../components/common/PageHero'
import FaqList from '../components/faq/FaqList'
import { faqCta, faqHero } from '../data/faqContent'
import { consultLabels } from '../data/homeContent'
import { programDetail } from '../data/programContent'

// 자주 묻는 질문 (SCR-022). 서비스 분류 탭으로 거르고 질문을 펼쳐 본다. 서버 기능 없이 정적 데이터만 쓴다
// 하단 상담 CTA에는 운동회 견적 요청(SCR-011) 버튼도 둔다 (2026-11-05)
function FaqPage({ onInstitutionConsultClick, onPersonalConsultClick, onQuoteClick }) {
  const [category, setCategory] = useState('all')

  return (
    <>
      <PageHero {...faqHero} titleId="faq-page-title" />
      <FaqList category={category} onCategoryChange={setCategory} />
      <ConsultCta
        variant="light"
        titleLines={[faqCta.title]}
        description={faqCta.description}
        titleId="faq-cta-title"
        labels={{ ...consultLabels, quote: programDetail.quoteButton }}
        onInstitutionConsultClick={onInstitutionConsultClick}
        onPersonalConsultClick={onPersonalConsultClick}
        onQuoteClick={onQuoteClick}
      />
    </>
  )
}

export default FaqPage
