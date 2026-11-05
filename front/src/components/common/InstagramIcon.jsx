import { useId } from 'react'

// 인스타그램 로고 (플로팅 메뉴 바로가기). 공식 글리프 모양에 브랜드 그라데이션을 채운다
function InstagramIcon({ size = 28 }) {
  const gradientId = useId()
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" aria-hidden="true">
      <defs>
        <radialGradient id={gradientId} cx="30%" cy="107%" r="150%">
          <stop offset="0%" stopColor="#fdf497" />
          <stop offset="5%" stopColor="#fdf497" />
          <stop offset="45%" stopColor="#fd5949" />
          <stop offset="60%" stopColor="#d6249f" />
          <stop offset="90%" stopColor="#285aeb" />
        </radialGradient>
      </defs>
      <rect x="1" y="1" width="22" height="22" rx="6.5" fill={`url(#${gradientId})`} />
      <rect x="5.5" y="5.5" width="13" height="13" rx="4" fill="none" stroke="#fff" strokeWidth="1.8" />
      <circle cx="12" cy="12" r="3.1" fill="none" stroke="#fff" strokeWidth="1.8" />
      <circle cx="16.1" cy="7.9" r="1" fill="#fff" />
    </svg>
  )
}

export default InstagramIcon
