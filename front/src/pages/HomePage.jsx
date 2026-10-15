import Hero from '../components/home/Hero'
import StatsBar from '../components/home/StatsBar'
import ServiceCards from '../components/home/ServiceCards'
import WhyMongkids from '../components/home/WhyMongkids'
import Reviews from '../components/home/Reviews'

function HomePage({ onInstitutionConsultClick }) {
  return (
    <>
      <Hero onInstitutionConsultClick={onInstitutionConsultClick} />
      <StatsBar />
      <ServiceCards />
      <WhyMongkids />
      <Reviews />
    </>
  )
}

export default HomePage
