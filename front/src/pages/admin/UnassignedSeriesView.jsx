import { useState } from 'react'
import { assignSeriesStaff, getSeriesStaffSuggestions, getUnassignedSeries } from '../../api/admin'
import AdminNotice from '../../components/admin/AdminNotice'
import AdminQueryBody from '../../components/admin/AdminQueryBody'
import AdminSplit from '../../components/admin/AdminSplit'
import SeriesSuggestion from '../../components/admin/SeriesSuggestion'
import SeriesTable from '../../components/admin/SeriesTable'
import { seriesAdmin } from '../../data/attendanceAdminContent'
import useAdminQuery from '../useAdminQuery'

// useAdminQuery는 params 객체를 넘기므로 받는 모양을 맞춘다
const loadSeries = () => getUnassignedSeries()
const loadSuggestion = ({ seriesId }) => getSeriesStaffSuggestions(seriesId)

// 수업 할당 안 "미정 수업 배정" 보기 (EMP-SCR-007, EMP-FR-022).
// 직원 미정 정기 수업 묶음을 고르면 옆 패널에 추천 직원을 보여 주고, 고른 직원에게 오늘 이후 미정 회차를 한꺼번에 배정한다
function UnassignedSeriesView() {
  const list = useAdminQuery(loadSeries, {})
  const [selectedId, setSelectedId] = useState(null)
  const suggestion = useAdminQuery(loadSuggestion, selectedId === null ? null : { seriesId: selectedId })
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')

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
      {message && (
        <p className="admin-callout admin-callout--info" role="status">
          {message}
        </p>
      )}
      <AdminSplit
        main={
          <>
            <AdminQueryBody query={list} empty={seriesAdmin.empty}>
              {(items) => <SeriesTable items={items} selectedId={selectedId} onSelect={select} />}
            </AdminQueryBody>
            <p className="admin-section__description">{seriesAdmin.singleHint}</p>
          </>
        }
        side={side}
      />
    </>
  )
}

export default UnassignedSeriesView
