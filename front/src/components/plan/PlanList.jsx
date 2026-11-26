import { plansText } from '../../data/plansContent'
import './PlanList.css'

const LockIcon = () => (
  <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <rect x="5" y="11" width="14" height="10" rx="2" />
    <path d="M8 11V8a4 4 0 0 1 8 0v3" />
  </svg>
)

// 수업 계획안 목록 (SCR-027). 연간·월간으로 묶고, 모두 잠김 표시를 붙인다 (연간 포함은 2026-11-26)
function PlanList({ plans, selectedId, onSelect }) {
  const groups = ['annual', 'monthly']
    .map((kind) => ({ kind, items: plans.filter((plan) => plan.kind === kind) }))
    .filter((group) => group.items.length > 0)

  return (
    <nav className="plan-list" aria-label={plansText.listLabel}>
      {groups.map((group) => (
        <div key={group.kind} className="plan-list__group">
          <h2 className="plan-list__group-title">{plansText.groups[group.kind]}</h2>
          <ul className="plan-list__items">
            {group.items.map((plan) => {
              const selected = plan.id === selectedId
              return (
                <li key={plan.id}>
                  <button
                    type="button"
                    className={`plan-list__item ${selected ? 'plan-list__item--selected' : ''}`}
                    aria-pressed={selected}
                    onClick={() => onSelect(plan.id)}
                  >
                    <span className="plan-list__text">
                      <span className="plan-list__title">{plan.title}</span>
                      <span className="plan-list__period">{plan.period}</span>
                    </span>
                    <span className="plan-list__lock">
                      <LockIcon />
                      {plansText.lockedBadge}
                    </span>
                  </button>
                </li>
              )
            })}
          </ul>
        </div>
      ))}
      <p className="plan-list__note">{plansText.listNote}</p>
    </nav>
  )
}

export { LockIcon }
export default PlanList
