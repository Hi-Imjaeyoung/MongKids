import { useEffect, useRef } from 'react'
import { institutionAdmin, RADIUS_METERS } from '../../data/attendanceAdminContent'
import { roundCoordinate } from './kakaoMap'
import './InstitutionMap.css'

// 위치가 없을 때 처음 보여 줄 곳 (서울시청)
const DEFAULT_CENTER = { latitude: 37.5665, longitude: 126.978 }

function cssColor(name) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim()
}

// 기관 위치 지도 (EMP-FR-012). 핀과 수업 시작 기준 반경(100m) 원을 보여 주고,
// 핀을 끌거나 지도를 누르면 onMove(latitude, longitude). maps는 kakao.maps
function InstitutionMap({ maps, latitude, longitude, onMove }) {
  const containerRef = useRef(null)
  const objectsRef = useRef(null)
  const onMoveRef = useRef(onMove)

  useEffect(() => {
    onMoveRef.current = onMove
  })

  // 지도·핀·원은 한 번만 만든다
  useEffect(() => {
    const center = new maps.LatLng(DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude)
    const map = new maps.Map(containerRef.current, { center, level: 3 })
    const marker = new maps.Marker({ position: center, draggable: true })
    const blue = cssColor('--blue')
    const circle = new maps.Circle({
      center,
      radius: RADIUS_METERS,
      strokeWeight: 2,
      strokeColor: blue,
      strokeOpacity: 0.9,
      strokeStyle: 'dash',
      fillColor: blue,
      fillOpacity: 0.12,
    })
    map.addControl(new maps.ZoomControl(), maps.ControlPosition.RIGHT)

    const move = (position) => {
      marker.setPosition(position)
      circle.setPosition(position)
      onMoveRef.current(roundCoordinate(position.getLat()), roundCoordinate(position.getLng()))
    }
    maps.event.addListener(marker, 'dragend', () => move(marker.getPosition()))
    maps.event.addListener(map, 'click', (event) => move(event.latLng))

    objectsRef.current = { map, marker, circle }
    return () => {
      marker.setMap(null)
      circle.setMap(null)
      objectsRef.current = null
    }
  }, [maps])

  // 주소로 찾았거나 좌표가 바뀌면 핀과 원을 옮기고 가운데로
  useEffect(() => {
    const objects = objectsRef.current
    if (!objects) return
    const { map, marker, circle } = objects
    if (latitude === null || longitude === null) {
      marker.setMap(null)
      circle.setMap(null)
      return
    }
    const position = new maps.LatLng(latitude, longitude)
    marker.setPosition(position)
    circle.setPosition(position)
    marker.setMap(map)
    circle.setMap(map)
    map.panTo(position)
  }, [maps, latitude, longitude])

  const hasPosition = latitude !== null && longitude !== null
  return (
    <div className="institution-map">
      <div
        ref={containerRef}
        className="institution-map__canvas"
        role="application"
        aria-label={institutionAdmin.mapLabel}
      />
      <span className="institution-map__tip">{hasPosition ? institutionAdmin.mapTip : institutionAdmin.mapEmpty}</span>
    </div>
  )
}

export default InstitutionMap
