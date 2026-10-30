import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { changeSnsAgreement, getWithdrawUrl } from '../api/members'
import Button from '../components/common/Button'
import MyInfoCard from '../components/member/MyInfoCard'
import MyInfoLayout from '../components/member/MyInfoLayout'
import SnsAgreementSwitch from '../components/member/SnsAgreementSwitch'
import WithdrawConfirmModal from '../components/member/WithdrawConfirmModal'
import WithdrawDone from '../components/member/WithdrawDone'
import WithdrawSection from '../components/member/WithdrawSection'
import { myInfoPage, snsAgreement, socialAccounts, withdrawErrors } from '../data/memberContent'

// 탈퇴 본인 확인(재로그인) 뒤 백엔드가 붙여 보내는 쿼리 파라미터 (FR-028)
const WITHDRAWN_PARAM = 'withdrawn'
const WITHDRAW_ERROR_PARAM = 'withdrawError'

// 내 정보 (SCR-025): 정보 확인(FR-027), SNS 수신 동의 변경(FR-019), 회원 탈퇴(FR-028).
// member: undefined면 로그인 상태 확인 중, null이면 비로그인. onMemberChange: 바뀐 내 정보를 App에 반영
function MyInfoPage({ member, onLoginClick, onMemberChange }) {
  const [searchParams, setSearchParams] = useSearchParams()
  // 결과는 처음 들어왔을 때 한 번만 보여 주고 주소에서 지운다
  const [withdrawn] = useState(() => searchParams.has(WITHDRAWN_PARAM))
  const [withdrawError] = useState(() => searchParams.get(WITHDRAW_ERROR_PARAM))
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [snsSaving, setSnsSaving] = useState(false)
  const [snsError, setSnsError] = useState(null)

  useEffect(() => {
    if (searchParams.has(WITHDRAWN_PARAM) || searchParams.has(WITHDRAW_ERROR_PARAM)) {
      setSearchParams({}, { replace: true })
    }
  }, [searchParams, setSearchParams])

  const closeConfirm = useCallback(() => setConfirmOpen(false), [])

  const handleSnsChange = async (agreed) => {
    setSnsSaving(true)
    setSnsError(null)
    try {
      onMemberChange(await changeSnsAgreement(agreed))
    } catch {
      setSnsError(snsAgreement.error)
    } finally {
      setSnsSaving(false)
    }
  }

  if (withdrawn) {
    return (
      <MyInfoLayout>
        <WithdrawDone />
      </MyInfoLayout>
    )
  }

  if (!member) {
    return (
      <MyInfoLayout>
        <div className="my-info-notice">
          <p className="my-info-notice__message">
            {member === undefined ? myInfoPage.checking : myInfoPage.loginRequired}
          </p>
          {member === null && (
            <Button variant="blue" onClick={onLoginClick}>
              {myInfoPage.login}
            </Button>
          )}
        </div>
      </MyInfoLayout>
    )
  }

  const providerLabel = socialAccounts[member.provider].label

  return (
    <MyInfoLayout>
      <div>
        <p className="my-info-heading__eyebrow">{myInfoPage.eyebrow}</p>
        <h1 className="my-info-heading__title">{myInfoPage.title}</h1>
        <p className="my-info-heading__description">{myInfoPage.description}</p>
      </div>

      {withdrawError && (
        <p className="my-info-alert" role="alert">
          {withdrawErrors[withdrawError] ?? withdrawErrors.default}
        </p>
      )}

      <MyInfoCard
        member={member}
        snsControl={
          <SnsAgreementSwitch
            agreed={member.snsAgreed}
            agreedAt={member.snsAgreedAt}
            saving={snsSaving}
            error={snsError}
            onChange={handleSnsChange}
          />
        }
      />

      <WithdrawSection role={member.role} onWithdrawClick={() => setConfirmOpen(true)} />

      {confirmOpen && (
        <WithdrawConfirmModal
          providerLabel={providerLabel}
          onConfirm={() => window.location.assign(getWithdrawUrl(member.provider))}
          onClose={closeConfirm}
        />
      )}
    </MyInfoLayout>
  )
}

export default MyInfoPage
