import Hero from '../components/home/Hero'
import StatsBar from '../components/home/StatsBar'
// import ServiceCards from '../components/home/ServiceCards'
import WhyMongkids from '../components/home/WhyMongkids'
import ClassGallery from '../components/home/ClassGallery'
import Reviews from '../components/home/Reviews'
import ProcessSteps from '../components/home/ProcessSteps'
import SpecialPrograms from '../components/home/SpecialPrograms'
import FaqPreview from '../components/home/FaqPreview'
import ContactCta from '../components/home/ContactCta'

function HomePage({ onInstitutionConsultClick, onPersonalConsultClick }) {
  return (
    <>
      <Hero />
      <StatsBar />
      {/* SCR-001 프로그램 섹션(services) 숨김: 삭제 전 QA 중 */}
      {/* <ServiceCards /> */}
      <WhyMongkids />
      <ClassGallery />
      <Reviews />
      <ProcessSteps />
      <SpecialPrograms />
      <FaqPreview />
      <ContactCta
        onInstitutionConsultClick={onInstitutionConsultClick}
        onPersonalConsultClick={onPersonalConsultClick}
      />
    </>
  )
}

export default HomePage
