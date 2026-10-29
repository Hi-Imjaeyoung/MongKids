import { locationFail } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import AttendanceScreen from './AttendanceScreen'
import './LocationResult.css'

// ② 위치 확인 실패. reason: 'outOfRange'(반경 밖, distance·radius 있음) | 'unavailable' | 'timeout'
function LocationFail({ classSession, reason, distance, radius, retrying, onRetry, onManual, onGuide, onBack }) {
  const copy = locationFail[reason]
  const message =
    reason === 'outOfRange' ? copy.message(classSession.institutionName, distance, radius) : copy.message

  return (
    <AttendanceScreen
      title={locationFail.title}
      onBack={onBack}
      footer={
        <>
          <AttendanceAction onClick={onRetry} disabled={retrying}>
            {locationFail.retry}
          </AttendanceAction>
          <AttendanceAction variant="outline" onClick={onManual} disabled={retrying}>
            {locationFail.manual}
          </AttendanceAction>
        </>
      }
    >
      <div className="location-result">
        <div className="location-result__icon location-result__icon--warn">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
            <path d="M12 21s-7-6.2-7-11a7 7 0 0 1 14 0c0 4.8-7 11-7 11z" />
            <path d="M12 7v4" />
            <path d="M12 14h.01" />
          </svg>
        </div>
        <h2 className="location-result__heading">{copy.heading}</h2>
        <p className="location-result__message" role="status">
          {message}
        </p>
      </div>

      <section className="location-tips">
        <h3 className="location-tips__title">{locationFail.tipsTitle}</h3>
        <ul className="location-tips__list">
          {locationFail.tips.map((tip) => (
            <li key={tip}>{tip}</li>
          ))}
        </ul>
      </section>

      {reason !== 'outOfRange' && (
        <button type="button" className="location-result__link" onClick={onGuide}>
          {locationFail.guide}
        </button>
      )}
    </AttendanceScreen>
  )
}

export default LocationFail
