import PageHero from '../components/common/PageHero'
import PrivacyPolicy from '../components/privacy/PrivacyPolicy'
import PrivacySummary from '../components/privacy/PrivacySummary'
import { privacyHero } from '../data/privacyContent'

// 개인정보처리방침 (SCR-023). 푸터 링크로 들어오며 서버 기능 없이 정적 데이터만 쓴다
function PrivacyPage() {
  return (
    <>
      <PageHero {...privacyHero} titleId="privacy-page-title" />
      <PrivacySummary />
      <PrivacyPolicy />
    </>
  )
}

export default PrivacyPage
