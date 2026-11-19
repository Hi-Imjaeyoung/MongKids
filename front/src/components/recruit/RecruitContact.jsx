import { recruitContact } from '../../data/recruitContent'
import './RecruitContact.css'

// 강사 채용 문의 안내 한 줄 (SCR-007)
function RecruitContact() {
  return (
    <div className="recruit-contact">
      <p className="recruit-contact__box">
        {recruitContact.label} <strong>{recruitContact.phone}</strong> · <strong>{recruitContact.email}</strong>
        <span className="recruit-contact__note">{recruitContact.note}</span>
      </p>
    </div>
  )
}

export default RecruitContact
