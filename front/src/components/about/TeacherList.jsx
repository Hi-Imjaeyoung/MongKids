import SectionHeading from '../common/SectionHeading'
import ChevronIcon from '../common/ChevronIcon'
import { aboutTeachers } from '../../data/aboutContent'
import './TeacherList.css'

// 강사 소개. 강사 채용(SCR-007) 화면 전까지 채용 링크는 앵커로 둔다
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
        <a href={aboutTeachers.recruit.href} className="teacher-list__recruit">
          {aboutTeachers.recruit.label}
          <ChevronIcon />
        </a>
      </div>
      <div className="teacher-list__items">
        {aboutTeachers.items.map((teacher) => (
          <article key={teacher.id} className="teacher-card">
            <div className="teacher-card__photo">
              <img src={teacher.photo} alt={aboutTeachers.photoLabel} />
            </div>
            <div>
              <h3 className="teacher-card__name">{teacher.name}</h3>
              <p className="teacher-card__meta">{teacher.school}</p>
              <p className="teacher-card__meta">{teacher.career}</p>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default TeacherList
