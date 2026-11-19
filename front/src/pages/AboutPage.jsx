import { useEffect, useState } from 'react'
import { getAboutCurriculumMonths } from '../api/aboutCurriculum'
import AboutHero from '../components/about/AboutHero'
import AboutPrinciple from '../components/about/AboutPrinciple'
import AboutStandards from '../components/about/AboutStandards'
import AboutWhy from '../components/about/AboutWhy'
import TeacherList from '../components/about/TeacherList'
import ConsultCta from '../components/common/ConsultCta'
import { isAdmin } from '../components/news/postPermission'
import { aboutCta, aboutPrinciples } from '../data/aboutContent'
import { consultLabels } from '../data/homeContent'
import { pageMeta } from '../data/seoContent'
import usePageMeta from './usePageMeta'

// ABOUT 몽키즈 (SCR-002). 월별 커리큘럼은 관리자가 저장한 값이 있으면 그 값을 쓴다 (FR-034)
function AboutPage({ member, onInstitutionConsultClick, onPersonalConsultClick }) {
  usePageMeta(pageMeta.about.title, pageMeta.about.description)
  // undefined: 불러오는 중, null: 불러오지 못함(기본 문구), 배열: 저장 값(빈 배열이면 기본 문구)
  const [savedMonths, setSavedMonths] = useState(undefined)

  useEffect(() => {
    let ignore = false
    getAboutCurriculumMonths()
      .then((months) => !ignore && setSavedMonths(months))
      .catch(() => !ignore && setSavedMonths(null))
    return () => {
      ignore = true
    }
  }, [])

  const curriculumEdit = { savedMonths, canEdit: isAdmin(member), onSavedMonthsChange: setSavedMonths }
  return (
    <>
      <AboutHero />
      <AboutStandards />
      {aboutPrinciples.map((principle) => (
        <AboutPrinciple key={principle.id} principle={principle} curriculumEdit={curriculumEdit} />
      ))}
      <AboutWhy />
      <TeacherList />
      <ConsultCta
        titleLines={aboutCta.titleLines}
        description={aboutCta.description}
        titleId="about-cta-title"
        labels={consultLabels}
        onInstitutionConsultClick={onInstitutionConsultClick}
        onPersonalConsultClick={onPersonalConsultClick}
      />
    </>
  )
}

export default AboutPage
