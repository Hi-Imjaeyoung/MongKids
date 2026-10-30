import { useEffect, useState } from 'react'
import { institutionAdmin } from '../../data/attendanceAdminContent'
import InstitutionMap from './InstitutionMap'
import { loadKakaoMaps, searchAddress } from './kakaoMap'
import './AdminForm.css'
import './InstitutionForm.css'

function toNumber(value) {
  return value === '' || value === null ? null : Number(value)
}

// 기관 등록·수정 (EMP-FR-012). 주소로 찾기 → 지도에서 핀 보정 → 위도·경도 확인 후 저장.
// 지도를 못 불러오면(키 없음·실패) 위도·경도를 직접 입력한다.
// institution이 null이면 새 기관. onSave(body), onDelete(), onCancel(). 부모가 key로 폼을 새로 만든다
function InstitutionForm({ institution, busy, onSave, onDelete, onCancel }) {
  const [form, setForm] = useState(() => ({
    name: institution?.name ?? '',
    address: institution?.address ?? '',
    latitude: institution ? String(institution.latitude) : '',
    longitude: institution ? String(institution.longitude) : '',
  }))
  const [map, setMap] = useState({ status: 'loading', maps: null })
  const [searching, setSearching] = useState(false)
  const [error, setError] = useState('')
  const text = institutionAdmin

  useEffect(() => {
    let ignore = false
    loadKakaoMaps()
      .then((maps) => {
        if (!ignore) setMap({ status: 'ready', maps })
      })
      .catch((loadError) => {
        if (!ignore) setMap({ status: loadError.message === 'NO_KEY' ? 'noKey' : 'error', maps: null })
      })
    return () => {
      ignore = true
    }
  }, [])

  const change = (patch) => {
    setForm((current) => ({ ...current, ...patch }))
    setError('')
  }

  const search = async () => {
    if (!map.maps || !form.address.trim()) return
    setSearching(true)
    const found = await searchAddress(map.maps, form.address.trim())
    setSearching(false)
    if (!found) {
      setError(text.notFound)
      return
    }
    change({ latitude: String(found.latitude), longitude: String(found.longitude) })
  }

  const submit = (event) => {
    event.preventDefault()
    const latitude = toNumber(form.latitude)
    const longitude = toNumber(form.longitude)
    if (latitude === null || longitude === null || Number.isNaN(latitude) || Number.isNaN(longitude)) {
      setError(text.needLocation)
      return
    }
    onSave({ name: form.name.trim(), address: form.address.trim(), latitude, longitude })
  }

  const mapReady = map.status === 'ready'
  const inUse = institution && institution.classSessionCount > 0
  const title = institution ? text.editTitle(institution.name) : text.createTitle

  return (
    <form className="admin-card institution-form" aria-label={title} onSubmit={submit}>
      <div className="admin-card__head">
        <h2 className="admin-card__title">{title}</h2>
        {institution && <span className="admin-card__sub">{text.assigned(institution.classSessionCount)}</span>}
      </div>

      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="institution-name">
          {text.name}
        </label>
        <input
          id="institution-name"
          className="admin-form__input"
          required
          maxLength={100}
          value={form.name}
          onChange={(event) => change({ name: event.target.value })}
        />
      </div>
      <div className="admin-form__field">
        <label className="admin-form__label" htmlFor="institution-address">
          {text.address}
        </label>
        <div className="institution-form__search">
          <input
            id="institution-address"
            className="admin-form__input"
            required
            maxLength={255}
            value={form.address}
            onChange={(event) => change({ address: event.target.value })}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && mapReady) {
                event.preventDefault()
                search()
              }
            }}
          />
          {mapReady && (
            <button
              type="button"
              className="admin-form__button admin-form__button--dark"
              disabled={searching || !form.address.trim()}
              onClick={search}
            >
              {searching ? text.searching : text.search}
            </button>
          )}
        </div>
      </div>

      {mapReady ? (
        <InstitutionMap
          maps={map.maps}
          latitude={toNumber(form.latitude)}
          longitude={toNumber(form.longitude)}
          onMove={(latitude, longitude) => change({ latitude: String(latitude), longitude: String(longitude) })}
        />
      ) : (
        <p className={`admin-callout ${map.status === 'loading' ? 'admin-callout--info' : 'admin-callout--warn'}`}>
          {map.status === 'loading' ? text.mapLoading : map.status === 'noKey' ? text.mapNoKey : text.mapUnavailable}
        </p>
      )}

      <div className="admin-form__pair">
        <div className="admin-form__field">
          <label className="admin-form__label" htmlFor="institution-latitude">
            {text.latitude}
          </label>
          <input
            id="institution-latitude"
            className="admin-form__input"
            type="number"
            step="0.000001"
            min="-90"
            max="90"
            readOnly={mapReady}
            value={form.latitude}
            onChange={(event) => change({ latitude: event.target.value })}
          />
        </div>
        <div className="admin-form__field">
          <label className="admin-form__label" htmlFor="institution-longitude">
            {text.longitude}
          </label>
          <input
            id="institution-longitude"
            className="admin-form__input"
            type="number"
            step="0.000001"
            min="-180"
            max="180"
            readOnly={mapReady}
            value={form.longitude}
            onChange={(event) => change({ longitude: event.target.value })}
          />
        </div>
      </div>

      <p className="admin-callout admin-callout--warn">{text.warning}</p>
      {institution && <p className="admin-callout admin-callout--info">{text.editInfo}</p>}
      {error && (
        <p className="admin-form__error" role="alert">
          {error}
        </p>
      )}

      <div className="admin-form__actions">
        {institution && (
          <button
            type="button"
            className="admin-form__button admin-form__button--danger institution-form__delete"
            disabled={busy || inUse}
            title={inUse ? text.deleteLocked : undefined}
            onClick={onDelete}
          >
            {text.delete}
          </button>
        )}
        <button type="button" className="admin-form__button admin-form__button--ghost" onClick={onCancel}>
          {text.cancel}
        </button>
        <button type="submit" className="admin-form__button admin-form__button--dark" disabled={busy}>
          {text.save}
        </button>
      </div>
    </form>
  )
}

export default InstitutionForm
