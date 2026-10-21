import { Link } from 'react-router-dom'
import ChevronIcon from '../common/ChevronIcon'
import { programDetail, programPath, programs } from '../../data/programContent'
import './OtherPrograms.css'

// 현재 프로그램을 뺀 나머지 프로그램 링크
function OtherPrograms({ currentId }) {
  return (
    <section className="other-programs container" aria-labelledby="other-programs-title">
      <h2 id="other-programs-title" className="other-programs__title">
        {programDetail.others}
      </h2>
      <div className="other-programs__list">
        {programs
          .filter((program) => program.id !== currentId)
          .map((program) => (
            <Link key={program.id} to={programPath(program.id)} className="other-programs__link">
              {program.name}
              <ChevronIcon />
            </Link>
          ))}
      </div>
    </section>
  )
}

export default OtherPrograms
