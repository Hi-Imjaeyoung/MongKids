import { useState } from 'react'
import { changeMemberRole, getMembers } from '../../api/admin'
import AdminListBody from '../../components/admin/AdminListBody'
import AdminSection from '../../components/admin/AdminSection'
import FilterChips from '../../components/admin/FilterChips'
import MemberSearch from '../../components/admin/MemberSearch'
import MemberTable from '../../components/admin/MemberTable'
import { toApiFilter } from '../../components/admin/adminFormat'
import Pagination from '../../components/lab/Pagination'
import { memberAdmin, memberRoles } from '../../data/adminContent'
import useAdminList from '../useAdminList'

const ROLE_FILTERS = memberRoles.filter((role) => !role.hidden)

// 회원 · 직원 권한 (FR-022). 지정·해제는 확인 창으로 바뀌는 내용을 알린 뒤 처리한다
function MembersTab() {
  const [role, setRole] = useState('ALL')
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(1)
  const list = useAdminList(getMembers, { role: toApiFilter(role), keyword, page: page - 1 })
  const [busyId, setBusyId] = useState(null)

  const changeRole = (value) => {
    setRole(value)
    setPage(1)
  }
  const search = (value) => {
    setKeyword(value)
    setPage(1)
  }

  const toggleStaff = async (member) => {
    const grant = member.role !== 'STAFF'
    if (!window.confirm(grant ? memberAdmin.grantConfirm(member.name) : memberAdmin.revokeConfirm(member.name))) return
    setBusyId(member.id)
    try {
      const updated = await changeMemberRole(member.id, grant ? 'STAFF' : 'MEMBER')
      list.updateItem(
        (item) => item.id === updated.id,
        () => updated,
      )
    } catch (error) {
      window.alert(error.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <AdminSection
      title={memberAdmin.title}
      description={memberAdmin.description}
      toolbar={
        <>
          <FilterChips label={memberAdmin.columns.role} options={ROLE_FILTERS} value={role} onChange={changeRole} />
          <MemberSearch onSearch={search} />
        </>
      }
    >
      <AdminListBody list={list} empty={memberAdmin.empty}>
        {(result) => <MemberTable members={result.content} busyId={busyId} onRoleChange={toggleStaff} />}
      </AdminListBody>
      {list.page && <Pagination page={page} totalPages={list.page.totalPages} onChange={setPage} />}
    </AdminSection>
  )
}

export default MembersTab
