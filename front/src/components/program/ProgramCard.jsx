import { Link } from 'react-router-dom'
import ChevronIcon from '../common/ChevronIcon'
import ProgramIcon from './ProgramIcon'
import { programCardMore, programPath } from '../../data/programContent'
import './ProgramCard.css'

// 전체 안내(SCR-003)의 프로그램 카드. 카드 전체가 상세 화면 링크다.
// variant: 'regular'(세로형, 정기 방문체육) | 'special'(가로형, 운동회·특별 프로그램)
function ProgramCard({ program, variant = 'regular' }) {
  return (
    <Link
      to={programPath(program.id)}
      className={`program-card program-card--${variant} program-card--${program.theme}`}
    >
      <div className="program-card__art">
        <ProgramIcon programId={program.id} />
      </div>
      <div className="program-card__body">
        <span className="program-card__tag">{program.tag}</span>
        <h3 className="program-card__title">{program.name}</h3>
        <p className="program-card__summary">{program.summary}</p>
        <span className="program-card__more">
          {programCardMore}
          <ChevronIcon />
        </span>
      </div>
      <span className="program-card__chevron">
        <ChevronIcon size={18} />
      </span>
    </Link>
  )
}

export default ProgramCard
