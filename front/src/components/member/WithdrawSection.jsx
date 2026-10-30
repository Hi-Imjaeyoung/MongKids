import { withdrawSection } from '../../data/memberContent'
import './WithdrawSection.css'

// 회원 탈퇴 영역 (FR-028). 일반 회원만 버튼, 직원·관리자는 탈퇴할 수 없다는 안내
function WithdrawSection({ role, onWithdrawClick }) {
  const blockedText = withdrawSection.blocked[role]

  return (
    <section className="withdraw-section" aria-labelledby="withdraw-section-title">
      <div className="withdraw-section__text">
        <h2 id="withdraw-section-title" className="withdraw-section__title">
          {withdrawSection.title}
        </h2>
        <p className="withdraw-section__description">{blockedText ?? withdrawSection.description}</p>
      </div>
      {!blockedText && (
        <button type="button" className="withdraw-section__button" onClick={onWithdrawClick}>
          {withdrawSection.button}
        </button>
      )}
    </section>
  )
}

export default WithdrawSection
