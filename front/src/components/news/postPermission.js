// 게시물 권한 (FR-009). member.role은 /api/v1/members/me 응답(DB의 현재 역할)이고, 서버도 같은 기준으로 검사한다
const POST_WRITER_ROLES = ['STAFF', 'ADMIN']

// 게시물을 작성할 수 있는 직원인지
export function canWritePost(member) {
  return POST_WRITER_ROLES.includes(member?.role)
}

// 게시물을 수정·삭제할 수 있는지: 직원인 작성자 본인이거나 관리자
export function canManagePost(member, post) {
  return canWritePost(member) && (member.id === post.authorId || member.role === 'ADMIN')
}
