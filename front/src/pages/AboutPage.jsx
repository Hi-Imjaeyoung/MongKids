import AboutHero from '../components/about/AboutHero'
import AboutPrinciple from '../components/about/AboutPrinciple'
import AboutStandards from '../components/about/AboutStandards'
import AboutWhy from '../components/about/AboutWhy'
import TeacherList from '../components/about/TeacherList'
import ConsultCta from '../components/common/ConsultCta'
import { aboutCta, aboutPrinciples } from '../data/aboutContent'
import { consultLabels } from '../data/homeContent'
import { pageMeta } from '../data/seoContent'
import usePageMeta from './usePageMeta'

// ABOUT 몽키즈 (SCR-002)
function AboutPage({ onInstitutionConsultClick, onPersonalConsultClick }) {
  usePageMeta(pageMeta.about.title, pageMeta.about.description)
  return (
    <>
      <AboutHero />
      <AboutStandards />
      {aboutPrinciples.map((principle) => (
        <AboutPrinciple key={principle.id} principle={principle} />
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
