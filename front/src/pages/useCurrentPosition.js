import { useCallback, useState } from 'react'

const POSITION_OPTIONS = { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }

// 기기 위치를 한 번 받는다 (EMP-FR-001). 성공하면 { latitude, longitude, accuracy },
// 실패하면 { kind }로 거절한다: 'denied'(권한 거부·위치 꺼짐) | 'unavailable'(못 받음) | 'timeout' | 'unsupported'
// 위치를 못 받은 경우는 서버에 요청하지 않고 화면에서 안내한다
function useCurrentPosition() {
  const [locating, setLocating] = useState(false)

  const locate = useCallback(() => {
    if (!navigator.geolocation) return Promise.reject({ kind: 'unsupported' })
    setLocating(true)
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        ({ coords }) => resolve({ latitude: coords.latitude, longitude: coords.longitude, accuracy: coords.accuracy }),
        (error) => {
          if (error.code === error.PERMISSION_DENIED) reject({ kind: 'denied' })
          else if (error.code === error.TIMEOUT) reject({ kind: 'timeout' })
          else reject({ kind: 'unavailable' })
        },
        POSITION_OPTIONS,
      )
    }).finally(() => setLocating(false))
  }, [])

  return { locate, locating }
}

export default useCurrentPosition
