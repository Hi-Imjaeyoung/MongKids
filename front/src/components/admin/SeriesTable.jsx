import { classTypes, eventTag, seriesAdmin } from '../../data/attendanceAdminContent'
import AdminBadge from './AdminBadge'
import { seriesDays, seriesTitle, timeRange } from './seriesFormat'
import './ClassSessionTable.css'
import './SeriesTable.css'

const REGULAR_LABEL = classTypes.find((type) => type.id === 'REGULAR').label

// 직원 미정 정기 수업 표 (EMP-SCR-007). items는 UnassignedSeriesResponse[].
// 줄을 누르면 onSelect(seriesId). selectedId인 줄을 강조한다
function SeriesTable({ items, selectedId, onSelect }) {
  const { columns } = seriesAdmin

  return (
    <div className="class-session-table series-table" role="region" aria-label={seriesAdmin.listLabel} tabIndex={0}>
      <table>
        <thead>
          <tr>
            <th scope="col">{columns.series}</th>
            <th scope="col">{columns.schedule}</th>
            <th scope="col">{columns.period}</th>
            <th scope="col" className="series-table__count">
              {columns.count}
            </th>
          </tr>
        </thead>
        <tbody>
          {items.map((series) => {
            const title = seriesTitle(series)
            const selected = series.seriesId === selectedId
            return (
              <tr key={series.seriesId} className={selected ? 'series-table__row--selected' : undefined}>
                <td>
                  <button
                    type="button"
                    className="series-table__pick"
                    aria-pressed={selected}
                    aria-label={seriesAdmin.pick(title)}
                    onClick={() => onSelect(series.seriesId)}
                  >
                    <strong>{title}</strong>
                    <span className="series-table__sub">
                      {series.classType === 'EVENT' ? (
                        <AdminBadge tone="purple" shape="tag">
                          {eventTag}
                        </AdminBadge>
                      ) : (
                        REGULAR_LABEL
                      )}
                    </span>
                  </button>
                </td>
                <td>
                  {seriesDays(series.daysOfWeek)} {timeRange(series)}
                </td>
                <td>
                  {series.firstDate}
                  <span className="series-table__sub">~ {series.lastDate}</span>
                </td>
                <td className="series-table__count">{seriesAdmin.count(series.unassignedCount)}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default SeriesTable
