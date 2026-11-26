import { useEffect, useState } from 'react'
import { deleteClassPlan, getClassPlans } from '../api/classPlans'
import PageHero from '../components/common/PageHero'
import { isAdmin } from '../components/news/postPermission'
import PlanForm from '../components/plan/PlanForm'
import PlanList from '../components/plan/PlanList'
import PlanViewer from '../components/plan/PlanViewer'
import { plansHero, plansText } from '../data/plansContent'
import { pageMeta } from '../data/seoContent'
import usePageMeta from './usePageMeta'

// 서버 응답의 종류(ANNUAL·MONTHLY)를 화면 키(annual·monthly)로 바꾼다
const toViewPlan = (plan) => ({ ...plan, kind: plan.kind.toLowerCase() })

// 수업 계획안 (SCR-027, FR-032, ADR-009). ABOUT 월별 커리큘럼의 "세부 수업 보러가기"에서 들어온다.
// 연간·월간 모두 관리자가 블러 처리한 PDF만 공개한다(원본은 서버가 권한 확인 후 전달, 연간 포함은 2026-11-26).
// 관리자는 이 화면에서 계획안을 추가·수정·삭제한다. form: null(보기) | { plan: null }(추가) | { plan }(수정)
function PlansPage({ member, onLoginClick }) {
  usePageMeta(pageMeta.plans.title, pageMeta.plans.description)
  const canManage = isAdmin(member)
  const [plans, setPlans] = useState(null)
  const [loadError, setLoadError] = useState(false)
  const [selectedId, setSelectedId] = useState(null)
  const [form, setForm] = useState(null)

  // 저장·삭제 뒤 목록을 다시 받고 nextSelectedId를 고른다 (null이면 첫 계획안)
  const reloadPlans = async (nextSelectedId) => {
    try {
      setPlans((await getClassPlans()).map(toViewPlan))
      setLoadError(false)
      setSelectedId(nextSelectedId)
    } catch {
      setLoadError(true)
    }
  }

  useEffect(() => {
    let ignore = false
    getClassPlans()
      .then((loaded) => !ignore && setPlans(loaded.map(toViewPlan)))
      .catch(() => !ignore && setLoadError(true))
    return () => {
      ignore = true
    }
  }, [])

  const list = plans ?? []
  const firstAnnual = list.find((plan) => plan.kind === 'annual')
  const selected = list.find((plan) => plan.id === selectedId) ?? firstAnnual ?? list[0]

  const handleSaved = async (saved) => {
    setForm(null)
    await reloadPlans(saved.id)
  }

  const handleDelete = async (plan) => {
    if (!window.confirm(plansText.admin.deleteConfirm(plan.title))) return
    try {
      await deleteClassPlan(plan.id)
      await reloadPlans(null)
    } catch (deleteError) {
      window.alert(deleteError.message || plansText.admin.deleteError)
    }
  }

  let viewer
  if (form) {
    viewer = (
      <PlanForm key={form.plan?.id ?? 'new'} plan={form.plan} onSaved={handleSaved} onCancel={() => setForm(null)} />
    )
  } else if (loadError) {
    viewer = <p className="plans-page__status" role="alert">{plansText.listError}</p>
  } else if (!plans) {
    viewer = <p className="plans-page__status">{plansText.listLoading}</p>
  } else if (!selected) {
    viewer = <p className="plans-page__status">{plansText.empty}</p>
  } else {
    viewer = (
      <PlanViewer
        key={selected.id}
        plan={selected}
        member={member}
        canManage={canManage}
        onLoginClick={onLoginClick}
        onEdit={() => setForm({ plan: selected })}
        onDelete={() => handleDelete(selected)}
      />
    )
  }

  return (
    <>
      <PageHero {...plansHero} titleId="plans-page-title" />
      <section className="plans-page container" aria-labelledby="plans-page-title">
        <div className="plans-page__list">
          {canManage && (
            <button type="button" className="plans-page__add" disabled={Boolean(form)} onClick={() => setForm({ plan: null })}>
              + {plansText.admin.add}
            </button>
          )}
          {list.length > 0 && (
            <PlanList
              plans={list}
              selectedId={form ? null : selected?.id}
              onSelect={(id) => {
                setForm(null)
                setSelectedId(id)
              }}
            />
          )}
        </div>
        <div className="plans-page__viewer">{viewer}</div>
      </section>
    </>
  )
}

export default PlansPage
