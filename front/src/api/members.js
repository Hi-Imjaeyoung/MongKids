// 내 정보(SCR-025) API. 내 정보 조회는 로그인 상태 확인과 같아서 auth.js의 fetchMyInfo를 쓴다
import { request } from './client'

// SNS 수신 동의 변경 (FR-019). 바뀐 내 정보를 돌려준다
export function changeSnsAgreement(agreed) {
  return request('/api/v1/members/me/sns-agreement', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ agreed }),
  })
}

// 회원 탈퇴 (FR-028)는 페이지 이동으로 시작한다. 가입한 소셜 계정으로 다시 로그인해 본인을 확인하고,
// 백엔드가 성공하면 홈(/?withdrawn=true, 완료 모달), 실패하면 /me?withdrawError=코드로 돌려보낸다
export function getWithdrawUrl(provider) {
  return `/oauth2/authorization/${provider.toLowerCase()}-withdraw`
}
