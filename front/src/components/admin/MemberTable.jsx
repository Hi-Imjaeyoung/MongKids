import { memberAdmin, memberProviders, memberRoles } from '../../data/adminContent'
import { formatDate } from '../news/postFormat'
import AdminBadge from './AdminBadge'
import { formatPhone } from './adminFormat'
import './MemberTable.css'

const findRole = (id) => memberRoles.find((role) => role.id === id)

// 회원 목록 (FR-022). 관리자 행에는 변경 버튼이 없다. busyId인 행은 버튼을 잠근다
function MemberTable({ members, busyId, onRoleChange }) {
  const { columns } = memberAdmin

  return (
    <div className="member-table" role="region" aria-label={memberAdmin.listLabel} tabIndex={0}>
      <div className="member-table__inner">
        <div className="member-table__row member-table__row--head">
          <span>{columns.name}</span>
          <span>{columns.phone}</span>
          <span>{columns.provider}</span>
          <span>{columns.createdAt}</span>
          <span>{columns.role}</span>
          <span />
        </div>
        {members.map((member) => {
          const role = findRole(member.role)
          const isStaff = member.role === 'STAFF'
          return (
            <div key={member.id} className="member-table__row">
              <span className="member-table__name">{member.name}</span>
              <span className="member-table__sub">{formatPhone(member.phone)}</span>
              <span>
                <span className={`member-table__provider member-table__provider--${member.provider.toLowerCase()}`}>
                  {memberProviders[member.provider]}
                </span>
              </span>
              <span className="member-table__sub">{formatDate(member.createdAt)}</span>
              <span>
                <AdminBadge tone={role.tone}>{role.label}</AdminBadge>
              </span>
              <span className="member-table__action">
                {member.role !== 'ADMIN' && (
                  <button
                    type="button"
                    className={`member-table__button ${isStaff ? 'member-table__button--revoke' : ''}`}
                    disabled={busyId === member.id}
                    onClick={() => onRoleChange(member)}
                  >
                    {isStaff ? memberAdmin.revoke : memberAdmin.grant}
                  </button>
                )}
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default MemberTable
