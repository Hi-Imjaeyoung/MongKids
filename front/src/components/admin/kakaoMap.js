// 카카오맵 JavaScript SDK (ADR-007). 기관 위치 화면(EMP-FR-012)에서만 필요할 때 한 번 불러온다.
// 키는 빌드 환경 변수 VITE_KAKAO_MAP_KEY (브라우저에 보이는 키라 카카오 콘솔에서 도메인으로 제한한다)

const SDK_URL = 'https://dapi.kakao.com/v2/maps/sdk.js'

let loading = null

// kakao.maps를 돌려준다. 키가 없으면 'NO_KEY', 스크립트를 못 불러오면 'LOAD_FAILED' 오류
export function loadKakaoMaps() {
  const key = import.meta.env.VITE_KAKAO_MAP_KEY
  if (!key) return Promise.reject(new Error('NO_KEY'))
  if (window.kakao?.maps?.services) return Promise.resolve(window.kakao.maps)

  loading ??= new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `${SDK_URL}?appkey=${encodeURIComponent(key)}&libraries=services&autoload=false`
    script.async = true
    script.onload = () => window.kakao.maps.load(() => resolve(window.kakao.maps))
    script.onerror = () => {
      loading = null
      script.remove()
      reject(new Error('LOAD_FAILED'))
    }
    document.head.appendChild(script)
  })
  return loading
}

// 주소(도로명·지번)로 좌표를 찾는다. 못 찾으면 null
export function searchAddress(maps, address) {
  return new Promise((resolve) => {
    new maps.services.Geocoder().addressSearch(address, (result, status) => {
      if (status !== maps.services.Status.OK || result.length === 0) {
        resolve(null)
        return
      }
      resolve({ latitude: roundCoordinate(Number(result[0].y)), longitude: roundCoordinate(Number(result[0].x)) })
    })
  })
}

// 소수점 6자리 (약 0.1m)
export function roundCoordinate(value) {
  return Math.round(value * 1e6) / 1e6
}
