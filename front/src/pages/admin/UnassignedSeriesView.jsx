import { useMemo, useState } from 'react'
import { assignSeriesStaff, getSeriesStaffSuggestions, getUnassignedSeries } from '../../api/admin'
import AdminNotice from '../../components/admin/AdminNotice'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import SeriesFilter from '../../components/admin/SeriesFilter'
import SeriesSuggestion from '../../components/admin/SeriesSuggestion'
import SeriesTable from '../../components/admin/SeriesTable'
import { seriesAdmin } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'

// useAdminQuery는 params 객체를 넘기므로 받는 모양을 맞춘다
const loadSeries = () => getUnassignedSeries()
const loadSuggestion = ({ seriesId }) => getSeriesStaffSuggestions(seriesId)

// 수업 할당 안 "미정 수업 배정" 보기 (EMP-SCR-007, EMP-FR-022).
// 직원 미정 정기 수업 묶음을 고르면 목록 아래 카드에 추천 직원을 보여 주고, 고른 직원에게 오늘 이후 미정 회차를 한꺼번에 배정한다
function UnassignedSeriesView() {
  const list = useAdminQuery(loadSeries, {})
  const [selectedId, setSelectedId] = useState(null)
  const suggestion = useAdminQuery(loadSuggestion, selectedId === null ? null : { seriesId: selectedId })
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [institutionId, setInstitutionId] = useState(null)
  // 기관 필터는 목록에 나온 기관만 고를 수 있게 한다
  const institutions = useMemo(() => {
    const byId = new Map((list.data ?? []).map((series) => [series.institutionId, series.institutionName]))
    return [...byId].map(([id, name]) => ({ id, name })).sort((a, b) => a.name.localeCompare(b.name, 'ko'))
  }, [list.data])
  const items = list.data?.filter((series) => institutionId === null || series.institutionId === institutionId)

  const changeInstitution = (value) => {
    setInstitutionId(value)
    setSelectedId(null)
  }

  const select = (seriesId) => {
    setMessage('')
    setSelectedId(seriesId)
  }

  const assign = async (staffId, name) => {
    setBusy(true)
    try {
      const assigned = await assignSeriesStaff(selectedId, staffId)
      setSelectedId(null)
      setMessage(seriesAdmin.assigned(assigned.length, name))
      list.reload()
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusy(false)
    }
  }

  let side = <AdminNotice inline message={seriesAdmin.choose} />
  if (selectedId !== null) {
    if (suggestion.error && !suggestion.loading) side = <AdminNotice inline error message={suggestion.error.message} />
    else if (!suggestion.data || suggestion.loading) side = <AdminNotice inline message={seriesAdmin.loading} />
    else
      side = (
        <SeriesSuggestion
          key={selectedId}
          suggestion={suggestion.data}
          busy={busy}
          onAssign={assign}
        />
      )
  }

  return (
    <>
      <p className="admin-section__description">{seriesAdmin.description}</p>
      <SeriesFilter institutions={institutions} value={institutionId} onChange={changeInstitution} />
      {message && (
        <p className="admin-callout admin-callout--info" role="status">
          {message}
        </p>
      )}
      <AdminQueryBody query={list} items={items} empty={seriesAdmin.empty}>
        {(rows) => <SeriesTable items={rows} selectedId={selectedId} onSelect={select} />}
      </AdminQueryBody>
      <p className="admin-section__description">{seriesAdmin.singleHint}</p>
      {/* 11-26 초안 수정: 추천 카드는 목록 옆이 아니라 아래에 둔다 */}
      {side}
    </>
  )
}

export default UnassignedSeriesView
