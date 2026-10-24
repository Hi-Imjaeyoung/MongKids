import './FilterChips.css'

// 하나만 고르는 필터 칩. options: [{ id, label }]
function FilterChips({ label, options, value, onChange }) {
  return (
    <div className="filter-chips" role="group" aria-label={label}>
      {options.map((option) => (
        <button
          key={option.id}
          type="button"
          className={`filter-chips__chip ${option.id === value ? 'filter-chips__chip--active' : ''}`}
          aria-pressed={option.id === value}
          onClick={() => onChange(option.id)}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}

export default FilterChips
