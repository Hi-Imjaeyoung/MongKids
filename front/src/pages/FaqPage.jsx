import { useState } from 'react'
import ConsultCta from '../components/common/ConsultCta'
import PageHero from '../components/common/PageHero'
import FaqList from '../components/faq/FaqList'
import { faqCta, faqHero } from '../data/faqContent'
import { consultLabels } from '../data/homeContent'

// 자주 묻는 질문 (SCR-022). 서비스 분류 탭으로 거르고 질문을 펼쳐 본다. 서버 기능 없이 정적 데이터만 쓴다
function FaqPage({ onInstitutionConsultClick, onPersonalConsultClick }) {
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
        labels={consultLabels}
        onInstitutionConsultClick={onInstitutionConsultClick}
        onPersonalConsultClick={onPersonalConsultClick}
      />
    </>
  )
}

export default FaqPage
