import { Link } from 'react-router-dom'
import SectionHeading from '../common/SectionHeading'
import ChevronIcon from '../common/ChevronIcon'
import { aboutTeachers } from '../../data/aboutContent'
import './TeacherList.css'

// 강사 소개. 채용 링크는 강사 채용(SCR-007) 화면으로 간다 (GNB에는 채용 메뉴가 없다)
function TeacherList() {
  return (
    <section className="teacher-list container" aria-labelledby="teacher-list-title">
      <div className="teacher-list__header">
        <SectionHeading
          eyebrow={aboutTeachers.eyebrow}
          accent={aboutTeachers.eyebrow}
          title={aboutTeachers.title}
          titleId="teacher-list-title"
          align="left"
        />
        <Link to={aboutTeachers.recruit.to} className="teacher-list__recruit">
          {aboutTeachers.recruit.label}
          <ChevronIcon />
        </Link>
      </div>
      <div className="teacher-list__items">
        {aboutTeachers.items.map((teacher) => (
          <article key={teacher.id} className="teacher-card">
            <div className="teacher-card__photo">
              <img src={teacher.photo} alt={aboutTeachers.photoLabel} />
            </div>
            <div>
              <h3 className="teacher-card__name">{teacher.name}</h3>
              {/* "전담 분야" 글자 뒤에 분야마다 뱃지 하나. 한 강사의 뱃지는 같은 색이다 */}
              <div className="teacher-card__specialties">
                <span className="teacher-card__specialty-label">{aboutTeachers.specialtyLabel}</span>
                <ul className="teacher-card__specialty-list">
                  {teacher.specialties.map((specialty) => (
                    <li key={specialty} className={`teacher-card__specialty teacher-card__specialty--${teacher.theme}`}>
                      {specialty}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default TeacherList
