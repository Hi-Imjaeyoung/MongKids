import { formatPhone } from '../admin/adminFormat'
import { formatDate } from '../news/postFormat'
import { myInfoFields, socialAccounts } from '../../data/memberContent'
import './MyInfoCard.css'

// 회원 정보 카드 (FR-027). snsControl: SNS 수신 동의 칸 내용 (SnsAgreementSwitch)
function MyInfoCard({ member, snsControl }) {
  const account = socialAccounts[member.provider]

  return (
    <section className="my-info-card" aria-label="회원 정보">
      <dl className="my-info-card__list">
        <div className="my-info-card__row">
          <dt className="my-info-card__label">{myInfoFields.name}</dt>
          <dd className="my-info-card__value">{member.name}</dd>
        </div>
        <div className="my-info-card__row">
          <dt className="my-info-card__label">{myInfoFields.phone}</dt>
          <dd className="my-info-card__value">
            {member.phone ? (
              formatPhone(member.phone)
            ) : (
              <span className="my-info-card__empty">{myInfoFields.noPhone}</span>
            )}
          </dd>
        </div>
        <div className="my-info-card__row">
          <dt className="my-info-card__label">{myInfoFields.email}</dt>
          <dd className="my-info-card__value my-info-card__value--email">
            {member.email ?? <span className="my-info-card__empty">{myInfoFields.noEmail}</span>}
          </dd>
        </div>
        <div className="my-info-card__row">
          <dt className="my-info-card__label">{myInfoFields.provider}</dt>
          <dd className="my-info-card__value my-info-card__account">
            <span className={`my-info-card__provider my-info-card__provider--${member.provider.toLowerCase()}`}>
              <span className="my-info-card__provider-mark" aria-hidden="true">
                {account.initial}
              </span>
              {account.label}
            </span>
            <span className="my-info-card__joined">{myInfoFields.joined(formatDate(member.createdAt))}</span>
          </dd>
        </div>
        <div className="my-info-card__row">
          <dt id="my-info-sns-label" className="my-info-card__label">
            {myInfoFields.sns}
          </dt>
          <dd className="my-info-card__value my-info-card__sns">{snsControl}</dd>
        </div>
      </dl>
    </section>
  )
}

export default MyInfoCard
