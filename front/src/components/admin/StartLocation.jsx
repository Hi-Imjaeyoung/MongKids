import { locationText, RADIUS_METERS } from '../../data/attendanceAdminContent'
import './AdminForm.css'

// 수업 시작 기록의 거리·정확도 (EMP-FR-011). 반경 밖 거리는 빨갛게, 위치가 없으면 "위치 없음".
// detail이면 위치 없음에 설명을 붙인다
function StartLocation({ start, detail = false }) {
  if (start.distanceMeters === null || start.distanceMeters === undefined) {
    return (
      <>
        <span className="admin-warn-text">{locationText.none}</span>
        {detail && <span>{locationText.noneDetail}</span>}
      </>
    )
  }
  const outside = start.distanceMeters > RADIUS_METERS
  return (
    <>
      <span className={outside ? 'admin-warn-text' : undefined}>{locationText.distance(start.distanceMeters)}</span>
      <span>{locationText.radius(RADIUS_METERS)}</span>
      {start.accuracy !== null && start.accuracy !== undefined && <span>{locationText.accuracy(start.accuracy)}</span>}
    </>
  )
}

export default StartLocation
