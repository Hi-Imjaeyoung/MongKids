// 로그인·회원가입 모달(SCR-013)과 헤더 로그인 영역 문구

export const loginModal = {
  title: '몽키즈 시작하기',
  description: ['카카오 또는 네이버 계정으로', '간편하게 로그인·회원가입할 수 있어요.'],
  notice: '처음 로그인하면 자동으로 회원가입됩니다.',
  errorMessage: '로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.',
}

export const socialProviders = [
  { id: 'kakao', label: '카카오로 시작하기', icon: '💬' },
  { id: 'naver', label: '네이버로 시작하기', icon: 'N' },
]

export const headerAuth = {
  login: '로그인',
  logout: '로그아웃',
  memberSuffix: '님',
}
