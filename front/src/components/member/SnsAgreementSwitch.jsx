import { formatDate } from '../news/postFormat'
import { snsAgreement } from '../../data/memberContent'
import './SnsAgreementSwitch.css'

// SNS 수신 동의 스위치 (FR-019). 바꾸는 동안에는 누를 수 없다
function SnsAgreementSwitch({ agreed, agreedAt, saving = false, error = null, onChange }) {
  return (
    <div className="sns-switch">
      <div className="sns-switch__row">
        <span className="sns-switch__text">
          {agreed ? snsAgreement.agreed(formatDate(agreedAt)) : snsAgreement.notAgreed}
        </span>
        <button
          type="button"
          role="switch"
          aria-checked={agreed}
          aria-labelledby="my-info-sns-label"
          className={`sns-switch__button ${agreed ? 'sns-switch__button--on' : ''}`}
          disabled={saving}
          onClick={() => onChange(!agreed)}
        >
          <span className="sns-switch__track" aria-hidden="true">
            <span className="sns-switch__knob" />
          </span>
          {agreed ? snsAgreement.on : snsAgreement.off}
        </button>
      </div>
      {error && (
        <p className="sns-switch__error" role="alert">
          {error}
        </p>
      )}
    </div>
  )
}

export default SnsAgreementSwitch
