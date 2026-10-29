import { useState } from 'react'
import { locationGuide } from '../../data/attendanceContent'
import AttendanceAction from './AttendanceAction'
import AttendanceScreen from './AttendanceScreen'
import './LocationGuide.css'

// ③ 위치 권한 거부·위치 꺼짐 안내. 기기·브라우저별 위치 켜는 방법을 탭으로 보여 준다
// unsupported: 브라우저가 위치 확인을 지원하지 않음. 다시 시도 대신 수동 등록을 권한다
function LocationGuide({ unsupported = false, retrying, onRetry, onManual, onBack }) {
  const [deviceId, setDeviceId] = useState(locationGuide.devices[0].id)
  const device = locationGuide.devices.find((item) => item.id === deviceId)

  return (
    <AttendanceScreen
      title={locationGuide.title}
      onBack={onBack}
      footer={
        <>
          {!unsupported && (
            <AttendanceAction onClick={onRetry} disabled={retrying}>
              {locationGuide.retry}
            </AttendanceAction>
          )}
          <AttendanceAction variant={unsupported ? 'primary' : 'outline'} onClick={onManual} disabled={retrying}>
            {locationGuide.manual}
          </AttendanceAction>
        </>
      }
    >
      <div className="location-guide__intro">
        <h2 className="location-guide__heading">{locationGuide.heading}</h2>
        <p className="location-guide__message">{unsupported ? locationGuide.unsupported : locationGuide.message}</p>
      </div>

      {!unsupported && (
        <>
          <div className="location-guide__tabs" role="tablist" aria-label={locationGuide.tabsLabel}>
            {locationGuide.devices.map((item) => (
              <button
                key={item.id}
                type="button"
                role="tab"
                aria-selected={item.id === deviceId}
                className={`location-guide__tab ${item.id === deviceId ? 'location-guide__tab--active' : ''}`}
                onClick={() => setDeviceId(item.id)}
              >
                {item.label}
              </button>
            ))}
          </div>
          <ol className="location-guide__steps" role="tabpanel">
            {device.steps.map((step) => (
              <li key={step}>{step}</li>
            ))}
          </ol>
          <p className="attendance-hint">{locationGuide.after}</p>
        </>
      )}
    </AttendanceScreen>
  )
}

export default LocationGuide
