// 내 정보 화면(SCR-025) 문구

export const myInfoPage = {
  eyebrow: 'MY PAGE',
  title: '내 정보',
  description: '몽키즈에 가입한 정보예요. 이름과 이메일은 연동된 소셜 계정에서 받아 왔어요.',
  checking: '로그인 상태를 확인하는 중이에요.',
  loginRequired: '내 정보를 보려면 로그인해 주세요.',
  login: '로그인',
}

export const myInfoFields = {
  name: '이름',
  phone: '휴대전화 번호',
  email: '이메일',
  provider: '연동된 계정',
  sns: 'SNS 수신 동의',
  noPhone: '등록된 번호가 없어요',
  noEmail: '등록된 이메일이 없어요',
  joined: (date) => `${date} 가입`,
}

// id는 백엔드 Provider
export const socialAccounts = {
  KAKAO: { label: '카카오', initial: 'K' },
  NAVER: { label: '네이버', initial: 'N' },
}

export const snsAgreement = {
  on: '받음',
  off: '받지 않음',
  agreed: (date) => `${date}에 동의했어요. 수업 소식·이벤트 안내를 받아요.`,
  notAgreed: '수업 소식·이벤트 안내를 받지 않아요.',
  error: 'SNS 수신 동의를 바꾸지 못했어요. 잠시 후 다시 시도해 주세요.',
}

export const withdrawSection = {
  title: '회원 탈퇴',
  description: '탈퇴하면 회원 정보가 바로 삭제되고 되돌릴 수 없어요.',
  button: '회원 탈퇴',
  // 탈퇴할 수 없는 역할 (FR-028)
  blocked: {
    STAFF: '직원 계정은 탈퇴할 수 없어요. 탈퇴하려면 관리자에게 직원 해제를 요청해 주세요.',
    ADMIN: '관리자 계정은 탈퇴할 수 없어요.',
  },
}

export const withdrawConfirm = {
  title: '회원 탈퇴',
  lead: '탈퇴하기 전에 꼭 확인해 주세요.',
  // provider: '카카오' | '네이버'. 굵게 보여 줄 부분은 { strong }으로 나눈다
  notices: (provider) => [
    ['이름·휴대전화 번호·이메일이 바로 삭제되고 되돌릴 수 없어요.'],
    [{ strong: provider }, ' 계정과 몽키즈의 연결도 끊겨요. 같은 계정으로 다시 가입하면 새 회원으로 시작해요.'],
    ['작성한 후기·게시물·댓글은 지워지지 않고, 작성자는 "탈퇴 회원"으로 표시돼요.'],
    ['남기고 싶지 않은 후기·댓글은 탈퇴 전에 직접 삭제해 주세요.'],
    ['본인 확인을 위해 ', { strong: provider }, ' 로그인을 한 번 더 진행해요.'],
  ],
  check: '안내를 모두 확인했어요',
  cancel: '취소',
  submit: (provider) => `${provider}로 본인 확인 후 탈퇴`,
  close: '닫기',
}

export const withdrawDone = {
  title: '탈퇴가 완료되었어요',
  description: '그동안 몽키즈와 함께해 주셔서 감사합니다.',
  confirm: '확인',
}

// 백엔드가 /me?withdrawError=코드로 돌려보낸 탈퇴 실패 사유 (back/docs/api-guidelines.md 6)
export const withdrawErrors = {
  cancelled: '본인 확인을 취소해서 탈퇴하지 않았어요.',
  A001: '로그인이 끝났어요. 다시 로그인한 뒤 탈퇴해 주세요.',
  M004: '관리자·직원 계정은 탈퇴할 수 없어요.',
  M005: '가입한 소셜 계정과 다른 계정으로 확인했어요. 가입한 계정으로 다시 시도해 주세요.',
  M006: '소셜 계정 연결을 끊지 못해 탈퇴하지 않았어요. 잠시 후 다시 시도해 주세요.',
  default: '탈퇴하지 못했어요. 잠시 후 다시 시도해 주세요.',
}
