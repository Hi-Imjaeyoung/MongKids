import { submitTeacherApplication } from '../api/teacherApplications'
import PageHero from '../components/common/PageHero'
import RecruitApplyForm from '../components/recruit/RecruitApplyForm'
import RecruitContact from '../components/recruit/RecruitContact'
import RecruitSummary from '../components/recruit/RecruitSummary'
import { recruitApplySection, recruitHero } from '../data/recruitContent'
import { pageMeta } from '../data/seoContent'
import usePageMeta from './usePageMeta'

// 강사 채용 (SCR-007). GNB에는 없고 ABOUT 몽키즈 강사 소개의 채용 링크로 들어온다.
// 지원서 양식 내려받기(정적 파일)와 작성한 지원서 올리기(FR-007, 로그인 불필요)만 제공한다
function RecruitPage() {
  usePageMeta(pageMeta.recruit.title, pageMeta.recruit.description)
  return (
    <>
      <PageHero {...recruitHero} titleId="recruit-page-title" />
      <RecruitSummary />
      <section id="apply" className="recruit-container" aria-labelledby="recruit-apply-title">
        <h2 id="recruit-apply-title" className="recruit-section-title">
          {recruitApplySection.title}
        </h2>
        <RecruitApplyForm onSubmit={submitTeacherApplication} />
      </section>
      <RecruitContact />
    </>
  )
}

export default RecruitPage
