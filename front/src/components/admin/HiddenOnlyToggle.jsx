import { reviewAdmin } from '../../data/adminContent'
import './HiddenOnlyToggle.css'

// "숨긴 후기만 보기" 체크박스 (FR-021)
function HiddenOnlyToggle({ checked, onChange }) {
  return (
    <label className="hidden-only-toggle">
      <input
        type="checkbox"
        className="hidden-only-toggle__input"
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
      />
      {reviewAdmin.hiddenOnly}
    </label>
  )
}

export default HiddenOnlyToggle
