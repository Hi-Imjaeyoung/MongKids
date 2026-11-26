import { useState } from 'react'
import { seriesAdmin, shortDateLabel } from '../../data/attendanceAdminContent'
import { formatTime } from '../attendance/attendanceFormat'
import { reasonDates, seriesDays, seriesTitle, timeRange } from './seriesFormat'
import './AdminForm.css'
import './SeriesSuggestion.css'

function NeighborLine({ position, neighbors, total }) {
  const label = seriesAdmin.position[position]
  const items = neighbors.filter((item) => item.position === position)
  if (items.length === 0) return <span className="series-suggestion__line">{seriesAdmin.noNeighbor(label)}</span>
  return items.map((item) => (
    <span key={`${item.institutionName}-${item.startTime}`} className="series-suggestion__line">
      {seriesAdmin.neighbor(label, item, timeRange(item), formatTime(item.arrivalTime), item.count < total)}
    </span>
  ))
}

// 정기 수업 담당 직원 추천 패널 (EMP-SCR-007, EMP-FR-022). suggestion은 SeriesStaffSuggestionResponse.
// 추천 직원을 순위대로 고르기(기본 1순위)로 보여 주고, 위쪽 배정 버튼 onAssign(staffId, name)으로 바로 배정한다.
// 배정할 수 없는 직원은 사유·날짜와 함께 따로 보여 준다
function SeriesSuggestion({ suggestion, busy, onAssign }) {
  const { series, candidates, unavailable } = suggestion
  const [staffId, setStaffId] = useState(candidates[0]?.staffId ?? null)
  const text = seriesAdmin
  const title = seriesTitle(series)
  const total = series.unassignedCount
  const picked = candidates.find((candidate) => candidate.staffId === staffId)

  const submit = (event) => {
    event.preventDefault()
    if (picked) onAssign(picked.staffId, picked.staffName)
  }

  return (
    <form className="series-suggestion" aria-label={text.panelLabel(title)} onSubmit={submit}>
      <div className="series-suggestion__head">
        <h2 className="series-suggestion__title">{title}</h2>
        <dl className="admin-fields">
          <dt>{text.facts.schedule}</dt>
          <dd>
            {seriesDays(series.daysOfWeek)} {timeRange(series)}
          </dd>
          <dt>{text.facts.period}</dt>
          <dd>
            {series.firstDate} ~ {series.lastDate}
          </dd>
          <dt>{text.facts.target}</dt>
          <dd>
            {text.target(total, shortDateLabel(series.nextDate), shortDateLabel(series.lastUnassignedDate))}
          </dd>
        </dl>
      </div>
      <p className="series-suggestion__rule">
        <strong>{text.ruleTitle}</strong> {text.rule}
      </p>
      {/* 11-26 초안 수정: 배정 버튼을 추천 목록 위에 둔다 */}
      {picked && (
        <div className="series-suggestion__actions">
          <button type="submit" className="admin-form__button admin-form__button--dark" disabled={busy}>
            {text.submit(picked.staffName, total)}
          </button>
        </div>
      )}

      {candidates.length === 0 ? (
        <p className="admin-callout admin-callout--warn">{text.noCandidates}</p>
      ) : (
        <fieldset className="series-suggestion__group">
          <legend className="admin-form__label">{text.candidatesLegend(candidates.length)}</legend>
          <ul className="series-suggestion__list">
            {candidates.map((candidate) => {
              const checked = candidate.staffId === staffId
              return (
                <li key={candidate.staffId}>
                  <label className={`series-suggestion__option ${checked ? 'series-suggestion__option--on' : ''}`}>
                    <input
                      type="radio"
                      name="series-staff"
                      className="series-suggestion__radio"
                      checked={checked}
                      onChange={() => setStaffId(candidate.staffId)}
                    />
                    <span className="series-suggestion__rank" aria-hidden="true">
                      {candidate.rank}
                    </span>
                    <span className="series-suggestion__body">
                      <span className="series-suggestion__name">{candidate.staffName}</span>
                      <NeighborLine position="BEFORE" neighbors={candidate.neighbors} total={total} />
                      <NeighborLine position="AFTER" neighbors={candidate.neighbors} total={total} />
                    </span>
                    <span className="series-suggestion__stats">
                      <strong>{text.weekly(candidate.weeklyAverage)}</strong>
                      <span>{text.extraAverage(candidate.extraTravelAverage)}</span>
                      <span>{text.extraMax(candidate.extraTravelMax)}</span>
                    </span>
                  </label>
                </li>
              )
            })}
          </ul>
        </fieldset>
      )}

      {unavailable.length > 0 && (
        <section className="series-suggestion__group" aria-labelledby="series-unavailable-title">
          <h3 id="series-unavailable-title" className="admin-form__label">
            {text.unavailableTitle(unavailable.length)}
          </h3>
          <ul className="series-suggestion__list">
            {unavailable.map((staff) => (
              <li key={staff.staffId} className="series-suggestion__unavailable">
                <span className="series-suggestion__unavailable-name">{staff.staffName}</span>
                {staff.reasons.map((reason) => {
                  const { labels, rest } = reasonDates(reason.dates)
                  return (
                    <span key={reason.type} className="series-suggestion__reason">
                      <strong className="series-suggestion__why">{text.reasons[reason.type](reason)}</strong>
                      <span className="series-suggestion__line">
                        {text.dates(labels, rest)} · {text.reasonDetail[reason.type](reason, timeRange(reason))}
                      </span>
                    </span>
                  )
                })}
              </li>
            ))}
          </ul>
        </section>
      )}

      <p className="admin-form__hint">{text.travelNote}</p>
    </form>
  )
}

export default SeriesSuggestion
