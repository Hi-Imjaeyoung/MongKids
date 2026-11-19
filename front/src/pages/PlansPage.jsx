import { useState } from 'react'
import PageHero from '../components/common/PageHero'
import PlanList from '../components/plan/PlanList'
import PlanViewer from '../components/plan/PlanViewer'
import { plans, plansHero } from '../data/plansContent'
import { pageMeta } from '../data/seoContent'
import usePageMeta from './usePageMeta'

// 수업 계획안 (SCR-027). ABOUT 월별 커리큘럼의 "세부 수업 보러가기"에서 들어온다.
// 연간 계획안은 누구나, 월간 계획안은 인증된 기관 회원만 본다(FR-032). 기관 인증·계획안 API 전까지는
// 월간을 모두에게 잠김(블러 미리보기)으로 보여 준다
function PlansPage({ member, onLoginClick }) {
  usePageMeta(pageMeta.plans.title, pageMeta.plans.description)
  const firstAnnual = plans.find((plan) => plan.kind === 'annual') ?? plans[0]
  const [selectedId, setSelectedId] = useState(firstAnnual.id)
  const selected = plans.find((plan) => plan.id === selectedId) ?? firstAnnual

  return (
    <>
      <PageHero {...plansHero} titleId="plans-page-title" />
      <section className="plans-page container" aria-labelledby="plans-page-title">
        <div className="plans-page__list">
          <PlanList plans={plans} selectedId={selected.id} onSelect={setSelectedId} />
        </div>
        <div className="plans-page__viewer">
          <PlanViewer
            key={selected.id}
            plan={selected}
            member={member}
            onLoginClick={onLoginClick}
            onShowAnnual={() => setSelectedId(firstAnnual.id)}
          />
        </div>
      </section>
    </>
  )
}

export default PlansPage
