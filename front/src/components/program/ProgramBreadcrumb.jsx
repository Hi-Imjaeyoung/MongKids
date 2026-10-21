import { Link } from 'react-router-dom'
import { programDetail, programGroups } from '../../data/programContent'
import './ProgramBreadcrumb.css'

// 위치 표시: 체육/행사프로그램 › 분류 › 이름
function ProgramBreadcrumb({ program }) {
  const group = programGroups.find((item) => item.id === program.group)
  return (
    <nav className="program-breadcrumb container" aria-label="현재 위치">
      <Link to="/programs" className="program-breadcrumb__root">
        {programDetail.breadcrumbRoot}
      </Link>
      <span aria-hidden="true"> › </span>
      {group.title}
      <span aria-hidden="true"> › </span>
      <span className="program-breadcrumb__current" aria-current="page">
        {program.name}
      </span>
    </nav>
  )
}

export default ProgramBreadcrumb
