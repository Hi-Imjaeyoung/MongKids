import { getInstitutions, getMembers } from '../../api/admin'
import useAdminQuery from '../useAdminQuery'

const NO_PARAMS = {}

// 직원 권한(STAFF) 회원. 직원 수는 적다고 보고 한 번에 100명까지 불러온다
async function loadStaffs() {
  const page = await getMembers({ role: 'STAFF', size: 100 })
  return page.content.map((member) => ({ id: member.id, name: member.name }))
}

// 근태 관리자 화면의 직원·기관 선택 목록. 불러오기 전에는 빈 배열
function useAttendanceOptions() {
  const staffs = useAdminQuery(loadStaffs, NO_PARAMS)
  const institutions = useAdminQuery(getInstitutions, NO_PARAMS)
  return { staffs: staffs.data ?? [], institutions: institutions.data ?? [] }
}

export default useAttendanceOptions
