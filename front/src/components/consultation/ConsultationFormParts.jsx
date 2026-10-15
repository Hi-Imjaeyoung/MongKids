// 상담 신청 모달(SCR-009, SCR-010)이 함께 쓰는 입력 조각

// selected가 배열이면 여러 개, 값 하나면 하나만 고르는 칩 묶음
export function ChipGroup({ options, selected, onToggle }) {
  return (
    <div className="consult-modal__chips">
      {options.map((option) => {
        const pressed = Array.isArray(selected) ? selected.includes(option.value) : selected === option.value
        return (
          <button
            key={String(option.value)}
            type="button"
            className={`consult-modal__chip ${pressed ? 'consult-modal__chip--on' : ''}`}
            aria-pressed={pressed}
            onClick={() => onToggle(option.value)}
          >
            {option.label}
          </button>
        )
      })}
    </div>
  )
}

export function FieldError({ message }) {
  if (!message) return null
  return <p className="consult-modal__field-error" role="alert">{message}</p>
}
